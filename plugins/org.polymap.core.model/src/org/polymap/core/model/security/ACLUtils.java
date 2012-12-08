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
package org.polymap.core.model.security;

import java.security.Principal;

import static org.polymap.core.model.Messages.i18n;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.Authentication;
import org.polymap.core.security.SecurityUtils;

/**
 * The static methods of this class define the {@link ACL} based security model
 * of POLYMAP3. It defines the admin role, default access, daemon access and so
 * on.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public final class ACLUtils {

    /**
     * 
     * @param entity
     * @param permission
     * @param throwException
     * @return True, if at least one of the principals of the current subject
     *         has the given permission on the given entity.
     * @throws SecurityException
     */
    public static boolean checkPermission( ACL entity, AclPermission permission,
            boolean throwException)
            throws SecurityException {
        
//        // check daemon thread (no session)
//        if (Polymap.getSessionDisplay() == null) {
//            // daemon threads are on admin level
//            return true;
//        }
        
        // user session principals
        for (Principal principal : Polymap.instance().getPrincipals()) {
            // check admin
            if (SecurityUtils.isAdmin( principal )) {
                return true;
            }
            // check permission
            else if (entity.checkPermission( principal, permission )) {
                return true;
            }
        }
        
        // ALL group
        if (entity.checkPermission( Authentication.ALL, permission )) {
            return true;
        }
        
        // nothing found
        if (throwException) {
            throwException( entity, permission );
        }
        return false;
    }
    
    
    private static void throwException( ACL entity, AclPermission permission ) {
        StringBuilder principals  = new StringBuilder();
        for (Principal principal : Polymap.instance().getPrincipals()) {
            principals.append( principal.getName() ).append( " " );
        }

        if (AclPermission.READ.equals( permission )) {
            throw new SecurityException( i18n( "ACL_noReadPermission", principals.toString() ) );
        }
        else if (AclPermission.WRITE.equals( permission )) {
            throw new SecurityException( i18n( "ACL_noWritePermission", principals.toString() ) );
        }
        else if (AclPermission.DELETE.equals( permission )) {
            throw new SecurityException( i18n( "ACL_noDeletePermission", principals.toString() ) );
        }
        else if (AclPermission.ACL.equals( permission )) {
            throw new SecurityException( i18n( "ACL_noAclPermission", principals.toString() ) );
        }
        else {
            throw new SecurityException( "Keine Berechtigung." );
        }
    }
    
}
