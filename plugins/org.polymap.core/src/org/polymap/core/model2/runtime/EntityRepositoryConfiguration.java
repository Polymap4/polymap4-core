package org.polymap.core.model2.runtime;

import com.google.common.base.Supplier;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.EntityRepositoryImpl;
import org.polymap.core.model2.store.StoreSPI;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheManager;

/**
 * Configuration API.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityRepositoryConfiguration {

    protected StoreSPI              store;
    
    protected Class[]               entities;
    
    protected Supplier<Cache>       cacheFactory;
    
    
    /**
     * 
     */
    protected EntityRepositoryConfiguration() {
        cacheFactory = new Supplier<Cache>() {
            public Cache get() {
                return CacheManager.instance().newCache( CacheConfig.DEFAULT
                        .concurrencyLevel( 4 )
                        .initSize( 1024 )
                        .defaultElementSize( 1024 ) );
            }
        };
    }

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
    
    public <K,V> Cache<K,V> newCache() {
        return cacheFactory.get();
    }
    
    /**
     * Specifies the factory to create caches for {@link Entity} and state
     * instances.
     */
    public EntityRepositoryConfiguration setCacheFactory( Supplier<Cache> cacheFactory ) {
        this.cacheFactory = cacheFactory;
        return this;
    }

}
