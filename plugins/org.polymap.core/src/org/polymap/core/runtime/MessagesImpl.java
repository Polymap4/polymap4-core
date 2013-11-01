/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.runtime;

import java.util.Locale;
import java.util.ResourceBundle;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a default implementation as a backend for the static Messages class of a
 * bundle.
 * 
 * @see ResourceBundle
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MessagesImpl
        implements IMessages {

    private static Log log = LogFactory.getLog( MessagesImpl.class );

    public static final String  SEPARATOR = "_";

    private String              bundleName;

    private ClassLoader         cl;
    
    private String              prefix;


    /**
     * 
     * @param bundleName The name of the resource bundle. This is different from the
     *        OSGi bundle name.For example: PLUGIN_ID + ".messages".
     * @param cl The {@link ClassLoader} to be used to load the resources.
     */
    public MessagesImpl( String bundleName, ClassLoader cl ) {
        this( bundleName, cl, null );
    }

    /**
     * 
     * @param bundleName The name of the resource bundle. This is different from the
     *        OSGi bundle name.For example: PLUGIN_ID + ".messages".
     * @param cl The {@link ClassLoader} to be used to load the resources.
     * @param prefix
     */
    public MessagesImpl( String bundleName, ClassLoader cl, String prefix ) {
        this.bundleName = bundleName;
        this.cl = cl;
        this.prefix = prefix != null ? prefix + SEPARATOR : "";
    }


    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    @Override
    public String get( String key, Object... args ) {
        Locale locale = Polymap.getSessionLocale();
        return get( locale, key, args );
    }


    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param locale The locale to use to localize the given message.
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    @Override
    public String get( Locale locale, String key, Object... args ) {
        try {
            // getBundle() caches the bundles
            ResourceBundle bundle = ResourceBundle.getBundle( bundleName, locale, cl );
            if (args == null || args.length == 0) {
                return bundle.getString( prefix + key );
            }
            else {
                String msg = bundle.getString( prefix + key );
                return MessageFormat.format( msg, args );
            }
        }
        catch (Exception e) {
            return StringUtils.substringAfterLast( key, SEPARATOR );
        }
    }


    public String get( Object caller, String keySuffix, Object... args ) {
        String key = new StringBuilder( 64 )
                .append( caller.getClass().getSimpleName() )
                .append( SEPARATOR )
                .append( keySuffix ).toString();
        return get( key, args );
    }

    
    @Override
    public IMessages forClass( final Class type ) {
        return forPrefix( type.getSimpleName() );
    }

    public IMessages forPrefix( String _prefix ) {
        return new MessagesImpl( bundleName, cl, _prefix );
    }
    
}
