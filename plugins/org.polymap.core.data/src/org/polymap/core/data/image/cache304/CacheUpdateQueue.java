/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.IRecordStore.Updater;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CacheUpdateQueue {

    private static Log log = LogFactory.getLog( CacheUpdateQueue.class );

    private Cache304            cache;

    private Queue<Command>      queue = new LinkedList();
    
    
    public CacheUpdateQueue( Cache304 cache ) {
        this.cache = cache;
    }

    
    public synchronized void push( Command command ) {
        queue.add( command );    
    }

    
    public synchronized void adaptCacheResult( List<CachedTile> tiles, RecordQuery query ) {
        log.debug( "adaptCacheResult(): elements in queue: " + queue.size() );
        for (Command command : queue) {
            command.adaptCacheResult( tiles, query );
        }
    }
    
            
    public synchronized void flush() {
        Timer timer = new Timer();
        
        Updater updater = cache.store.prepareUpdate();
        log.debug( "flush(): elements in queue: " + queue.size() );
        while (!queue.isEmpty()) {
            try {
                queue.poll().apply( updater );
            }
            catch (Exception e) {
                log.error( "Error while flushing command queue: ", e );
            }
        }
        updater.apply();
        log.debug( "flush(): done. (" + timer.elapsedTime() + "ms)" );
    }
    
    
    /**
     * 
     */
    abstract static class Command {
        
        public abstract void apply( Updater updater ) 
        throws Exception;
        
        public abstract void adaptCacheResult( List<CachedTile> tiles, RecordQuery query );
    }
    
    
    /**
     * 
     */
    static class StoreCommand
            extends Command {
        
        private CachedTile          tile;
        

        public StoreCommand( CachedTile tile ) {
            this.tile = tile;
        }
        
        public void apply( Updater updater ) 
        throws Exception {
            updater.store( tile.state() );
        }

        public void adaptCacheResult( List<CachedTile> tiles, RecordQuery query ) {
            SimpleQuery simpleQuery = (SimpleQuery)query;
            for (QueryExpression exp : simpleQuery.expressions()) {
                if (!exp.evaluate( tile.state() )) {
                    return;
                }
            }
            tiles.add( tile );
        }
    
    }

    
    /**
     * 
     */
    static class TouchCommand
            extends Command {
        
        private CachedTile          tile;
        

        public TouchCommand( CachedTile tile ) {
            this.tile = tile;
        }
        
        public void apply( Updater updater ) 
        throws Exception {
            updater.store( tile.state() );
        }

        public void adaptCacheResult( List<CachedTile> tiles, RecordQuery query ) {
        }
    
    }
    
    
}
