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
package org.polymap.core.model2.store;

import java.util.Collection;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.runtime.UnitOfWork;

/**
 * Represents the store interface provided by an underlying store to be
 * used by a front-end {@link UnitOfWork}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface StoreUnitOfWork {

    public <T extends Entity> CompositeState loadEntityState( Object id, Class<T> entityClass );

    public <T extends Entity> CompositeState adoptEntityState( Object state, Class<T> entityClass );

    public <T extends Entity> CompositeState newEntityState( Object id, Class<T> entityClass );

    /**
     * 
     *
     * @param <T>
     * @param entityClass
     * @return Collection of ids of the found entities.
     */
    public <T extends Entity> Collection find( Class<T> entityClass );
    
    public void prepareCommit( Iterable<Entity> loaded ) throws Exception;
    
    public void commit();
    
    public void close();

}
