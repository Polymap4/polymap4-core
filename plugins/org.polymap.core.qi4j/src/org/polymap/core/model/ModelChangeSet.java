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

package org.polymap.core.model;

import java.util.Map;

/**
 * A change set is a non-persistent set of changes or snapshot of the domain
 * model. It can be discarded in order to undo the changes.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface ModelChangeSet {

    /**
     * True if the given id was changed or was removed in the change set.
     * <p>
     * Implementations have to be thread safe as it is called from
     * {@link GlobalEntityChangeSets}.
     */
    public boolean hasChanges( String id );


    /**
     * Returns all entities of this change set. These are the entities that have
     * changed in this change set.
     * 
     * @return Map of entity Ids into {@link Entity}.
     */
    public Map<String,Entity> entities();
    
}
