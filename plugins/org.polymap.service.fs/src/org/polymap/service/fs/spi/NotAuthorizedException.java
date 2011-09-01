/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.spi;

/**
 * Indicates that the current user is not able to perform the requested operation
 * <p/>
 * This should not normally be used. Instead, a resource should determine if a user
 * can perform an operation in its authorised() method (yet to be introduced).
 * However, this exception allows for cases where the authorised status can only be
 * determined during processing.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NotAuthorizedException
        extends Exception {

    public NotAuthorizedException() {
        super();
    }

    public NotAuthorizedException( String message, Throwable cause ) {
        super( message, cause );
    }

    public NotAuthorizedException( String message ) {
        super( message );
    }

    public NotAuthorizedException( Throwable cause ) {
        super( cause );
    }
    
}
