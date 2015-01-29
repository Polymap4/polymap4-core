/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.apache.commons.logging;

/**
 * Replacement for org.apache.commons.logging classes.
 * <p/>
 * Polymap started out with commons-logging. A lot of code depends on it. However,
 * it causes some problem with RAP.
 * <ul>
 * <li>Initialization: Thread ClassLoader is bundle specific</li>
 * <li>no global caches, no leak problem</li>
 * <li>just hard code my log message style</li>
 * </ul> 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LogFactory {

    private static final LogImpl.Level      DEFAULT_LEVEL = LogImpl.Level.INFO;

    public static Log getLog( Class clazz ) {
        return new LogImpl( clazz.getSimpleName(), DEFAULT_LEVEL );
    }
    
    public static Log getLog( String name ) {
        return new LogImpl( name, DEFAULT_LEVEL );
    }
    
}
