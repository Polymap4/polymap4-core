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
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreUnitOfWork;

/**
 * The API to access the engine from within an {@link Entity}. Holds the
 * {@link EntityStatus status} of the entity.
 * <p/>
 * Implementation note: This approach is maybe not that elegant than any kind of
 * dependency injection but it saves the memory of the references used by dependency
 * injection. Maybe later I will search for a better solution
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface EntityRuntimeContext {

    /**
     * The status an Entity can have.
     */
    public enum EntityStatus {
        LOADED( 0 ), 
        CREATED( 1 ), 
        MODIFIED( 2 ),
        REMOVED( 3 ),
        /** This status indicates that the Entity was evicted from its UnitOfWork. */
        EVICTED( 4 );
        
        public int         status;
        
        EntityStatus( int status ) {
            this.status = status;    
        }
        
    }
    
    public CompositeInfo getInfo();

    public CompositeState getState();
    
    public EntityStatus getStatus();
    
    public void raiseStatus( EntityStatus newStatus );
    
    public void resetStatus( EntityStatus loaded );

    public UnitOfWork getUnitOfWork();

    public StoreUnitOfWork getStoreUnitOfWork();

    public EntityRepository getRepository();

    public void methodProlog( String methodName, Object[] args );
    
}
