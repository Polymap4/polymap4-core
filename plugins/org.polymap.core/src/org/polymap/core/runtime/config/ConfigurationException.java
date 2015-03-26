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
package org.polymap.core.runtime.config;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConfigurationException
        extends RuntimeException {

    public ConfigurationException() {
        super();
    }

    public ConfigurationException( String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace ) {
        super( message, cause, enableSuppression, writableStackTrace );
    }

    public ConfigurationException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ConfigurationException( String message ) {
        super( message );
    }

    public ConfigurationException( Throwable cause ) {
        super( cause );
    }

}
