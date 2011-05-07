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

package org.polymap.core.project.model;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;

import org.polymap.core.project.IMap;

/**
 * Provides interface and mixin to give any entity an association to its parent
 * map. In most cases this a bidirectional map, which is controlled from the map.
 * So, the {@link #setParentMap(IMap)} method must not be called directly, it is
 * automatically updated.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface ParentMap
        extends org.polymap.core.project.ParentMap {

    @Optional
    Association<IMap>       map();
    
    /**
     * The mixin.
     */
    public abstract static class Mixin
            implements ParentMap {

        public IMap getMap() {
            return map().get();
        }

        public void setParentMap( @Optional IMap map ) {
            map().set( map );
        }
    }
    
}
