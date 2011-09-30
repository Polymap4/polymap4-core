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

package org.polymap.core.qi4j.security;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;

import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.TransientProperty;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.runtime.Polymap;

/**
 * Checks if the session principals have proper READ/WRITE permissions
 * on the invoked entity.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ACLCheckConcern
        extends GenericConcern {

    private static Log log = LogFactory.getLog( ACLCheckConcern.class );

    @This ACL               composite;
    
    @Structure Module       _module;

    
    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable {                

        if (!(composite instanceof ACL)) {
            log.warn( "Entity is not instanceof ACL, skipping ACL check." );
        }
        
        // skip internal ACL methods
        else if (method.getName().equals( "ownerName" )
                || method.getName().equals( "aclEntries" )
                || method.getName().equals( "checkPermission" )
                // XXX these methods do not actually change the model
                || method.getName().equals( "setVisible" )
                || method.getName().equals( "setSelectable" )
                || method.getName().equals( "setEditable" )
                || method.getName().equals( "setLayerStatus" )
                || method.getName().equals( "setExtent" )
                || method.getName().equals( "updateExtent" )
                || method.getName().equals( "identity" )) {
            return next.invoke( proxy, method, args );
        }
        
        else if (!Qi4jPlugin.isInitialized()) {
            log.debug( "Qi4JPlugin still about to initialize. Skipping this modification." );
        }
        
        else if (Polymap.getSessionDisplay() == null) {
            log.debug( "!!! No session when modifying entity !!!" );
        }
        
        else {
            // check READ permission
            ACLUtils.checkPermission( composite, AclPermission.READ, true );
            
            // any property annotation present?
            ModelProperty a = method.getAnnotation( ModelProperty.class );
            TransientProperty a2 = method.getAnnotation( TransientProperty.class );

            if (a != null || a2 != null) {
                log.debug( "invoke(): annotation found for: " + method.getName() );

                // check WRITE permission
                ACLUtils.checkPermission( composite, AclPermission.WRITE, true );
            }
        }
        
        // call underlying
        Object result = next.invoke( proxy, method, args );
        
        return result;
    }

}
