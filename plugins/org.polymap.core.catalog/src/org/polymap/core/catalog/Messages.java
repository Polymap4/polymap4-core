/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.catalog;

import java.text.MessageFormat;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.MessagesImpl;

/**
 * The messages of the <code>org.polymap.core.services</code> plugin.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a> 
 */
public class Messages {

    private static final String BUNDLE_NAME = CatalogPlugin.PLUGIN_ID + ".messages"; //$NON-NLS-1$

    private static final MessagesImpl instance = new MessagesImpl( BUNDLE_NAME, Messages.class.getClassLoader() );


    private Messages() {
        // prevent instantiation
    }


    /**
     * Find the localized message for the given key. If arguments are given, then
     * the result message is formatted via {@link MessageFormat}.
     *  
     * @param key
     * @param args If not null, then the message is formatted via
     *        {@link MessageFormat}
     * @return The message for the given key.
     */
    public static String get( String key, Object... args ) {
        return instance.get( key, args );
    }
    

    public static IMessages forPrefix( String prefix ) {
        return instance.forPrefix( prefix );
    }

}
