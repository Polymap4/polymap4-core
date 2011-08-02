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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.graphics.Graphics;

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
        //implements IIndexableLazyContentProvider {

    private static Log log = LogFactory.getLog( DeferredFeatureContentProvider.class );
    
    private static final Color  LOADING_FOREGROUND = Graphics.getColor( 0xa0, 0xa0, 0xa0 );
    
    private FeatureTableViewer  viewer;
    
    private FeatureSource       fs;
    
    private Filter              filter;
    
    private Color               tableForeground;
    
    private Map<String,Integer> fidIndex = new HashMap();
   
    
    DeferredFeatureContentProvider( FeatureTableViewer viewer,
            FeatureSource fs, Filter filter, Comparator sortOrder ) {
        super( sortOrder );
        this.viewer = viewer;
        this.fs = fs;
        this.filter = filter;
        this.tableForeground = this.viewer.getTable().getForeground();
    }


    @SuppressWarnings("hiding")
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        super.inputChanged( viewer, null, new Model() );
    }

    
    protected void markTableLoading( final boolean loading ) {
        Display display = viewer.getTable().getDisplay();
        display.asyncExec( new Runnable() {
            public void run() {
                if (viewer.getTable().isDisposed()) {
                    return;
                }
                if (loading) {
                    viewer.getTable().setForeground( LOADING_FOREGROUND );
                }
                else {
                    viewer.getTable().setForeground( tableForeground );
                }
            }
        });
    }
    
    
    public int findElement( Object element ) {
        if (element instanceof IFeatureTableElement) {
            String fid = ((IFeatureTableElement)element).fid();
            Integer result = fidIndex.get( fid );
            return result != null ? result : -1;
        }
        return -1;
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
            
            if (viewer.getTable().isDisposed()) {
                return;
            }
            fidIndex.clear();
            listener.setContents( ArrayUtils.EMPTY_OBJECT_ARRAY );
            
            job = new Job( Messages.get( "FeatureTableFetcher_name" ) ) {
                protected IStatus run( IProgressMonitor monitor ) {
                    
                    FeatureCollection coll = null;
                    Iterator it = null;
                    int c;
                    
                    try {
                        coll = fs.getFeatures( filter );
                        monitor.beginTask( getName(), coll.size() );

                        it = coll.iterator();
                        List chunk = new ArrayList( 256 );
                        int chunkSize = 8;
                        
                        for (c=0; it.hasNext(); c++) {
                            SimpleFeatureTableElement elm = new SimpleFeatureTableElement( (SimpleFeature)it.next(), fs );
                            chunk.add( elm );
                            fidIndex.put( elm.fid(), c );
                            monitor.worked( 1 );

                            if (monitor.isCanceled() || Thread.interrupted()) {
                                return Status.CANCEL_STATUS;
                            }
                            
                            if (chunk.size() >= chunkSize) {
                                chunkSize *= 2;
                                log.debug( "adding chunk to table. size=" + chunk.size() );
                                listener.add( chunk.toArray() );
                                chunk.clear();
                                
                                viewer.firePropChange( FeatureTableViewer.PROP_CONTENT_SIZE, null, c );

                                markTableLoading( true );
                                
                                // let the UI thread update the table so that the user sees
                                // first results quickly
                                Thread.sleep( 100 );
                            }
                        }
                        listener.add( chunk.toArray() );
                        viewer.firePropChange( FeatureTableViewer.PROP_CONTENT_SIZE, null, c );
                        markTableLoading( false );
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
