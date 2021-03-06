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

import java.util.concurrent.atomic.AtomicBoolean;

import java.io.IOException;
import java.time.Duration;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorProbe;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.ui.StatusDispatcher;

/**
 * Periodicly copy the entire contents of the upstream/backend {@link DataStore} into
 * the cache. No real sync. Bypasses upstream processors.
 *
 * @author Falko Br�utigam
 */
public class FullDataStoreSyncStrategy
        extends SyncStrategy {

    private static final Log log = LogFactory.getLog( FullDataStoreSyncStrategy.class );

    /** True, if a {@link SyncJob} is running currently. */
    private static AtomicBoolean    running = new AtomicBoolean();

    /** The original (upstream) service found in the processor site. */
    private DataAccess          ds;

    private Name                resName;
    
    /** The original (upstream) {@link FeatureSource} found in the processor site. */
    private FeatureSource       fs;

    private DataAccess          cacheDs;
    
    private SyncJob             syncJob = new SyncJob();

    
    @Override
    public void beforeInit( StoreCacheProcessor proc, PipelineProcessorSite site ) throws Exception {
        this.ds = (DataAccess)site.dsd.get().service.get();
        this.resName = new NameImpl( site.dsd.get().resourceName.get() );
        this.fs = ds.getFeatureSource( resName );
        this.cacheDs = proc.cacheDataStore();
        
        // schedule sync
        assert syncJob.getState() == Job.NONE;
        proc.ifUpdateNeeded( () -> {
            if (running.compareAndSet( false, true )) {
                try {
                    syncJob.schedule();
                    // waiting here is important as several processors may work on the same
                    // layer and we need a relyable status after init;
                    // moreover, it seems that processor init() is called several times from
                    // different instances (!) when a pipeline is created; if we would not
                    // wait here then one syncJob starts, ifUpdateNeeded is updated and the next
                    // instances assumes that cache is up-to-date and shows maybe empty layer
                    if (Display.getCurrent() == null) {
                        // FIXME limit wait time in order to prevent hang
                        syncJob.join();
                    }
                    else {
                        if (!syncJob.joinAndDispatch( Duration.ofSeconds( 30 ).toMillis() )) {
                            StatusDispatcher.handleError( 
                                    "The cache of resource '" + resName + "'"
                                            + "could not be updated within 30s. The contents"
                                            + "might be out of sync.", null );
                        }
                    }
                    return true;
                }
                finally {
                    running.set( false );
                }
            }
            else {
                log.warn( "Update needed but other Job is running!" );
                return false;
            }
        });
    }

    
    @Override
    public void beforeProbe( StoreCacheProcessor proc, ProcessorProbe probe, ProcessorContext context ) throws Exception {
        proc.ifUpdateNeeded( () -> {
            if (running.compareAndSet( false, true )) {
                try {
                    syncJob.schedule();
                    // XXX unfortunately remove/re-create schema and fetching features is not
                    // an atomic operation (due to stupid geotools DataStore API)
                    // FIXME limit wait time in order to prevent hang
                    syncJob.join();
                    return true;
                }
                finally {
                    running.set( false );
                }
            }
            else {
                log.warn( "Update needed but other Job is running!" );
                return false;
            }
        });
    }


    /**
     * 
     */
    protected class SyncJob
            extends UIJob {
        
        public SyncJob() {
            super( "Cache update", false );
            setPriority( Job.BUILD );
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            monitor.beginTask( getName(), 4 );
            log.debug( "Start cache update..." );

            // XXX remove/re-create schema and fetching features is not an
            // atomic operation (due to stupid geotools DataStore Transaction API)
            
            // re-create schema
            monitor.subTask( "Re-creating schema" );
            FeatureType schema = fs.getSchema();
            if (cacheDs.getNames().contains( schema.getName() )) {
                log.debug( "Removing schema: " + schema.getName() );
                cacheDs.removeSchema( schema.getName() );
            }
            log.debug( "Creating cache schema: " + schema.getName() );
            cacheDs.createSchema( schema );
            monitor.worked( 1 );
                        
            // fill cache
//            Transaction tx = new DefaultTransaction();
            try {
                Timer timer = new Timer();                
                FeatureStore cacheFs = (FeatureStore)cacheDs.getFeatureSource( resName );
//                cacheFs.setTransaction( tx );
                
                monitor.subTask( "Fetching" );
                cacheFs.addFeatures( fs.getFeatures() );
                log.debug( "Cache filled: " + timer.elapsedTime() + "ms" ); timer.start();
                monitor.worked( 1 );
                
                monitor.subTask( "Committing" );
//                tx.commit();
                log.info( "Cache updated. (" + timer.elapsedTime() + "ms)" );
            }
            catch (Exception e) {
                //tx.rollback();
                log.warn( "Cache not committed. (No rollback)", e );
            }
            finally {
//                tx.close();
                monitor.done();
            }
        }

        
        protected void checkUpdateSchema() throws IOException {
            FeatureStore cacheFs = (FeatureStore)cacheDs.getFeatureSource( resName );
            FeatureType cacheSchema = cacheFs.getSchema();
            FeatureType schema = fs.getSchema();
            
            // simple check as equals() does not work for us here
            boolean needsUpdate = false;
            if (!schema.getName().equals( cacheSchema.getName() )) {
                needsUpdate = true;
            }
            else {
                for (PropertyDescriptor prop : schema.getDescriptors()) {
                    PropertyDescriptor cacheProp = cacheSchema.getDescriptor( prop.getName() );
                    if (cacheProp == null || !prop.getType().getBinding().equals( cacheProp.getType().getBinding() )) {
                        needsUpdate = true;
                        break;
                    }
                }
            }        
            if (needsUpdate) {
                log.info( "Schema has changed. Re-creating: " + cacheSchema.getName() );
                cacheDs.removeSchema( cacheSchema.getName() );
                cacheDs.createSchema( schema );
            }
        }

    }
    
}
