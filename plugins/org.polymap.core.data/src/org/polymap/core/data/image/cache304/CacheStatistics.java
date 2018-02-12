/* 
 * polymap.org
 * Copyright 2012-2018, Polymap GmbH. All rights reserved.
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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.polymap.recordstore.IRecordState;
import org.polymap.recordstore.ResultSet;
import org.polymap.recordstore.SimpleQuery;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CacheStatistics {

    private ConcurrentMap<String,AtomicInteger> layerHitCounters = new ConcurrentHashMap( 128 );
    
    private ConcurrentMap<String,AtomicInteger> layerMissCounters = new ConcurrentHashMap( 128 );
    
    
    void incLayerHitCounter( List<String> layers, boolean miss ) {
        for (String layer : layers) {
            if (miss) {
                AtomicInteger counter = layerMissCounters.computeIfAbsent( layer, key -> new AtomicInteger() );
                counter.incrementAndGet();
            }
            else {
                AtomicInteger counter = layerHitCounters.computeIfAbsent( layer, key -> new AtomicInteger() );
                counter.incrementAndGet();
            }
        }
    }
    
    public int layerHitCount( String layer ) {
        assert layer != null;
        AtomicInteger counter = layerHitCounters.get( layer );
        return counter != null ? counter.intValue() : 0;
    }
    
    public int layerMissCount( String layer ) {
        assert layer != null;
        AtomicInteger counter = layerMissCounters.get( layer );
        return counter != null ? counter.intValue() : 0;
    }
    
    public long layerStoreSize( Cache304 cache, String layer ) {
        try {
            SimpleQuery query = new SimpleQuery();
            query.setMaxResults( Integer.MAX_VALUE );
            query.eq( CachedTile.TYPE.layerId.name(), layer );

            long result = 0;
            try (
                ResultSet resultSet = cache.store.find( query );
            ){
                for (IRecordState state : resultSet) {
                    result += new CachedTile( state, null ).filesize.get();
                }
            }
            return result;
        }
        catch (Exception e) {
            return -1;
        }
    }
    
    public int layerTileCount( Cache304 cache, String layer ) {
        try {
            SimpleQuery query = new SimpleQuery();
            query.setMaxResults( Integer.MAX_VALUE );
            query.eq( CachedTile.TYPE.layerId.name(), layer );

            try (ResultSet resultSet = cache.store.find( query )) {
                return resultSet.count();
            }
        }
        catch (Exception e) {
            return -1;
        }
    }
    
    public long totalStoreSize( Cache304 cache ) {
        return cache.dataDirSize.get();  //store.storeSizeInByte();
    }
    
}
