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
package org.polymap.core.style.model.feature;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;

import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.filter.Filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.polymap.core.runtime.Timer;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyValue;

/**
 * Base class for {@link Filter} target {@link StylePropertyValue}s. For example used
 * by {@link Style#visibleIf}.
 * 
 * @author Falko Bräutigam
 */
public abstract class FilterStyleProperty
        extends StylePropertyValue<Filter> {

    private static final Log log = LogFactory.getLog( FilterStyleProperty.class );
    
    private final static QName name = org.geotools.filter.v1_1.OGC.Filter;

    private static final Configuration ENCODE_CONFIG = new org.geotools.filter.v1_1.OGCConfiguration();

    private static final Configuration CONFIGURATION = new OGCConfiguration();

    public static final Charset ENCODE_CHARSET = Charset.forName( "UTF-8" );

//    private static final ThreadLocal<Encoder> ENCODERS = ThreadLocal.withInitial( () -> {
//        Encoder encoder = new Encoder( CONFIGURATION );
//        encoder.setIndenting( true );
//        encoder.setEncoding( ENCODE_CHARSET );
//        encoder.setNamespaceAware( false );
//        encoder.setOmitXMLDeclaration( true );
//        return encoder;
//    });

    private static final ThreadLocal<Parser> PARSERS = ThreadLocal.withInitial( () -> {
        Parser parser = new Parser( ENCODE_CONFIG );
        parser.setValidating( false );
        return parser;
    });

    
    // instance *******************************************
    
    public abstract Filter filter();
    
    
    // Filter encode/decode *******************************
    
    /**
     * 
     * @throws RuntimeException Any parser problem is wrapped into an
     *         {@link RuntimeException}.
     */
    public static Filter decode( String encoded ) throws RuntimeException {
        try {
            Timer t = new Timer().start();
            Object result = PARSERS.get().parse( new ByteArrayInputStream( encoded.getBytes( ENCODE_CHARSET ) ) );
            log.info( "Filter decoded (" + t.elapsedTime() + "ms): " );
            if (result instanceof Filter) {
                return (Filter)result;
            }
            if (result instanceof String && StringUtils.isBlank( (String)result )) {
                return Filter.INCLUDE;
            }
            throw new RuntimeException( "unknown parser result " + result );
        }
        catch (Exception e) {
            throw Throwables.propagate( e );
        }
    }


    /**
     * 
     * @throws RuntimeException Any parser problem is wrapped into an
     *         {@link RuntimeException}.
     */
    public static String encode( Filter filter ) throws RuntimeException {
        try {
            Timer t = new Timer().start();
            Encoder encoder = new Encoder( CONFIGURATION );
            encoder.setIndenting( true );
            encoder.setEncoding( ENCODE_CHARSET );
            encoder.setNamespaceAware( false );
            encoder.setOmitXMLDeclaration( true );
            
            String encoded = encoder.encodeAsString( filter, name );
            log.info( "Filter encoded (" + t.elapsedTime() + "ms): " );
            return encoded;
        }
        catch (Exception e) {
            throw Throwables.propagate( e );
        }
    }

}
