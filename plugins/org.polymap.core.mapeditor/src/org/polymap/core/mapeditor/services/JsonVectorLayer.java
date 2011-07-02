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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    
    
    public JsonVectorLayer( String name, SimpleJsonServer jsonServer,
            JsonEncoder jsonEncoder, StyleMap styleMap ) {
        
        super( name, new Protocol( Protocol.TYPE.HTTP, 
                jsonServer.getURL() + "/" + jsonEncoder.getName(), "GeoJSON" ), 
                styleMap );
        this.jsonEncoder = jsonEncoder;
        this.jsonServer = jsonServer;
        log.debug( "URL: " + jsonServer.getURL() + "/" + jsonEncoder.getName() );
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
    
}
