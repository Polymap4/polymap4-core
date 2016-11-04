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

import org.geoserver.wms.GetMapRequest;
import org.geoserver.wms.WMS;
import org.geoserver.wms.map.GetMapKvpRequestReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: This logic shouldn't be necessary as parsing the BBox parameters in a GetMap
 * request should be done as part of
 * {@link org.geoserver.wms.map.GetMapKvpRequestReader#read(Object, Map, Map)} either
 * directly, or in its super class, or via discovered service, e.g. implementing
 * org.geotools.util.ConverterFactory to convert String to Envelope.
 * 
 * @author Joerg Reichert
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class BBoxGetMapKvpRequestReader
        extends GetMapKvpRequestReader {

    private static final Log log = LogFactory.getLog( BBoxGetMapKvpRequestReader.class );

    public BBoxGetMapKvpRequestReader( WMS wms ) {
        super( wms );
    }

    
    @Override
    public GetMapRequest read( Object request, Map kvp, Map rawKvp ) throws Exception {
        GetMapRequest getMap = super.read( request, kvp, rawKvp );
        
        log.info( "BBOX: " + rawKvp.get( "bbox" ) );
        
//        String bbox = String.valueOf( kvp.get( "bbox" ) );
//        if (bbox != null) {
//            String version = String.valueOf( kvp.get( "version" ) );
//
//            BBoxKvpParser bboxKvpParser = new BBoxKvpParser();
//            bboxKvpParser.setRequest( kvp.get( "request" ).toString() );
//            bboxKvpParser.setService( kvp.get( "service" ).toString() );
//            bboxKvpParser.setVersion( new Version( version ) );
//
//            Envelope envelope = (Envelope)bboxKvpParser.parse( bbox );
//
//            getMap.setBbox( envelope );
//        }
        return getMap;
    }
    
}
