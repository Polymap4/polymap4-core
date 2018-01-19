/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.data.rs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.geotools.GML;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class SchemaCoder {

    private static final Log log = LogFactory.getLog( SchemaCoder.class );
    
    public String encode( FeatureType schema ) throws Exception {
        return gmlEncode( (SimpleFeatureType)schema );
    }
    
    
    public FeatureType decode( String input ) throws Exception{
        return gmlDecode( input );
    }
    

    // GML ************************************************
    
    private static final Pattern COMPLEX_TYPE = Pattern.compile( "<xsd:complexType name=\"([^\"]+)\">" );
    
    // bundle and its test files are encoded in ISO-8859-1
    private static final Charset ENCODING = Charset.forName( "ISO-8859-1" );
    
    
    public FeatureType gmlDecode( String input ) throws Exception{
        GML coder = new GML( GML.Version.WFS1_1 );
        coder.setEncoding( ENCODING );
        File f = File.createTempFile( getClass().getName(), "xsd" );
        try {
            FileUtils.write( f, input, ENCODING );
            URL url = f.toURI().toURL();
            // find name
            Matcher match = COMPLEX_TYPE.matcher( input );
            if (!match.find()) {
                throw new IllegalStateException( "No <xsd:complexType name=... found." );
            }
            String name = match.group( 1 );
            return coder.decodeSimpleFeatureType( url, new NameImpl( name ) );
        }
        finally {
            f.delete();
        }
    }

    
    protected String gmlEncode( SimpleFeatureType schema ) throws Exception {
        GML coder = new GML( GML.Version.WFS1_1 );
        coder.setEncoding( ENCODING );
        OutputStream out = new ByteArrayOutputStream( 4096 );
        coder.setBaseURL( new URL( "file:///" ) );
        coder.setCoordinateReferenceSystem( schema.getCoordinateReferenceSystem() );
//        encoder.setNamespace( "location", locationURL.toExternalForm() );
        coder.encode( out, schema );
        return out.toString();
    }
    
}
