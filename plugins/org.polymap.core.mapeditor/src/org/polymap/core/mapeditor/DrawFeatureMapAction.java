/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor;

import java.util.HashMap;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.ContributionItem;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.i18n.IMessages;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.internal.Messages;

import org.polymap.rap.openlayers.base.OpenLayersEventListener;
import org.polymap.rap.openlayers.base.OpenLayersObject;
import org.polymap.rap.openlayers.base_types.OpenLayersMap;
import org.polymap.rap.openlayers.base_types.Style;
import org.polymap.rap.openlayers.base_types.StyleMap;
import org.polymap.rap.openlayers.controls.DrawFeatureControl;
import org.polymap.rap.openlayers.geometry.Geometry;
import org.polymap.rap.openlayers.layers.VectorLayer;

/**
 * Punkt digitalisieren. Setzt die Geometry im übergebenen MCase (siehe
 * {@link OrtMixin}). Feuert ein {@link PropertyChangeEvent} mit dem Namen
 * {@link #EVENT_NAME} und der Geometry als Value.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DrawFeatureMapAction
        extends ContributionItem
        implements OpenLayersEventListener {

    private static Log log = LogFactory.getLog( DrawFeatureMapAction.class );
    
    public static final IMessages   i18n = Messages.forPrefix( "KarteDigitalisieren" ); //$NON-NLS-1$

    public static final String      EVENT_NAME = "_ort_"; //$NON-NLS-1$

    private IPanelSite              site;
    
    private OpenLayersMap           map;
    
    private String                  handler;

    private DrawFeatureControl      drawControl;

    private VectorLayer             vectorLayer;
    
    private boolean                 isVectorLayerCreated;

    private Button                  btn;

    
    public DrawFeatureMapAction( MapViewer viewer, VectorLayer vectorLayer, String handler ) {
        this.site = viewer.getPanelSite();
        this.map = viewer.getMap();
        this.vectorLayer = vectorLayer;
        this.isVectorLayerCreated = vectorLayer == null;
        this.handler = handler;
    }

    
    @Override
    public void dispose() {
    }

    
    public void addListener( Object annotated ) {
        EventManager.instance().subscribe( annotated, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent input ) {
                return input.getSource() == DrawFeatureMapAction.this;
            }
        });
    }
    
    public boolean removeListener( Object annotated ) {
        return EventManager.instance().unsubscribe( annotated );
    }
    
    
    @Override
    public void fill( Composite parent ) {
        btn = site.toolkit().createButton( parent, i18n.get( "buttonTitle" ), SWT.TOGGLE );
        btn.setToolTipText( i18n.get( "buttonTip" ) );
        //btn.setImage( BatikPlugin.instance().imageForName( "resources/icons/location.png" ) );
        btn.setEnabled( true );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                if (btn.getSelection()) {
                    activate();
                }
                else {
                    deactivate();
                }
            }
        });
    }

    
    public void activate() {
        // if called from outside
        if (btn != null) {
            btn.setSelection( true );
        }
        if (isVectorLayerCreated) {
            vectorLayer = new VectorLayer( i18n.get( "ebeneMarkierung" ) );
            vectorLayer.setVisibility( true );
            vectorLayer.setIsBaseLayer( false );
            vectorLayer.setZIndex( 10000 );

            // style
            Style standard = new Style();
            standard.setAttribute( "strokeColor", i18n.get( "strokeColor" ) ); //$NON-NLS-1$
            standard.setAttribute( "strokeWidth", i18n.get( "strokeWidth" ) ); //$NON-NLS-1$
            standard.setAttribute( "pointRadius", i18n.get( "pointRadius" ) ); //$NON-NLS-1$
            StyleMap styleMap = new StyleMap();
            styleMap.setIntentStyle( "default", standard ); //$NON-NLS-1$
            styleMap.setIntentStyle( "select", standard ); //$NON-NLS-1$
            styleMap.setIntentStyle( "temporary", standard ); //$NON-NLS-1$
            vectorLayer.setStyleMap( styleMap );

            map.addLayer( vectorLayer );
        }
        
        // control
        drawControl = new DrawFeatureControl( vectorLayer, DrawFeatureControl.HANDLER_POINT );
        map.addControl( drawControl );

        // register event handler
        Map<String, String> payload = new HashMap();
        payload.put( "features", "new OpenLayers.Format.GeoJSON().write(event.feature, false)" ); //$NON-NLS-1$ //$NON-NLS-2$
        drawControl.events.register( DrawFeatureMapAction.this, DrawFeatureControl.EVENT_ADDED, payload );
        drawControl.activate();
        vectorLayer.redraw();
    }

    
    public void deactivate() {
        // if called from outside
        if (btn != null) {
            btn.setSelection( false );
        }
        if (drawControl != null) {
            map.removeControl( drawControl );
            drawControl.deactivate();
            // FIXME this crashes
            //drawControl.destroy();
            drawControl.dispose();
            drawControl = null;
        }
        if (vectorLayer != null && isVectorLayerCreated) {
            map.removeLayer( vectorLayer );
            vectorLayer.dispose();
            vectorLayer = null;
        }
    }

    
    @Override
    public void process_event( OpenLayersObject src_obj, String event_name, HashMap<String, String> payload ) {
        try {
            log.info( "JSON: " + payload.get( "features" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            FeatureJSON io = new FeatureJSON();
            SimpleFeature feature = io.readFeature( new StringReader( payload.get( "features" ) ) ); //$NON-NLS-1$
            log.info( "Feature: " + feature ); //$NON-NLS-1$
            
            Geometry geom = (Geometry)feature.getDefaultGeometry();
            onFeatureChange( geom );
        }
        catch (IOException e) {
            log.warn( "", e ); //$NON-NLS-1$
        }
    }

    
    protected void onFeatureChange( Geometry geom ) {
       log.info( geom );    
       EventManager.instance().publish( new PropertyChangeEvent( this, EVENT_NAME, null, geom ) );            
    }
    
}
