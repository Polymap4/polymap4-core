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

import java.util.ResourceBundle;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.RWT;

/**
 * Provides a default implementation as a backend for the static Messages class of a
 * bundle.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MessagesImpl {

    private static Log log = LogFactory.getLog( MessagesImpl.class );

    public static final String  SEPARATOR = "_";

    private String              bundleName;

    private ClassLoader         cl;


    public MessagesImpl( String bundleName, ClassLoader cl ) {
        this.bundleName = bundleName;
        this.cl = cl;
    }


    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    public String get( String key, Object... args ) {
        try {
            // getBundle() caches the bundles
            ResourceBundle bundle = ResourceBundle.getBundle( bundleName, RWT.getLocale(), cl );
            if (args == null || args.length == 0) {
                return bundle.getString( key );
            }
            else {
                String msg = bundle.getString( key );
                return MessageFormat.format( msg, args );
            }
        }
        catch (Exception e) {
            return StringUtils.substringAfterLast( key, SEPARATOR );
        }
    }


    public String get( Object caller, String keySuffix, Object... args ) {
        String key = new StringBuffer( 64 )
                .append( caller.getClass().getSimpleName() )
                .append( SEPARATOR )
                .append( keySuffix ).toString();
        return get( key, args );
    }

}
