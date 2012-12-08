/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.image.cache304;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.IRecordStore.ResultSet;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CacheStatistics {

    private Cache304            cache;
    
    private ConcurrentMap<String,AtomicInteger> layerHitCounters = new ConcurrentHashMap( 128 );
    
    private ConcurrentMap<String,AtomicInteger> layerMissCounters = new ConcurrentHashMap( 128 );
    
    
    public CacheStatistics( Cache304 cache ) {
        this.cache = cache;
    }

    
    void incLayerHitCounter( Set<ILayer> layers, boolean miss ) {
        assert layers != null;
        
        for (ILayer layer : layers) {
            
            AtomicInteger counter = layerHitCounters.get( layer.id() );
            if (counter == null) {
                layerHitCounters.putIfAbsent( layer.id(), counter = new AtomicInteger() );
            }
            counter.incrementAndGet();

            if (miss) {
                counter = layerMissCounters.get( layer.id() );
                if (counter == null) {
                    layerMissCounters.putIfAbsent( layer.id(), counter = new AtomicInteger() );
                }
                counter.incrementAndGet();
            }
        }
    }
    
    
    public int layerHitCount( ILayer layer ) {
        assert layer != null;
        AtomicInteger counter = layerHitCounters.get( layer.id() );
        return counter != null ? counter.intValue() : 0;
    }
    
    public int layerMissCount( ILayer layer ) {
        assert layer != null;
        AtomicInteger counter = layerMissCounters.get( layer.id() );
        return counter != null ? counter.intValue() : 0;
    }
    
    public long layerStoreSize( ILayer layer ) {
        try {
            SimpleQuery query = new SimpleQuery();
            query.setMaxResults( Integer.MAX_VALUE );
            query.eq( CachedTile.TYPE.layerId.name(), layer.id() );

            long result = 0;
            ResultSet resultSet = cache.store.find( query );
            for (IRecordState state : resultSet) {
                result += new CachedTile( state ).data.get().length;
            }
            return result;
        }
        catch (Exception e) {
            return -1;
        }
    }
    
    public int layerTileCount( ILayer layer ) {
        try {
            SimpleQuery query = new SimpleQuery();
            query.setMaxResults( Integer.MAX_VALUE );
            query.eq( CachedTile.TYPE.layerId.name(), layer.id() );

            ResultSet resultSet = cache.store.find( query );
            return resultSet.count();
        }
        catch (Exception e) {
            return -1;
        }
    }
    
    public long totalStoreSize() {
        return cache.store.storeSizeInByte();
    }
    
}
