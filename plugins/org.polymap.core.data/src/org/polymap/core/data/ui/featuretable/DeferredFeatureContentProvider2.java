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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.viewers.IIndexableLazyContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.polymap.core.data.Messages;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheManager;

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
    
    /*
     * XXX This is used by BackgroundContentProvider as well for sorting; for equal
     * elements we have different order though
     */
    private Object[]                sortedElements;
    
    private Comparator              sortOrder;
    
    private Cache<String,SimpleFeature> elementCache = 
            CacheManager.instance().newCache( CacheConfig.DEFAULT );

    
    DeferredFeatureContentProvider2( FeatureTableViewer viewer,
            FeatureSource fs, Filter filter, Comparator sortOrder ) {
        this.viewer = viewer;
        this.fs = fs;
        this.filter = filter;
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
        
    }

    
    public void updateElement( int index ) {
        try {
            if (index < sortedElements.length) {
                Object elm = sortedElements[index];
                viewer.replace( elm, index );
            }
        }
        catch (Exception e) {
            log.warn( "", e );
        }
    }


    public void setSortOrder( Comparator sortOrder ) {
//        if (sortedElements != null) {
//            viewer.remove( sortedElements );
//        }
        sortedElements = new Object[0];
        this.sortOrder = sortOrder;
        UpdatorJob job = new UpdatorJob();
        job.schedule( 100 );
        viewer.refresh();
    }


    public int findElement( Object search ) {
        assert search != null;
        if (search instanceof IFeatureTableElement) {
            int c = 0;
            for (Object elm : sortedElements) {
                if (search.equals( elm )) {
                    return c;
                }
                ++c;
            }
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
            display = viewer.getTable().getDisplay();
//            viewer.getTable().setItemCount( 0 );
//            viewer.firePropChange( FeatureTableViewer.PROP_CONTENT_SIZE, null, c );
        }


        protected void addChunk( List chunk, IProgressMonitor monitor ) {
            log.debug( "adding chunk to table. size=" + chunk.size() );
            
            // sort chunk
            Collections.sort( chunk, sortOrder );
            
            // merge chunk and sortedElements into newArray
            Object[] newArray = new Object[ sortedElements.length + chunk.size() ];
            int readIndex = 0;
            int writeIndex = 0;
            for (Object elm : chunk) {
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
                        viewer.getTable().clearAll();
                        viewer.getTable().setItemCount( sortedElements.length );
                        viewer.firePropChange( FeatureTableViewer.PROP_CONTENT_SIZE, null, sortedElements.length );
                    }
                }
            });
        }

        
        @Override
        protected void runWithException( IProgressMonitor monitor )
        throws Exception {

            FeatureCollection coll = null;
            Iterator it = null;
            int c;

            try {
                if (fs == null) {
                    return;
                }
                coll = fs.getFeatures( filter );
                monitor.beginTask( getName(), coll.size() );
                if (viewer != null) { viewer.markTableLoading( true ); }

                it = coll.iterator();
                int chunkSize = 8;
                List chunk = new ArrayList( chunkSize ); 

                for (c=0; it.hasNext() && elementCache != null; c++) {
                    SimpleFeatureTableElement elm = new SimpleFeatureTableElement( (SimpleFeature)it.next(), fs, elementCache );
                    chunk.add( elm );
                    monitor.worked( 1 );

                    if (monitor.isCanceled() 
                            || Thread.interrupted()
                            // disposed?
                            || elementCache == null) {
                        monitor.setCanceled( true );
                        return;
                    }

                    if (chunk.size() >= chunkSize) {
                        addChunk( chunk, monitor );

                        chunkSize = Math.min( 2*chunkSize, 4096 );
                        chunk = new ArrayList( chunkSize );

                        // let the UI thread update the table so that the user sees
                        // first results quickly
                        Thread.sleep( 100 );
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
            // NPE when disposed and variables are null; dont show to user
            catch (NullPointerException e) {
                log.warn( "", e );
            }
            catch (Exception e) {
                throw e;
            }
            finally {
                monitor.done();
                if (coll != null) { coll.close( it ); }
                if (viewer != null) { viewer.markTableLoading( false ); }
            }
        }
    };

}
