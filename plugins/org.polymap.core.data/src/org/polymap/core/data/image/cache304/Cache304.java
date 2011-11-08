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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.io.File;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.recordstore.IRecordFieldSelector;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.IRecordStore.ResultSet;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class Cache304 {

    private static Log log = LogFactory.getLog( Cache304.class );
    
    private static final Cache304       instance;
    
    
    static {
        instance = new Cache304();
    }
    
    public static final Cache304 instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    IRecordStore                store;
    
    private CacheUpdateQueue    updateQueue = new CacheUpdateQueue( this );
    
    
    protected Cache304() {
        try {
            store = new LuceneRecordStore( new File( Polymap.getCacheDir(), "tiles" ), false );
            
            store.setIndexFieldSelector( new IRecordFieldSelector() {
                public boolean accept( String key ) {
                    return !key.equals( CachedTile.TYPE.data );
                }
            });
        }
        catch (Exception e) {
            log.error( "Error starting Cache304.", e );
        }
    }
    
    
    public CachedTile get( GetMapRequest request, Set<ILayer> layers ) {
        try {
            RecordQuery query = buildQuery( request, layers );
            ResultSet resultSet = store.find( query );
            if (resultSet.count() > 1) {
                log.warn( "More than one tile for query: " + request ); 
            }
            
            List<CachedTile> result = new ArrayList();
            for (IRecordState state : resultSet) {
                result.add( new CachedTile( state ) );
            }
            
            updateQueue.adaptCacheResult( result, query );
            
            if (result.size() > 1) {
                log.warn( "More than one tile in result: " + result.size() ); 
            }
            
            return !result.isEmpty() ? result.get( 0 ) : null;
        }
        catch (Exception e) {
            log.error( "", e );
            return null;
        }
    }
    
    
    public CachedTile put( GetMapRequest request, Set<ILayer> layers, byte[] data ) {
        try {
            // FIXME do updates async with queue
            
            CachedTile cachedTile = get( request, layers );
            if (cachedTile == null) {
                cachedTile = new CachedTile( store.newRecord() );
                long now = System.currentTimeMillis();
                cachedTile.lastModified.put( now );
                cachedTile.lastAccessed.put( now );

                cachedTile.width.put( request.getWidth() );
                cachedTile.height.put( request.getHeight() );

                assert layers.size() == 1 : "put(): more than one layer in request.";
                ILayer layer = layers.iterator().next();
                String styleHash = "hash" + layer.getStyle().createSLD( new NullProgressMonitor() ).hashCode();
                cachedTile.style.put( styleHash );

                cachedTile.layerId.put( layer.id() );
                
                ReferencedEnvelope bbox = request.getBoundingBox();
                cachedTile.minx.put( bbox.getMinX() );
                cachedTile.miny.put( bbox.getMinY() );
                cachedTile.maxx.put( bbox.getMaxX() );
                cachedTile.maxy.put( bbox.getMaxY() );
                
                cachedTile.data.put( data );
                
                updateQueue.push( new CacheUpdateQueue.StoreCommand( cachedTile ) );
            }
            return cachedTile;
        }
        catch (Exception e) {
            log.error( "", e );
            return null;
        }
    }
    
    
    protected RecordQuery buildQuery( GetMapRequest request, Set<ILayer> layers ) {
        SimpleQuery query = new SimpleQuery();

        if (request.getWidth() != -1) {
            query.eq( CachedTile.TYPE.width.name(), request.getWidth() );
        }
        if (request.getHeight() != -1) {
            query.eq( CachedTile.TYPE.height.name(), request.getHeight() );
        }
        
        if (request.getBoundingBox() != null) {
            ReferencedEnvelope bbox = request.getBoundingBox();            
            query.eq( CachedTile.TYPE.maxx.name(), bbox.getMaxX() );
            query.eq( CachedTile.TYPE.minx.name(), bbox.getMinX() );
            query.eq( CachedTile.TYPE.maxy.name(), bbox.getMaxY() );
            query.eq( CachedTile.TYPE.miny.name(), bbox.getMinY() );
            
//            // maxx > bbox.getMinX
//            query.greater( CachedTile.TYPE.maxx.name(), bbox.getMinX() );
//            // minx < bbox.getMaxX
//            query.less( CachedTile.TYPE.minx.name(), bbox.getMaxX() );
//            // maxy > bbox.getMinY
//            query.greater( CachedTile.TYPE.maxy.name(), bbox.getMinY() );
//            // miny < bbox.getMaxY
//            query.less( CachedTile.TYPE.miny.name(), bbox.getMaxY() );
        }

        if (layers != null && !layers.isEmpty()) {
            assert layers.size() == 1 : "put(): more than one layer in request: " + layers;
            ILayer layer = layers.iterator().next();

            // layerId
            query.eq( CachedTile.TYPE.layerId.name(), layer.id() );

            // style
            String styleHash = "hash" + layer.getStyle().createSLD( new NullProgressMonitor() ).hashCode();
            query.eq( CachedTile.TYPE.style.name(), styleHash );
        }
        return query;
    }
    
}
