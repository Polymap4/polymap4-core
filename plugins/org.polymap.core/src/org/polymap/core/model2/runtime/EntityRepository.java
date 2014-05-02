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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.store.StoreSPI;

/**
 * 
 * <p/>
 * One repository is backed by exactly one underlying store. Client may decide to
 * work with different repositories and their {@link UnitOfWork} instances. It is
 * responsible of synchronizing commit/rollback between those instances.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class EntityRepository {

    private static Log log = LogFactory.getLog( EntityRepository.class );

    // config factory *************************************
    
    public static EntityRepositoryConfiguration newConfiguration() {
        return new EntityRepositoryConfiguration();
    }
    
    // instance *******************************************
    
    public abstract StoreSPI getStore();
    
    public abstract EntityRepositoryConfiguration getConfig();
    
    public abstract void close();


    /**
     * 
     * 
     * @param <T>
     * @param compositeClass Class of {@link Entity}, Mixin or complex property.
     * @return The info object, or null if the given Class is not an Entity, Mixin or
     *         complex property in this repository.
     */
    public abstract <T extends Composite> CompositeInfo<T> infoOf( Class<T> compositeClass );
    
    public abstract UnitOfWork newUnitOfWork();
    
}
