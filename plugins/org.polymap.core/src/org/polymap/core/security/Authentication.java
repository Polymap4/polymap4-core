/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.security;

import java.security.Principal;
import java.security.acl.Group;

import sun.security.acl.PrincipalImpl;

/**
 * Provides the static API und SPI of the authentication system.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class Authentication {

    public static final Principal       ALL = new PrincipalImpl( "ALL" );
    
    private static Authentication       instance = new Authentication();

    
    // static API *****************************************

    /**
     * Returns the {@link Principal} the the given name. The principal represent
     * a user or a {@link Group} of users.
     * 
     * @param name
     * @return The {@link Principal} for the given name or null, if the is no
     *         such principal.
     */
    public static Principal principalForName( String name ) {
        return instance()._principalForName( name );
    }
    

    protected static final Authentication instance() {
       return instance;    
    }
    
    
    // instance SPI ***************************************
    
    protected Principal _principalForName( String name ) {
        throw new RuntimeException( "Don't know what kind of principal to return here. It has to be compatible to all principal types of the system. See ACL.CompatiblePrincipal." );
        //return new PrincipalImpl( name );
    }

}
