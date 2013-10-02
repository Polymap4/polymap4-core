/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IMessages {
    
    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    String get( String key, Object... args );
    
    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param locale The locale to use to localize the given message.
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    String get( Locale locale, String key, Object... args );
    
    IMessages forClass( Class type );

}
