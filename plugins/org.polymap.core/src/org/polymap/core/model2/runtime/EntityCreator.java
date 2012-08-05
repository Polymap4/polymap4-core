/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.runtime;

import org.polymap.core.model2.Entity;

/**
 * Used by {@link UnitOfWork#newEntity(Class, Object, EntityCreator)} 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface EntityCreator<T extends Entity> {

    public void create( T prototype ) throws Exception;
    
}
