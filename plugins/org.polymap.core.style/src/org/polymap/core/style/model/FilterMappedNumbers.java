/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.model;

import java.util.Collection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;

import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Collections2;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.runtime.ValueInitializer;
import org.polymap.model2.test.Timer;

/**
 * Describes a constant number as style property value.
 *
 * @author Falko Bräutigam
 */
public class FilterMappedNumbers<T extends Number>
        extends StylePropertyValue<T> {

    private static Log log = LogFactory.getLog( FilterMappedNumbers.class );

    public static final Configuration   ENCODE_CONFIG = new org.geotools.filter.v1_1.OGCConfiguration();
    public static final Charset         ENCODE_CHARSET = Charset.forName( "UTF-8" );
    
    /**
     * Initializes a newly created instance with default values.
     */
    public static <R extends Number> ValueInitializer<FilterMappedNumbers<R>> defaults() {
        return new ValueInitializer<FilterMappedNumbers<R>>() {
            @Override
            public FilterMappedNumbers<R> initialize( FilterMappedNumbers<R> proto ) throws Exception {
                return proto;
            }
        };
    }
    

    // instance *******************************************
    
    public CollectionProperty<Number>           values;
    
    public CollectionProperty<String>           filters;
    
    
    public Collection<Filter> filters() {
        return Collections2.transform( filters, (encoded) -> {
            try {
                Parser parser = new Parser( ENCODE_CONFIG );
                return (Filter)parser.parse( new ByteArrayInputStream( encoded.getBytes( ENCODE_CHARSET ) ) );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        });
    }
    
    
    public FilterMappedNumbers add( T number, Filter filter ) throws IOException {
        // number
        values.add( number );
        
        // encode filter
        Timer t = Timer.startNow();
        Configuration configuration = new OGCConfiguration();
        Encoder encoder = new Encoder( configuration );
        encoder.setIndenting( true );
        encoder.setEncoding( ENCODE_CHARSET );
        encoder.setNamespaceAware( false );
        encoder.setOmitXMLDeclaration( true );
        QName name = org.geotools.filter.v1_1.OGC.Filter; //new QName( "http://www.opengis.net/ogc", "Filter" );
        String encoded = encoder.encodeAsString( filter, name );
        log.info( "Filter encoded (" + t.elapsedTime() + "ms)" );
        filters.add( encoded );
        return this;
    }
    
}
