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

import java.util.function.Supplier;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    public static final Param<Class>    SYNC_TYPE = new Param( "syncType", Class.class );

    private static Lazy<DataAccess>     cachestore;
    
    /**
     * Initialize the global cache store.
     */
    public static void init( Supplier<DataAccess> cacheStoreSupplier ) {
        assert cachestore == null : "cachestore is set already.";
        cachestore = new LockedLazyInit( cacheStoreSupplier );
    }
    
    
    // instance *******************************************
    
    private PipelineProcessorSite       site;

    private DataAccess                  cacheDs;

    private String                      resName;

    private DataSourceProcessor         cache;
    
    private SyncStrategy                sync;

    
    @Override
    public void init( @SuppressWarnings( "hiding" ) PipelineProcessorSite site ) throws Exception {
        this.site = site;

        // XXX shouldn't the ressource name come from upstream schema?
        assert cachestore != null : "cachestore is not yet initialized.";
        cacheDs = cachestore.get();
        resName = site.dsd.get().resourceName.get();
        
        // init sync strategy
        sync = (SyncStrategy)SYNC_TYPE.get( site ).newInstance();
        sync.beforeInit( this, site );

        DataSourceDescriptor cacheDsd = new DataSourceDescriptor( cacheDs, resName );
        PipelineProcessorSite cacheSite = new PipelineProcessorSite( Params.EMPTY );
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
        withSync( request, context, () -> 
                cache.modifyFeaturesRequest( request, context ) );
    }

    @Override
    public void removeFeaturesRequest( RemoveFeaturesRequest request, ProcessorContext context ) throws Exception {
        withSync( request, context, () -> 
                cache.removeFeaturesRequest( request, context ) );
    }

    @Override
    public void addFeaturesRequest( AddFeaturesRequest request, ProcessorContext context ) throws Exception {
        withSync( request, context, () -> 
                cache.addFeaturesRequest( request, context ) );
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
