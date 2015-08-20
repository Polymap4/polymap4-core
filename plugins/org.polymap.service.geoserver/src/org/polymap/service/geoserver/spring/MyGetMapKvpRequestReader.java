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


/**
 * TODO: This logic shouldn't be necessary as
 * parsing the BBox parameters in a GetMap 
 * request should be done as part of 
 * {@link org.geoserver.wms.map.GetMapKvpRequestReader#read(Object, Map, Map)}
 * either directly, or in its super class, or via 
 * discovered service, e.g. implementing 
 * org.geotools.util.ConverterFactory to convert String to 
 * Envelope.
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class MyGetMapKvpRequestReader
        extends GetMapKvpRequestReader {

    /**
     * @param wms
     */
    public MyGetMapKvpRequestReader( WMS wms ) {
        super( wms );
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.wms.map.GetMapKvpRequestReader#read(java.lang.Object, java.util.Map, java.util.Map)
     */
    @Override
    public GetMapRequest read( Object request, Map kvp, Map rawKvp ) throws Exception {
        GetMapRequest getMapRequest =  super.read( request, kvp, rawKvp );
        String service = String.valueOf(kvp.get( "service" ));
        String version = String.valueOf(kvp.get( "version" ));
        String req = String.valueOf(kvp.get( "request" ));
        String srs = String.valueOf(kvp.get( "srs" ));
        String crs = String.valueOf(kvp.get( "crs" ));
        String bbox = String.valueOf(kvp.get( "bbox" ));
        if(bbox != null) {
            BBoxKvpParser bboxKvpParser = new BBoxKvpParser();
            bboxKvpParser.setRequest( req );
            bboxKvpParser.setService( service );
            bboxKvpParser.setVersion( new Version(version) );
            com.vividsolutions.jts.geom.Envelope envelope = (com.vividsolutions.jts.geom.Envelope) bboxKvpParser.parse( bbox );
            if(version != null && version.startsWith( "1.1" )) {
                // swap longitude and latitude as described here:
                // http://docs.geoserver.org/stable/en/user/services/wms/basics.html#axis-ordering
                double minX = envelope.getMinY();
                double maxX = envelope.getMaxY();
                double minY = envelope.getMinX();
                double maxY = envelope.getMaxX();
                CoordinateReferenceSystem crsCode = null;
                if(srs != null) {
                    crsCode = CRS.decode( srs );
                } else if (crs != null) {
                    crsCode = CRS.decode( crs );
                }
                envelope = new ReferencedEnvelope(minX, maxX, minY, maxY, crsCode);
            }
            getMapRequest.setBbox(envelope);
        }
        
        return getMapRequest;
    }
}
