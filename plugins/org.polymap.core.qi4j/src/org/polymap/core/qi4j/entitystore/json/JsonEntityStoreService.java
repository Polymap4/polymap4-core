/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
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
package org.polymap.core.qi4j.entitystore.json;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entitystore.EntityStateVersions;
import org.qi4j.spi.entitystore.EntityStore;

/**
 * EntityStore backed by plain files and JSON object representation.
 * <p/>
 * Each entity is stored under its identity name in the file system.
 * <p/>
 * Property types are converted to native Preferences API types
 * as much as possible. All others will be serialized to a string using JSON.
 * <p/>
 * ManyAssociations are stored as multi-line strings (one identity
 * per line), and Associations are stored as the identity
 * of the referenced Entity.
 * <p/>
 * The main use of the EntityStore is for storage of ConfigurationComposites for ServiceComposites.
 *
 * @see ServiceComposite
 * @see org.qi4j.api.configuration.Configuration
 */
 
// _p3: falko: polymap3 handles concurrent modifications; this concern causes
// error when objects are saved twice (dont know why) and it is overhead
//@Concerns( ConcurrentModificationCheckConcern.class )

@Mixins( JsonEntityStoreMixin.class )
public interface JsonEntityStoreService
    extends EntityStore, ServiceComposite, EntityStateVersions, Activatable {
}
