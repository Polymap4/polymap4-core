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

package org.polymap.core.qi4j;

import java.lang.reflect.Method;

import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.ModelChangeSet;
import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.TransientProperty;
import org.polymap.core.runtime.Polymap;

/**
 * Lets the entity participate on the current {@link ModelChangeSet} of its
 * {@link org.polymap.core.model.Module}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ModificationConcern
        extends GenericConcern {

    private static Log log = LogFactory.getLog( ModificationConcern.class );

    @This EntityComposite   composite;
    
    @Structure Module       _module;

    
    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable {                
        QiModule applied = null;
        
        ModelProperty a = method.getAnnotation( ModelProperty.class );
        TransientProperty a2 = method.getAnnotation( TransientProperty.class );

        if (!Qi4jPlugin.isInitialized()) {
            log.debug( "Qi4JPlugin still about to initialize. Skipping this modification." );
        }
        else if (Polymap.getSessionDisplay() == null) {
            log.debug( "!!! No session when modifying entity !!!" );
        }
        else {
            // any property annotation present?
            if (a != null || a2 != null /*|| method.getName().startsWith( "set" )*/) {
                log.debug( "invoke(): method=" + method.getName() );

                // find the module to apply
                applied = Qi4jPlugin.Session.instance().resolveModule( _module );

                // add this to the current changeset
                if (a != null) {
                    NestedChangeSet changeSet = (NestedChangeSet)applied.currentChangeSet();
                    changeSet.compositeUpdate( (Identity)composite );
                }
            }
        }
        
        // call underlying
        Object result = next.invoke( proxy, method, args );
        
        // fire event
        if (applied != null) {
            String propName = a != null ? a.value() : a2.value();
            applied.fireChangeEvent( proxy, propName, null, args[0] );
        }
        
        return result;
    }

}
