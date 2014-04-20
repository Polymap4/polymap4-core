/* 
 * polymap.org
 * Copyright (C) 2011-2013, Polymap GmbH. All rights reserved.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.lifecycle.UICallBack;

import org.eclipse.jface.viewers.IIndexableLazyContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.data.Messages;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheManager;
import org.polymap.core.ui.SelectionAdapter;

/**
 * Feature content provider that performs sorting and filtering in a background Job.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DeferredFeatureContentProvider2
        implements IDeferredFeatureContentProvider, IIndexableLazyContentProvider {

    private static Log log = LogFactory.getLog( DeferredFeatureContentProvider2.class );
    
    private FeatureTableViewer      viewer;
    
    private FeatureSource           fs;
    
    private Filter                  filter;
    
    private Set<ViewerFilter>       vfilters;
    
    /*
     * XXX This is used by BackgroundContentProvider as well for sorting; for equal
     * elements we have different order though
     */
    private Object[]                sortedElements;
    
    private Comparator              sortOrder;
    
    private Cache<String,Feature>   elementCache = 
            CacheManager.instance().newCache( CacheConfig.DEFAULT );

    private volatile UpdatorJob     updator;

    
    DeferredFeatureContentProvider2( FeatureTableViewer viewer,
            FeatureSource fs, Filter filter, Comparator sortOrder, ViewerFilter[] viewerFilters ) {
        this.viewer = viewer;
        this.fs = fs;
        this.filter = filter;
        this.vfilters = Sets.newHashSet( viewerFilters );
        setSortOrder( sortOrder );
    }


    public void dispose() {
        sortedElements = null;
        viewer = null;
        fs = null;
        elementCache.dispose();
        elementCache = null;
    }


    @SuppressWarnings("hiding")
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        if (elementCache != null) {
            elementCache.clear();
        }
    }

    
    @Override
    public void updateElement( int index ) {
        try {
            if (sortedElements != null && index < sortedElements.length) {
                Object elm = sortedElements[index];
                viewer.replace( elm, index );
            }
            else {
                viewer.replace( FeatureTableViewer.LOADING_ELEMENT, 0 );
            }
        }
        catch (Exception e) {
            log.warn( "", e );
        }
    }


    public synchronized void setSortOrder( Comparator sortOrder ) {
        while (updator != null) {
            updator.cancel();
            try {
                log.info( "Waitung for updator to cancel..." );
                Thread.sleep( 100 );
            }
            catch (InterruptedException e) {
            }
        }
        
        final ISelection selection = viewer.getSelection();

        // display LOADING_ELEMENT
        viewer.getTable().clearAll();
        viewer.getTable().setItemCount( 1 );
        viewer.replace( FeatureTableViewer.LOADING_ELEMENT, 0 );

        this.sortOrder = sortOrder;
        
        // XXX actually start Updator (and access data) just when needed by UI!?
        // currently, subsequent refresh() may cause a lot of CPU as they always
        // start the updator
        updator = new UpdatorJob();
        updator.addJobChangeListener( new JobChangeAdapter() {
            public void done( IJobChangeEvent ev ) {
                updator = null;

                // preserve selection
                if (selection != null && !selection.isEmpty()) {
                    viewer.getControl().getDisplay().asyncExec( new Runnable() {
                        public void run() {
                            IFeatureTableElement elm = SelectionAdapter.on( selection ).first( IFeatureTableElement.class );
                            viewer.selectElement( elm.fid(), true );
                        }
                    });
                }
            }
        });
        // wait for subsequent sort/refresh request
        updator.schedule( 100 );
        
        viewer.refresh( true );
    }


    public Comparator getSortOrder() {
        return sortOrder;
    }


    public void addViewerFilter( ViewerFilter vfilter ) {
        if (vfilters.add( vfilter )) {
            setSortOrder( sortOrder );
        }
    }

    public void removeViewerFilter( ViewerFilter vfilter ) {
        if (vfilters.remove( vfilter )) {
            setSortOrder( sortOrder );
        }
    }


    @Override
    public int findElement( Object search ) {
        assert search != null;
        
//        // wait for possible running updator
//        // XXX this polling causes race cond with updator on sortedElements
        Object[] currentElms = sortedElements;
//        while (updator != null && currentElms != null) {
//            try { Thread.sleep( 100 ); } catch (InterruptedException e) {}
//            currentElms = sortedElements;
//        }
        return doFindElement( currentElms, search );
    }


    protected int doFindElement( Object[] elms, Object search ) {
        assert search != null;
        if (search instanceof IFeatureTableElement && elms != null) {
            return Iterables.indexOf( Arrays.asList( elms ), Predicates.equalTo( search ) );
        }
        return -1;
    }


    /*
     * 
     */
    class UpdatorJob
            extends UIJob {

        private Display         display;
        
        
        UpdatorJob() {
            super( Messages.get( "FeatureTableFetcher_name" ) );
//            setPriority( Job.LONG );
            display = viewer.getTable().getDisplay();
//            viewer.getTable().setItemCount( 0 );
//            viewer.firePropChange( FeatureTableViewer.PROP_CONTENT_SIZE, null, c );
//            UICallBack.activate( UpdatorJob.class.getName() );
        }


        protected void addChunk( List chunk, IProgressMonitor monitor ) {
            log.debug( "adding chunk to table. size=" + chunk.size() );
            
            // filter chunk
            final List filtered = new ArrayList( chunk.size() );
            outer: for (Object elm : chunk) {
                for (ViewerFilter vfilter : vfilters) {
                    if (!vfilter.select( viewer, null, elm )) {
                        continue outer;
                    }
                }
                filtered.add( elm );
            }
            
            // sort: stable, supporting equal elements
            Collections.sort( filtered, sortOrder );
            
            // merge chunk and sortedElements into newArray
            Object[] newArray = new Object[ sortedElements.length + filtered.size() ];
            int readIndex = 0;
            int writeIndex = 0;
            for (Object elm : filtered) {
                while (readIndex < sortedElements.length) {
                    Object sortedElm = sortedElements[ readIndex ];
                    if (sortOrder.compare( sortedElm, elm ) <= 0) {
                        newArray[ writeIndex++ ] = sortedElm;
                        readIndex++;
                    }
                    else {
                        break;
                    }
                    if (monitor.isCanceled()) {
                        return;
                    }
                }
                newArray[ writeIndex++ ] = elm;
            }
            System.arraycopy( sortedElements, readIndex, newArray, writeIndex, sortedElements.length-readIndex );
            sortedElements = newArray;

            // update UI
            display.asyncExec( new Runnable() {
                public void run() {
                    // disposed?
                    if (viewer != null && viewer.getTable() != null) {
                        viewer.getTable().setItemCount( sortedElements.length );
                        viewer.getTable().clearAll();
//                        viewer.getTable().select( 0 );
//                        viewer.refresh( false );
                        viewer.firePropChange( FeatureTableViewer.PROP_CONTENT_SIZE, null, sortedElements.length );                        
                    }
                }
            });
        }

        
        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            sortedElements = new Object[0];

            FeatureCollection coll = null;
            Iterator it = null;

            try {
                if (fs == null) {
                    return;
                }
                coll = fs.getFeatures( filter );
                monitor.beginTask( getName(), coll.size() );
                if (viewer != null) { 
                    viewer.markTableLoading( true ); 
                }

                it = coll.iterator();
                int chunkSize = 16;
                List chunk = new ArrayList( chunkSize ); 

                for (int c=0; it.hasNext() && elementCache != null; c++) {
                    SimpleFeatureTableElement elm = new SimpleFeatureTableElement( (Feature)it.next(), fs, elementCache );
                    chunk.add( elm );

                    // check canceled or disposed
                    if (monitor.isCanceled() || Thread.interrupted() || elementCache == null) {
                        break;
                    }

                    if (chunk.size() >= chunkSize) {
                        addChunk( chunk, monitor );
                        monitor.worked( chunk.size() );

                        chunkSize = Math.min( 4*chunkSize, 4096 );
                        chunk = new ArrayList( chunkSize );

                        // let the UI thread update the table so that the user sees
                        // first results quickly
                        //log.info( "sleeping: chunkSize=" + chunkSize );
                        //Thread.sleep( 50 );
                    }
                }
                // disposed?
                if (elementCache != null) {
                    addChunk( chunk, monitor );
                }
                else {
                    monitor.setCanceled( true );
                }
            }
            // NPE when disposed and variables are null; don't show to user
            catch (NullPointerException e) {
                log.warn( "", e );
            }
            catch (Exception e) {
                throw e;
            }
            finally {                
                monitor.done();
                if (viewer != null) {
                    viewer.markTableLoading( false ); 
                }
                if (coll != null) { coll.close( it ); }
                
                display.asyncExec( new Runnable() {
                    public void run() {
                        UICallBack.deactivate( UpdatorJob.class.getName() );
//                        // XXX Fix the "empty" table problem
                        if (sortedElements.length > 0) {
                            viewer.reveal( sortedElements[sortedElements.length-1] );
                            viewer.reveal( sortedElements[0] );
                        }
//                        
                        // XXX only necessary if just one element
                        viewer.refresh( true );
                    }
                });
            }
        }
    };

}
