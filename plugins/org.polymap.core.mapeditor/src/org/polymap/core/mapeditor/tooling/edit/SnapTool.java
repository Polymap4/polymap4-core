/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor.tooling.edit;

import static com.google.common.collect.Iterables.getFirst;
import static org.polymap.core.mapeditor.tooling.EditorTools.isEqual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.DefaultEditorTool;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerVisitor;

import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.SnappingControl;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SnapTool
        extends DefaultEditorTool 
        implements PropertyChangeListener, VectorLayerStylerAware {

    private static final int            DEFAULT_TOLERANCE = 10;

    private static Log log = LogFactory.getLog( SnapTool.class );

    private BaseLayerEditorTool         parentTool;

    private SnappingControl             control;

    private List<EditVectorLayer>       snapLayers;
    
    private VectorLayerStyler           styler;
    
    private StyleMap                    styleMap;

    private Spinner                     toleranceField;

    
    @Override
    public boolean init( IEditorToolSite site ) {
        boolean result = super.init( site );
        parentTool = (BaseLayerEditorTool)getFirst( site.filterTools( isEqual( getToolPath().removeLastSegments( 1 ) ) ), null );
        assert parentTool != null;
        return result;
    }


    @Override
    public void dispose() {
        log.debug( "dispose(): ..." );
        super.dispose();
    }

    
    /**
     * Called when selected layer of parent tool has changed.
     */
    @Override
    public void propertyChange( PropertyChangeEvent ev ) {
        if (ev.getPropertyName().equals( BaseLayerEditorTool.PROP_LAYER_ACTIVATED )) {
            onDeactivate();
            onActivate();
        }
    }


    @Override
    public void onActivate() {
        log.debug( "onActivate(): ..." );
        
        // create snapLayers
        snapLayers = new ArrayList();
        final Predicate<ILayer> isVector = BaseLayerEditorTool.isVector();
        parentTool.getSelectedLayer().getMap().visit( new LayerVisitor() {
            public void visit( ILayer layer ) {
                if (layer.isVisible() && isVector.apply( layer )) {
                    try {
                        EditVectorLayer snapLayer = new EditVectorLayer( getSite().getEditor(), layer );
                        snapLayer.activate();

                        ReferencedEnvelope bounds = getSite().getEditor().getMap().getExtent();
                        snapLayer.selectFeatures( bounds, false );

                        snapLayers.add( snapLayer );
                        getSite().getEditor().addLayer( snapLayer.getVectorLayer() );
                    }
                    catch (Exception e) {
                        log.warn( "", e );
                    }
                }
            }
        });

        // vector styler
        styler = new VectorLayerStyler() {
            protected void styleChanged( StyleMap newStyleMap ) {
                if (styleMap != null) {
                    styleMap.dispose();
                }
                styleMap = newStyleMap;
                for (EditVectorLayer snapLayer : snapLayers) {
                    snapLayer.getVectorLayer().setStyleMap( styleMap );
                    snapLayer.getVectorLayer().redraw();
                }
            }
        };        
        Map<String,Object> standard = new HashMap();
        standard.put( "strokeWidth", 1 );
        standard.put( "strokeDashstyle", "dot" );
        standard.put( "strokeColor", new RGB( 80, 80, 80 ) );
        standard.put( "strokeOpacity", 1 );
        standard.put( "fillOpacity", 0 );
        styler.changeStyles( standard, false );

        // control
        VectorLayer[] targetLayers = Iterables.toArray( Lists.transform( snapLayers, BaseVectorLayer.toVectorLayer()) , VectorLayer.class );
        int tolerance = toleranceField != null ? toleranceField.getSelection() : DEFAULT_TOLERANCE;
        control = new SnappingControl( parentTool.getVectorLayer().getVectorLayer(), targetLayers, false, tolerance );
        getSite().getEditor().addControl( control );
        control.activate();
        
        parentTool.addListener( this );
    }


    @Override
    public void createPanelControl( Composite parent ) {
        super.createPanelControl( parent );
        
        // toleranceField
        toleranceField = getSite().getToolkit().createSpinner( parent );
        toleranceField.setMaximum( 50 );
        toleranceField.setMinimum( 0 );
        toleranceField.setSelection( 10 );
        layoutControl( i18n( "toleranceLabel" ), toleranceField );
        toleranceField.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {
                onDeactivate();
                onActivate();
            }
        });
        
        // vector style
        styler.createPanelControl( parent, this );
    }


    @Override
    public void onDeactivate() {
        log.debug( "onDeactivate(): ..." );
        parentTool.removeListener( this );
        if (toleranceField != null) {
            toleranceField.dispose();
            toleranceField = null;
        }
        if (control != null) {
            getSite().getEditor().removeControl( control );
            control.destroy();
            control.dispose();
            control = null;
        }
        for (EditVectorLayer snapLayer : snapLayers) {
            snapLayer.dispose();
        }
        snapLayers = null;
        styler = null;
        if (styleMap != null) {
            styleMap.dispose();
            styleMap = null;
        }
    }


    /** Allow {@link VectorLayerStyler} to access. */
    @Override
    public IEditorToolSite getSite() {
        return super.getSite();
    }


    /** Allow {@link VectorLayerStyler} to access. */
    @Override
    public void layoutControl( String _label, Control _control ) {
        super.layoutControl( _label, _control );
    }


    public String i18n( String key, Object... args ) {
        return Messages.get( "SnapTool_" + key, args );    
    }

}
