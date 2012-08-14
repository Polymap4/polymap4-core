package org.polymap.core.model2.runtime;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.EntityRepositoryImpl;
import org.polymap.core.model2.store.StoreSPI;

/**
 * Fluent configuration API.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityRepositoryConfiguration {

    protected StoreSPI            store;
    
    protected Class[]             entities;
    
    
    public EntityRepository create() {
        return new EntityRepositoryImpl( this );
    }
    
    public StoreSPI getStore() {
        return store;
    }
    
    public EntityRepositoryConfiguration setStore( StoreSPI store ) {
        this.store = store;
        return this;
    }

    public Class<Entity>[] getEntities() {
        return entities;
    }
    
    public EntityRepositoryConfiguration setEntities( Class... entities ) {
        this.entities = entities;
        return this;
    }
    
}