/*
 * polymap.org Copyright 2009, Polymap GmbH, and individual contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 * 
 * $Id$
 */
package org.polymap.core.model.security;

import java.security.Principal;

import org.polymap.core.model.ModelProperty;

/**
 * The access control list API of entities.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface ACL {

    public static final String      PROP_ACL = "acl";

    /**
     * One entry of an {@link ACL}. An entry is immutable. The ACL interface
     * has to be used to change the ACL. 
     */
    public interface Entry {
        
        public Principal getPrincipal();
        
        public Iterable<AclPermission> permissions();
        
    }
    
    
    /**
     * Updates the ACL of the entity.
     * <p>
     * ModelProperty: {@link #PROP_LABEL}
     */
    @ModelProperty(PROP_ACL)
    public boolean addPermission( String principal, AclPermission... permissions );

    
    /**
     * Updates the ACL of the entity.
     * <p>
     * ModelProperty: {@link #PROP_LABEL}
     */
    @ModelProperty(PROP_ACL)
    public boolean removePermission( String principal, AclPermission... permissions );


    /**
     * Checks the given permission for the given principal.
     * <p>
     * Use {@link ACLUtils} if you want to check permission for a given entity.
     * 
     * @param principal
     * @param permission
     * @return True, if the permission is granted.
     */
    public boolean checkPermission( Principal principal, AclPermission permission );

    
    public Iterable<Entry> entries();
    
}
