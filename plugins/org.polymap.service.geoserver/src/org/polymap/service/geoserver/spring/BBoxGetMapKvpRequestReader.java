/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.service.geoserver.spring;

import java.util.Map;

import org.geoserver.wfs.kvp.BBoxKvpParser;
import org.geoserver.wms.GetMapRequest;
import org.geoserver.wms.WMS;
import org.geoserver.wms.map.GetMapKvpRequestReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.Version;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * TODO: This logic shouldn't be necessary as parsing the BBox parameters in a GetMap
 * request should be done as part of
 * {@link org.geoserver.wms.map.GetMapKvpRequestReader#read(Object, Map, Map)} either
 * directly, or in its super class, or via discovered service, e.g. implementing
 * org.geotools.util.ConverterFactory to convert String to Envelope.
 * 
 * @author Joerg Reichert
 * @author Falko Bräutigam
 */
public class BBoxGetMapKvpRequestReader
        extends GetMapKvpRequestReader {

    public BBoxGetMapKvpRequestReader( WMS wms ) {
        super( wms );
    }

    
    @Override
    public GetMapRequest read( Object request, Map kvp, Map rawKvp ) throws Exception {
        GetMapRequest getMapRequest = super.read( request, kvp, rawKvp );
        
        String bbox = String.valueOf( kvp.get( "bbox" ) );
        if (bbox != null) {
            String version = String.valueOf( kvp.get( "version" ) );
            String srs = String.valueOf( kvp.get( "srs" ) );
            String crs = String.valueOf( kvp.get( "crs" ) );

            BBoxKvpParser bboxKvpParser = new BBoxKvpParser();
            bboxKvpParser.setRequest( kvp.get( "request" ).toString() );
            bboxKvpParser.setService( kvp.get( "service" ).toString() );
            bboxKvpParser.setVersion( new Version( version ) );

            Envelope envelope = (Envelope)bboxKvpParser.parse( bbox );

            // XXX is this ok?
            if (version != null && version.startsWith( "1.3.0" )) {
                // swap longitude and latitude as described here:
                // http://docs.geoserver.org/stable/en/user/services/wms/basics.html#axis-ordering
                double minX = envelope.getMinY();
                double maxX = envelope.getMaxY();
                double minY = envelope.getMinX();
                double maxY = envelope.getMaxX();
                CoordinateReferenceSystem crsCode = null;
                if (srs != null) {
                    crsCode = CRS.decode( srs );
                }
                else if (crs != null) {
                    crsCode = CRS.decode( crs );
                }
                envelope = new ReferencedEnvelope( minX, maxX, minY, maxY, crsCode );
            }
            getMapRequest.setBbox( envelope );
        }
        return getMapRequest;
    }
    
}
