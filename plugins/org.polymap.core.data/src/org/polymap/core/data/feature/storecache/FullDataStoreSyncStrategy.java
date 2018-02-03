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

import java.io.IOException;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorProbe;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;

/**
 * Periodicly copy the entire contents of the upstream/backend {@link DataStore} into
 * the cache. No real sync. Bypasses upstream processors.
 *
 * @author Falko Bräutigam
 */
public class FullDataStoreSyncStrategy
        extends SyncStrategy {

    private static final Log log = LogFactory.getLog( FullDataStoreSyncStrategy.class );

    /** The original (upstream) service found in the processor site. */
    private DataAccess          ds;

    private Name                resName;
    
    /** The original (upstream) {@link FeatureSource} found in the processor site. */
    private FeatureSource       fs;

    private DataAccess          cacheDs;
    
    private SyncJob             syncJob = new SyncJob();

    
    @Override
    public void beforeInit( StoreCacheProcessor proc, PipelineProcessorSite site ) throws Exception {
        this.ds = (DataStore)site.dsd.get().service.get();
        this.resName = new NameImpl( site.dsd.get().resourceName.get() );
        this.fs = ds.getFeatureSource( resName );
        this.cacheDs = proc.cacheDataStore();
        
        // check/init schema
        if (!cacheDs.getNames().contains( resName )) {
            cacheDs.createSchema( fs.getSchema() );
        }
        else {
            checkUpdateSchema();
        }        
        // schedule sync
        assert syncJob.getState() == Job.NONE;
        proc.ifUpdateNeeded( () -> syncJob.schedule() );
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
        if (schema.getDescriptors().equals( cacheSchema.getDescriptors() )) {
            needsUpdate = true;
        }
        
        if (needsUpdate) {
            throw new RuntimeException( "Not implemented: schema update" );
        }
    }

    @Override
    public void beforeProbe( StoreCacheProcessor proc, ProcessorProbe probe, ProcessorContext context ) throws Exception {
        proc.ifUpdateNeeded( () -> syncJob.schedule() );
        syncJob.join();
    }


    /**
     * 
     */
    class SyncJob
            extends UIJob {

        public SyncJob() {
            super( "Cache synchronization", false );
            assert getJobManager().isIdle();
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            log.info( "Start cache update..." );
            FeatureStore cacheFs = (FeatureStore)cacheDs.getFeatureSource( resName );
            
            // fill cache
            Transaction tx = new DefaultTransaction();
            try {
                Timer timer = new Timer();
                cacheFs.setTransaction( tx );
                cacheFs.removeFeatures( Filter.INCLUDE );
                log.info( "Cache cleared: " + timer.elapsedTime() + "ms" ); timer.start();
                cacheFs.addFeatures( fs.getFeatures() );
                log.info( "Cache filled: " + timer.elapsedTime() + "ms" ); timer.start();
                tx.commit();
                log.info( "Cache committed: " + timer.elapsedTime() + "ms" );
            }
            catch (Exception e) {
                tx.rollback();
            }
            finally {
                tx.close();
            }
        }
    }
    
}
