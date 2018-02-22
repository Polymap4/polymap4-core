/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.feature.storecache;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import java.time.Duration;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.polymap.core.data.feature.AddFeaturesRequest;
import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.ModifyFeaturesRequest;
import org.polymap.core.data.feature.RemoveFeaturesRequest;
import org.polymap.core.data.feature.TransactionRequest;
import org.polymap.core.data.pipeline.DataSourceDescriptor;
import org.polymap.core.data.pipeline.Param;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;
import org.polymap.core.data.pipeline.ProcessorProbe;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;

/**
 * Caches the entire contents of the upstream {@link FeaturesProducer} (aka backend
 * {@link FeatureStore}) in a cache {@link DataStore}. Requests are entirely served
 * from the cache.
 *
 * @author Falko Bräutigam
 */
public class StoreCacheProcessor
        implements FeaturesProducer {

    private static final Log log = LogFactory.getLog( StoreCacheProcessor.class );

    @Param.UI( description="The sync strategy to use", values={"FullDataStoreSyncStrategy"} )
    public static final Param<String>   SYNC_TYPE = new Param( "syncType", String.class, "FullDataStoreSyncStrategy" );

    @Param.UI( description="The minimum time between subsequent updates of the cache" )
    public static final Param<Duration> MIN_UPDATE_TIMEOUT = new Param( "minTimeout", Duration.class, Duration.ofHours( 24 ) );

    /** The maximum timeout between updates of the cache. */
    public static final Param<Duration> MAX_UPDATE_TIMEOUT = new Param( "maxTimeout", Duration.class, Duration.ofDays( 3 ) );

    /** Dummy param that displays statistics in the UI. */
    @Param.UI( description="Statistics", custom=StatisticsSupplier.class )
    public static final Param           STATISTICS = new Param( "resname", String.class );

    /**
     * XXX I'm to stupid to use AtomicLong check/set methods?
     */
    static class AtomicLong2
            extends AtomicLong {

        public <E extends Exception> void checkSet( Function<Long,Boolean> check, Task<E> set ) throws E {
            if (check.apply( get() )) {
                synchronized (this) {
                    if (check.apply( get() )) {
                        set.run();
                    }
                }
            }
        }
    }
    
    /** Maps {@link PipelineProcessorSite#layerId} into atomic timestamp. */
    static ConcurrentMap<String,AtomicLong2>    lastUpdated = new ConcurrentHashMap();
    
    private static Lazy<DataAccess>             cachestore;
    
    /**
     * Initialize the global cache store.
     */
    public static void init( Callable<DataAccess> cacheStoreSupplier ) {
        assert cachestore == null : "cachestore is set already.";
        cachestore = new LockedLazyInit( () -> {
            try {
                return cacheStoreSupplier.call();
            }
            catch (Exception e) {
                throw Throwables.propagate( e );
            }
        });
    }
    
    
    // instance *******************************************
    
    private PipelineProcessorSite       site;

    private DataAccess                  cacheDs;

    private DataSourceProcessor         cache;
    
    private SyncStrategy                sync;

    
    @Override
    public void init( @SuppressWarnings( "hiding" ) PipelineProcessorSite site ) throws Exception {
        this.site = site;

        // XXX shouldn't the ressource name come from upstream schema?
        assert cachestore != null : "cachestore is not yet initialized.";
        cacheDs = cachestore.get();
        String resName = site.dsd.get().resourceName.get();
        
        lastUpdated.computeIfAbsent( site.layerId.get(), k -> new AtomicLong2() );
        
        // init sync strategy
        String classname = getClass().getPackage().getName() + "." + SYNC_TYPE.get( site );
        sync = (SyncStrategy)getClass().getClassLoader().loadClass( classname ).newInstance();
        sync.beforeInit( this, site );

        DataSourceDescriptor cacheDsd = new DataSourceDescriptor( cacheDs, resName );
        PipelineProcessorSite cacheSite = new PipelineProcessorSite( Params.EMPTY );
        cacheSite.layerId.set( site.layerId.get() );
        cacheSite.usecase.set( site.usecase.get() );
        cacheSite.builder.set( site.builder.get() );
        cacheSite.dsd.set( cacheDsd );

        cache = new DataSourceProcessor();
        cache.init( cacheSite );
        
        sync.afterInit( this, site );
    }

    
    protected DataAccess cacheDataStore() {
        return cacheDs;
    }

    
    protected <E extends Exception> void ifUpdateNeeded( Task<E> task ) throws E {
        AtomicLong2 timestamp = lastUpdated.get( site.layerId.get() );
        long timeout = MIN_UPDATE_TIMEOUT.get( site ).toMillis();
        
        log.info( "Cache timeout: T - " + Math.max(0, timestamp.get()+timeout-System.currentTimeMillis())/1000 + "s" );
        
        timestamp.checkSet( c -> c+timeout < System.currentTimeMillis(), () -> {
            task.run();
            timestamp.set( System.currentTimeMillis() );            
        });
    }
    
    
    @FunctionalInterface
    interface Task<E extends Exception> {
        public void run() throws E;
    }
    
    protected void withSync( ProcessorProbe probe, ProcessorContext context, Task task ) throws Exception {
        // XXX Exception?
        sync.beforeProbe( this, probe, context );
        try {
            task.run();
        }
        finally {
            sync.afterProbe( this, probe, context );
        }
    }
    
    @Override
    public void setTransactionRequest( TransactionRequest request, ProcessorContext context ) throws Exception {
        withSync( request, context, () -> 
                cache.setTransactionRequest( request, context ) );
    }

    @Override
    public void modifyFeaturesRequest( ModifyFeaturesRequest request, ProcessorContext context ) throws Exception {
        throw new UnsupportedOperationException( "Modifying a cached layer is not yet supported." );
//        withSync( request, context, () -> 
//                cache.modifyFeaturesRequest( request, context ) );
    }

    @Override
    public void removeFeaturesRequest( RemoveFeaturesRequest request, ProcessorContext context ) throws Exception {
        throw new UnsupportedOperationException( "Modifying a cached layer is not yet supported." );
//        withSync( request, context, () -> 
//                cache.removeFeaturesRequest( request, context ) );
    }

    @Override
    public void addFeaturesRequest( AddFeaturesRequest request, ProcessorContext context ) throws Exception {
        throw new UnsupportedOperationException( "Modifying a cached layer is not yet supported." );
//        withSync( request, context, () -> 
//                cache.addFeaturesRequest( request, context ) );
    }

    @Override
    public void getFeatureTypeRequest( GetFeatureTypeRequest request, ProcessorContext context ) throws Exception {
        withSync( request, context, () -> 
                cache.getFeatureTypeRequest( request, context ) );
    }

    @Override
    public void getFeatureSizeRequest( GetFeaturesSizeRequest request, ProcessorContext context ) throws Exception {
        withSync( request, context, () -> 
                cache.getFeatureSizeRequest( request, context ) );
    }

    @Override
    public void getFeatureBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        withSync( request, context, () -> 
                cache.getFeatureBoundsRequest( request, context ) );
    }

    @Override
    public void getFeatureRequest( GetFeaturesRequest request, ProcessorContext context ) throws Exception {
        withSync( request, context, () -> 
                cache.getFeatureRequest( request, context ) );
    }
    
}
