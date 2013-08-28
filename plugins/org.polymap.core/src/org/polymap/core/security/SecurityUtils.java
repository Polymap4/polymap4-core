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
package org.polymap.core.security;

import java.util.Collection;

import java.security.Principal;

/**
 * Provides basic security checks.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public final class SecurityUtils {

    public static final String      ADMIN_USER = "admin";
    public static final String      ADMIN_GROUP = "admins";
    
    
    public static boolean isAdmin( Principal principal ) {
        return principal.getName().equals( ADMIN_USER )
                || principal.getName().equals( ADMIN_GROUP );
    }


    public static boolean isAdmin( Collection<Principal> principals ) {
        for (Principal principal : principals) {
            if (isAdmin( principal )) {
                return true;
            }
        }
        return false;
    }
    
}
