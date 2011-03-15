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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.model.Entity;

/**
 * Provides a helper for {@link QiModule} that manages global entity
 * versions and checks for concurrent modifications.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GlobalEntityVersions {

    private static Log log = LogFactory.getLog( GlobalEntityVersions.class );
    
    /** 
     * This holds the last commited version of the entities.
     */
    private Map<String,Integer>     entityVersions = new HashMap();

    
    public void checkSetEntityVersions( Collection<Entity> entities )
    throws ConcurrentModificationException {
        for (Entity entity : entities) {
            checkSetEntityVersion( entity );
        }
    }

    public void checkSetEntityVersion( Entity entity )
    throws ConcurrentModificationException {
        synchronized (entityVersions) {
            int version = entity.version();
            String id = entity.id();
            
            Integer old = entityVersions.put( id, version + 1 );
            if (old == null || old == version) {
                entity._version().set( version + 1 );
            }
            else {
                entityVersions.put( entity.id(), old );
                throw new ConcurrentModificationException( "Entity was concurrently modified: " + entity.id() );
            }
        }
    }
    
    public boolean isConcurrentlyCommited( Entity entity ) {
        synchronized (entityVersions) {
            Integer version = entityVersions.get( entity.id() );
            return version == null
                    ? false
                    : version > entity.version();
        }
    }

}
