/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.entity;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class ConcurrentModificationException
        extends Exception {

    public ConcurrentModificationException() {
        super();
    }

    public ConcurrentModificationException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ConcurrentModificationException( String message ) {
        super( message );
    }

    public ConcurrentModificationException( Throwable cause ) {
        super( cause );
    }

}
