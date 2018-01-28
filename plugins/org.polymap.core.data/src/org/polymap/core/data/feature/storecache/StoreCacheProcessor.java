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
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
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

    private static Lazy<DataAccess>     cachestore;
    
    /**
     * Initialize the global cache store.
     *
     * @param cachestoreSupplier
     */
    public static void init( Supplier<DataAccess> cachestoreSupplier ) {
        cachestore = new LockedLazyInit( cachestoreSupplier );
    }
    
//    protected static RDataStore initBuffers() {
//        try {
//            File dataDir = CorePlugin.getDataLocation( DataPlugin.getDefault() );
//            LuceneRecordStore store = new LuceneRecordStore( new File( dataDir, "feature-buffer" ), false );
//            return new RDataStore( store, new LuceneQueryDialect() );
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }
//    
//    
//    protected static FeatureSource cacheFor( PipelineProcessorSite site ) throws IOException {
//        DataAccess ds = (DataAccess)site.dsd.get().service.get();
//        String resName = ds.getInfo().getSource() + ":" + site.dsd.get().resourceName.get();
//        Name name = new NameImpl( resName );
//
//        if (!cachestore.get().getNames().contains( name )) {
//            // XXX check concurrent create
//            
//        }
//    }
    
    // instance *******************************************
    
    private PipelineProcessorSite       site;

    private Lazy<DataSourceProcessor>   cache = new LockedLazyInit( () -> initCache() );

    
    protected DataSourceProcessor initCache() {
        throw new RuntimeException( "" );
        
//        PipelineProcessorSite bufferSite = new PipelineProcessorSite( null );
//        bufferSite.usecase.set( new ProcessorSignature( FeaturesProducer.class ) );
//        bufferSite.builder.set( site.builder.get() );
//        bufferSite.dsd.set( bufferDsd );
//        
//        cache = new DataSourceProcessor();
//        cache.init( site );        
    }

    @Override
    public void init( @SuppressWarnings( "hiding" ) PipelineProcessorSite site ) throws Exception {
        this.site = site;
    }

    @Override
    public void setTransactionRequest( TransactionRequest request, ProcessorContext context ) throws Exception {
        cache.get().setTransactionRequest( request, context );
    }

    @Override
    public void modifyFeaturesRequest( ModifyFeaturesRequest request, ProcessorContext context ) throws Exception {
        cache.get().modifyFeaturesRequest( request, context );
    }

    @Override
    public void removeFeaturesRequest( RemoveFeaturesRequest request, ProcessorContext context ) throws Exception {
        cache.get().removeFeaturesRequest( request, context );
    }

    @Override
    public void addFeaturesRequest( AddFeaturesRequest request, ProcessorContext context ) throws Exception {
        cache.get().addFeaturesRequest( request, context );
    }

    @Override
    public void getFeatureTypeRequest( GetFeatureTypeRequest request, ProcessorContext context ) throws Exception {
        cache.get().getFeatureTypeRequest( request, context );
    }

    @Override
    public void getFeatureSizeRequest( GetFeaturesSizeRequest request, ProcessorContext context ) throws Exception {
        cache.get().getFeatureSizeRequest( request, context );
    }

    @Override
    public void getFeatureBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        cache.get().getFeatureBoundsRequest( request, context );
    }

    @Override
    public void getFeatureRequest( GetFeaturesRequest request, ProcessorContext context ) throws Exception {
        cache.get().getFeatureRequest( request, context );
    }
    
}
