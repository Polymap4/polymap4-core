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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.deferred.AbstractConcurrentModel;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.IConcurrentModelListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;

/**
 * Feature content provider that performs sorting and filtering in a background
 * thread based on {@link DeferredContentProvider}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DeferredFeatureContentProvider
        extends DeferredContentProvider {

    private static Log log = LogFactory.getLog( DeferredFeatureContentProvider.class );
    
    private FeatureTableViewer  viewer;
    
    private FeatureSource       fs;
    
    private Filter              filter;
   
    private String              sortPropName;
    
    
    DeferredFeatureContentProvider( FeatureTableViewer viewer,
            FeatureSource fs, Filter filter, String sortPropName ) {
        super( newComparator( sortPropName ) );
        this.viewer = viewer;
        this.fs = fs;
        this.filter = filter;
        this.sortPropName = sortPropName;
    }


    private static Comparator newComparator( final String sortPropName ) {
        return new Comparator<IFeatureTableElement>() {
            public int compare( IFeatureTableElement elm1, IFeatureTableElement elm2 ) {
                String value1 = (String)elm1.getValue( sortPropName );
                String value2 = (String)elm2.getValue( sortPropName );
                return value1.compareTo( value2 );
            }
        };
    }

    
    @SuppressWarnings("hiding")
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        super.inputChanged( viewer, null, new Model() );
    }


    /*
     * 
     */
    class Model
            extends AbstractConcurrentModel {
        
        private Job         job;
        
        public synchronized void requestUpdate( final IConcurrentModelListener listener ) {
            if (job != null) {
                log.info( "Fetcher job found, cancel and waiting..." );
                job.cancel();
                try {
                    job.join();
                }
                catch (InterruptedException e) {
                }
                log.info( "Fetcher job done." );
            }
            
            listener.setContents( ArrayUtils.EMPTY_OBJECT_ARRAY );
            
            job = new Job( Messages.get( "FeatureTableFetcher_name" ) ) {
                protected IStatus run( IProgressMonitor monitor ) {
                    
                    FeatureCollection coll = null;
                    Iterator it = null;
                    int c;
                    
                    try {
                        // XXX add SortBy to the query!?
                        coll = fs.getFeatures( filter );
                        monitor.beginTask( getName(), coll.size() );

                        it = coll.iterator();
                        List chunk = new ArrayList( 256 );
                        int chunkSize = 2;
                        
                        for (c=0; it.hasNext(); c++) {
                            chunk.add( new SimpleFeatureTableElement( (SimpleFeature)it.next(), fs ) );
                            monitor.worked( 1 );

                            if (monitor.isCanceled() || Thread.interrupted()) {
                                return Status.CANCEL_STATUS;
                            }
                            
                            if (chunk.size() >= chunkSize) {
                                Thread.sleep( 1000 );
                                chunkSize *= 2;
                                log.info( "adding chunk to table. size=" + chunk.size() );
                                listener.add( chunk.toArray() );
                                chunk.clear();
                                viewer.firePropChange( FeatureTableViewer.PROP_CONTENT_SIZE, null, c );
                            }
                        }
                        listener.add( chunk.toArray() );
                        viewer.firePropChange( FeatureTableViewer.PROP_CONTENT_SIZE, null, c );
                        return Status.OK_STATUS;
                    }
                    catch (Exception e) {
                        log.warn( "", e );
                        return new Status( IStatus.ERROR, DataPlugin.PLUGIN_ID, "", e );
                    }
                    finally {
                        coll.close( it );
                        monitor.done();
                    }
                }
            };
            job.schedule();
        }
    };

}
