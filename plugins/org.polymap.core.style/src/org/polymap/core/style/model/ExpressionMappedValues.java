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
import java.util.List;
import java.util.stream.Collectors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;

import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.filter.expression.Expression;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Concerns;
import org.polymap.model2.Property;

/**
 * Conditions based numbers.
 * 
 * @author Steffen Stundzig
 */
public abstract class ExpressionMappedValues<T>
        extends StylePropertyValue<T> {

    public static final Configuration ENCODE_CONFIG = new org.geotools.filter.v1_1.OGCConfiguration();

    public static final Charset ENCODE_CHARSET = Charset.forName( "UTF-8" );

    private static Log log = LogFactory.getLog( ExpressionMappedValues.class );
    //
    // /**
    // * Initializes a newly created instance with default values.
    // */
    // public static <R extends Number> ValueInitializer<PropertyMappedValues<R>>
    // defaults() {
    // return new ValueInitializer<PropertyMappedValues<R>>() {
    // @Override
    // public PropertyMappedValues<R> initialize( PropertyMappedValues<R> proto )
    // throws Exception {
    // return proto;
    // }
    // };
    // }

    // instance *******************************************

    // XXX Collections are not supported yet, use force-fire-fake prop?

    @Concerns(StylePropertyChange.Concern.class)
    public Property<String> propertyName;

    // @Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<String> expressions;


    public ExpressionMappedValues add( Expression expression, T value ) throws IOException {
        expressions.add( encode( expression ) );
        return add( value );
    }


    public abstract ExpressionMappedValues add( T value );


    public abstract T defaultValue();


    public abstract Collection<T> values();


    public List<Expression> expressions() {
        return expressions.stream().map( encoded ->  decode( encoded )).collect( Collectors.toList() );
    }


    public static Expression decode( String encoded ) {
        try {
            Parser parser = new Parser( ENCODE_CONFIG );
            Object result = parser.parse( new ByteArrayInputStream( encoded.getBytes( ENCODE_CHARSET ) ) );
            if (result instanceof Expression) {
                return (Expression)result;
            }
            if (result instanceof String && StringUtils.isBlank( (String)result )) {
                return Expression.NIL;
            }
            throw new IOException( "unknown parser result " + result );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public static String encode( Expression expression ) throws IOException {
        Timer t = new Timer().start();
        Configuration configuration = new OGCConfiguration();
        Encoder encoder = new Encoder( configuration );
        encoder.setIndenting( true );
        encoder.setEncoding( ENCODE_CHARSET );
        encoder.setNamespaceAware( false );
        encoder.setOmitXMLDeclaration( true );
        QName name = org.geotools.filter.v1_1.OGC.expression; // new QName(
                                                              // "http://www.opengis.net/ogc",
                                                              // "Filter" );
        String encoded = encoder.encodeAsString( expression, name );
        log.info( "Filter encoded (" + t.elapsedTime() + "ms)" );
        return encoded;
    }
}
