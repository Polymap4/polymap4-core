/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.runtime.i18n.MessagesImpl;

/**
 * The messages of the <code>org.polymap.core.style</code> plugin.
 * 
 * @author Steffen Stundzig
 */
public class Messages {

    private static final String       BUNDLE_NAME = StylePlugin.PLUGIN_ID + ".messages";

    private static final MessagesImpl instance = new MessagesImpl( BUNDLE_NAME, Messages.class.getClassLoader() );


    public static IMessages forPrefix( String prefix ) {
        return instance.forPrefix( prefix );
    }


    // instance *******************************************

    private Messages() {
        // prevent instantiation
    }


    /**
     * @param key the message key
     * @param args objects for inserting into the message
     * @return the localized message based on Polymap.getSessionLocale() 
     */
    public static String get( String key, Object... args ) {
        return instance.get( key, args );
    }


    public static String get2( Object caller, String key, Object... args ) {
        return instance.get( caller, key, args );
    }

}
