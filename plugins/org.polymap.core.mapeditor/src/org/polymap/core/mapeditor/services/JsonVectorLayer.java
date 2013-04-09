/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.services;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.PhaseEvent;
import org.eclipse.rwt.lifecycle.PhaseId;
import org.eclipse.rwt.lifecycle.PhaseListener;

import org.polymap.openlayers.rap.widget.base_types.Protocol;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * A {@link VectorLayer} that displays features encoded by an {@link JsonEncoder}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JsonVectorLayer
        extends VectorLayer {

    private static Log log = LogFactory.getLog( JsonVectorLayer.class );
 
    private JsonEncoder         jsonEncoder;
    
    private SimpleJsonServer    jsonServer;
    
    private PhaseListener       phaseListener;
    
    
    public JsonVectorLayer( String name, SimpleJsonServer jsonServer,
            JsonEncoder jsonEncoder, StyleMap styleMap ) {
        super( name, new Protocol( Protocol.TYPE.HTTP, 
                StringUtils.removeStart( jsonServer.getPathSpec(), "/" ) + "/" + jsonEncoder.getName(),
                "GeoJSON" ), styleMap );
        this.jsonEncoder = jsonEncoder;
        this.jsonServer = jsonServer;
        
        // init phase listener: prevent subsequent refreshs if just created
        RWT.getLifeCycle().addPhaseListener( phaseListener = new PhaseListener() {
            @Override
            public PhaseId getPhaseId() {
                return PhaseId.RENDER;
            }
            @Override
            public void beforePhase( PhaseEvent event ) {
            }
            @Override
            public void afterPhase( PhaseEvent event ) {
                log.info( "AFTER PHASE: " + getName() ); 
                phaseListener = null;
                RWT.getLifeCycle().removePhaseListener( this );
            }
        });
        
        log.debug( "URL: " + jsonServer.getPathSpec() + "/" + jsonEncoder.getName() );
    }

    
    public void dispose() {
        if (jsonEncoder != null) {
            jsonServer.removeLayer( jsonEncoder );
            log.debug( "disposed: " + jsonEncoder.getName() );
            jsonEncoder = null;
        }
        super.dispose();
    }

    
    public JsonEncoder getJsonEncoder() {
        return jsonEncoder;
    }


    public SimpleJsonServer getJsonServer() {
        return jsonServer;
    }


    @Override
    public void refresh() {
        if (phaseListener == null) {
            // prevent subsequent refreshs during one request; each refresh generates
            // a refresh() call in JS that causes reload all features - even if nothing has changed
            phaseListener = new PhaseListener() {
                @Override
                public PhaseId getPhaseId() {
                    return PhaseId.RENDER;
                }
                @Override
                public void beforePhase( PhaseEvent event ) {
                }
                @Override
                public void afterPhase( PhaseEvent event ) {
                    log.info( "AFTER PHASE: " + getName() ); 
                    phaseListener = null;
                    JsonVectorLayer.super.refresh();
                    RWT.getLifeCycle().removePhaseListener( this );
                }
            };
            RWT.getLifeCycle().addPhaseListener( phaseListener );
        }
    }
    
}
