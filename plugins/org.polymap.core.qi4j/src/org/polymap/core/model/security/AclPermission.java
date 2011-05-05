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
package org.polymap.core.model.security;

import java.security.acl.Permission;

/**
 * The model entity permissions.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class AclPermission
        implements Permission {

    public static final AclPermission   READ = new AclPermission( "READ" );
    public static final AclPermission   WRITE = new AclPermission( "WRITE" );
    public static final AclPermission   DELETE = new AclPermission( "DELETE" );
    /** Permission to manipulate the permissions of an entity. */
    public static final AclPermission   ACL = new AclPermission( "ACL" );
    
    public static final AclPermission[] ALL = new AclPermission[] { READ, WRITE, DELETE, ACL };

    
    public static final AclPermission forName( String name ) {
        if (READ.name.equals( name )) {
            return READ;
        }
        else if (WRITE.name.equals( name )) {
            return WRITE;
        }
        else if (DELETE.name.equals( name )) {
            return DELETE;
        }
        else if (ACL.name.equals( name )) {
            return ACL;
        }
        else {
            throw new IllegalArgumentException( "Illegal permission: " + name );
        }
    }
    

    // instance *******************************************

    private String              name;
    

    private AclPermission( String name ) {
        this.name = name;
    }

    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof AclPermission) {
            return ((AclPermission)obj).name.equals( this.name );
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
    
}
