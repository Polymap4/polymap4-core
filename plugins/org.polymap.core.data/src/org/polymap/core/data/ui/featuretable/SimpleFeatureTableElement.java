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
package org.polymap.core.data.ui.featuretable;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.DataPlugin;

/**
 * Default implementation for {@link SimpleFeature} features.
 * <p/>
 * The {@link #feature} is referenced by a weak/soft reference, so the GC
 * may reclaim the memory. The feature is then re-fetched from the underlying
 * FeatureSource in an {@link Job}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SimpleFeatureTableElement
        implements IFeatureTableElement {

    private static Log log = LogFactory.getLog( SimpleFeatureTableElement.class );

    private static final FilterFactory  ff = CommonFactoryFinder.getFilterFactory( null );
    
    private Reference<SimpleFeature>    ref;
    
    private String                      fid;
    
    private FeatureSource               fs;
    
    
    public SimpleFeatureTableElement( SimpleFeature feature, FeatureSource fs ) {
        super();
        this.ref = newReference( feature );
        this.fs = fs;
        this.fid = feature.getID();
    }


    public String fid() {
        return fid;
    }


    public Object getValue( String name ) {
        return feature().getAttribute( name );
    }
    
    
    public SimpleFeature feature() {
        SimpleFeature result = ref.get();
        
        if (result == null) {
        
            synchronized (this) {
                while (result == null) {
                    FetchJob fetcher = new FetchJob();
                    fetcher.schedule();

                    try {
                        // XXX this may block forever; use PlatformJobs!?
                        fetcher.join();
                        result = (SimpleFeature)fetcher.result;
                        ref = newReference( result );
                    }
                    catch (InterruptedException e) {
                        log.warn( "", e );
                    }
                }
            }
        }
        return result;
    }

    
    protected <T> Reference<T> newReference( T feature ) {
        return new SoftReference( feature );
    }


    /*
     * 
     */
    class FetchJob
            extends Job {

        Feature             result;
        
        
        public FetchJob() {
            super( "Fetching " + fid + "..." );
        }

        protected IStatus run( IProgressMonitor monitor ) {
            try {
                log.info( "fetching " + fid + "..." );
                Id filter = ff.id( Collections.singleton( ff.featureId( fid ) ) );
                fs.getFeatures( filter ).accepts( new FeatureVisitor() {
                    public void visit( Feature feature ) {
                        result = feature;
                    }
                }, null );
                return Status.OK_STATUS;
            }
            catch (IOException e) {
                return new Status( Status.ERROR, DataPlugin.PLUGIN_ID, "", e );
            }
        }
        
    }
    
}
