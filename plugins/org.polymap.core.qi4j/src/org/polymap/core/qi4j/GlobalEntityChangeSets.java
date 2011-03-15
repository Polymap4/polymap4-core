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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.util.WeakHashSet;

import org.polymap.core.model.Entity;
import org.polymap.core.model.ModelChangeSet;

/**
 * Provides a helper for {@link QiModule} that manages global entity
 * {@link ModelChangeSet}s. It allows to check for pending changes that
 * are made by other users sessions.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class GlobalEntityChangeSets {

    private static Log log = LogFactory.getLog( GlobalEntityChangeSets.class );
    
    /** Holds all modules of the VM. */
    private WeakHashSet<QiModule>       modules = new WeakHashSet( QiModule.class );

    
    void registerModule( QiModule module ) {
        synchronized (modules) {
            modules.add( module );
        }
    }
    
    void unregisterModule( QiModule module ) {
        synchronized (modules) {
            modules.remove( module );
        }
    }
    
    boolean isConcurrentlyChanged( Entity entity, QiModule caller ) {
        // there is no way to check if an entity is loading for an module,
        // so checking the state of a given id for a module always results in
        // loading this entity; to avoid this we just check the change sets
        String id = entity.id();
        synchronized (modules) {
            for (QiModule module : modules ) {
                if (!module.equals( caller )) {
                    for (ModelChangeSet changeSet : module.changeSets()) {
                        if (changeSet.hasChanges( id )) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
}
