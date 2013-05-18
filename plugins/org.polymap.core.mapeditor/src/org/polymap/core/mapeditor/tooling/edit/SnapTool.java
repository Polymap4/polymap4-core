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

import org.eclipse.ui.IMemento;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.DefaultEditorTool;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.mapeditor.tooling.ToolingEvent;
import org.polymap.core.mapeditor.tooling.ToolingEvent.EventType;
import org.polymap.core.mapeditor.tooling.ToolingListener;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerVisitor;
import org.polymap.core.runtime.Polymap;

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

    private static Log log = LogFactory.getLog( SnapTool.class );

    private static final int            DEFAULT_TOLERANCE = 10;

    private BaseLayerEditorTool         parentTool;

    private SnappingControl             control;

    private List<SnapVectorLayer>       snapLayers;
    
    private VectorLayerStyler           styler;
    
    private StyleMap                    styleMap;

    private Spinner                     toleranceField;
    
    private Integer                     tolerance;

    
    @Override
    public boolean init( IEditorToolSite site ) {
        boolean result = super.init( site );

        tolerance = site.getMemento().getInteger( "tolerance" );
        tolerance = tolerance != null ? tolerance : DEFAULT_TOLERANCE;
        
        // disable deactivation for other tools
        getSite().removeListener( this );
        
        parentTool = (BaseLayerEditorTool)getFirst( site.filterTools( isEqual( getToolPath().removeLastSegments( 1 ) ) ), null );
        assert parentTool != null;

        // vector styler
        String mementoKey = "vectorStyle";  //"vectorStyle_" + layer.id()
        IMemento stylerMemento = getSite().getMemento().getChild( mementoKey );
        stylerMemento = stylerMemento != null ? stylerMemento : getSite().getMemento().createChild( mementoKey );

        styler = new VectorLayerStyler( stylerMemento ) {
            protected void styleChanged( StyleMap newStyleMap ) {
                super.styleChanged( newStyleMap );
                if (styleMap != null) {
                    styleMap.dispose();
                }
                styleMap = newStyleMap;
                for (SnapVectorLayer snapLayer : snapLayers) {
                    snapLayer.getVectorLayer().setStyleMap( styleMap );
                    snapLayer.getVectorLayer().redraw();
                }
            }
        };
        // change default style
        if (stylerMemento.getFloat( "strokeWidth" ) == null) {
            Map<String,Object> standard = new HashMap();
            standard.put( "strokeWidth", 1f );
            standard.put( "strokeDashstyle", "dot" );
            standard.put( "strokeColor", new RGB( 80, 80, 80 ) );
            standard.put( "strokeOpacity", 1 );
            standard.put( "fillOpacity", 0 );
            styler.changeStyles( standard, false );
        }

        // listen to state changes of parentTool
        site.addListener( new ToolingListener() {
            public void toolingChanged( ToolingEvent ev ) {
                if (ev.getSource().equals( parentTool )) {
                    if (ev.getType() == EventType.TOOL_ACTIVATED) {
                        Boolean active = getSite().getMemento().getBoolean( "active" );
                        if (active != null && active) {
                            // delay triggerTool() until UI has been created
                            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                                public void run() {
                                    getSite().triggerTool( getSite().getToolPath(), true );
                                }
                            });
                        }                        
                    }
                    else if (ev.getType() == EventType.TOOL_DEACTIVATING) {
                        getSite().getMemento().putBoolean( "active", isActive() );                        
                    }
                }
            }
        });
        return result;
    }


    @Override
    public void dispose() {
        styler.dispose();
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
        super.onActivate();
        
        // create snapLayers
        snapLayers = new ArrayList();
        final Predicate<ILayer> isVector = BaseLayerEditorTool.isVector();
        parentTool.getSelectedLayer().getMap().visit( new LayerVisitor() {
            public boolean visit( ILayer layer ) {
                if (layer.isVisible() && isVector.apply( layer )) {
                    try {
                        SnapVectorLayer snapLayer = new SnapVectorLayer( getSite(), layer );
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
                return true;
            }
        });

        // control
        VectorLayer[] targetLayers = Iterables.toArray( Lists.transform( snapLayers, BaseVectorLayer.toVectorLayer()) , VectorLayer.class );
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
        toleranceField.setIncrement( 2 );
        toleranceField.setSelection( tolerance );
        layoutControl( i18n( "toleranceLabel" ), toleranceField );
        toleranceField.setToolTipText( i18n( "toleranceTip" ) );
        toleranceField.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {
                tolerance = toleranceField.getSelection();
                onDeactivate();
                onActivate();
            }
        });
        
        // vector style
        styler.createPanelControl( parent, this );
    }


    @Override
    public void onDeactivate() {
        super.onDeactivate();
        
        getSite().getMemento().putInteger( "tolerance", tolerance );
        
        parentTool.removeListener( this );
        
        if (control != null) {
            getSite().getEditor().removeControl( control );
            control.deactivate();
            control.destroy();
            control.dispose();
            control = null;
        }
        if (snapLayers != null) {
            for (SnapVectorLayer snapLayer : snapLayers) {
                snapLayer.dispose();
            }
            snapLayers = null;
        }
        
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
