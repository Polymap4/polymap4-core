/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.catalog.ui;

import static org.polymap.core.runtime.UIThreadExecutor.logErrorMsg;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IMetadataCatalog;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.runtime.StreamIterable;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.DefaultString;
import org.polymap.core.runtime.config.Mandatory;

/**
 * Content provider for {@link IMetadataCatalog} (or Collection or Array thereof) as input,
 * {@link IMetadata}, {@link IServiceInfo} and {@link IResourceInfo} objects.
 * <p/>
 * XXX check databinding ObservableListContentProvider
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MetadataContentProvider
        extends Configurable
        implements /*ITreeContentProvider,*/ ILazyTreeContentProvider {

    private static final Log log = LogFactory.getLog( MetadataContentProvider.class );
    
    public static final Object          LOADING = new Object();
    
    public static final Object[]        CACHE_LOADING = {LOADING};
    
    public static final Object          NO_CHILDREN = new Object();
    
    public static final Object[]        CACHE_NO_CHILDREN = {NO_CHILDREN};
    
    @Mandatory
    @DefaultString( IMetadataCatalog.ALL_QUERY )
    public Config<String>               catalogQuery;
    
    /** Max results of {@link IMetadataCatalog#query(String, IProgressMonitor)}. */
    @Mandatory
    @DefaultInt( 25 )
    public Config<Integer>              maxResults;
    
    private TreeViewer                  viewer;
    
    private Object                      input;

    private IMetadataResourceResolver   resolver;
    
    /* XXX make this evictable? */
    private ConcurrentMap<Object,Object[]> cache = new ConcurrentHashMap( 32 );

    
    public MetadataContentProvider( IMetadataResourceResolver resolver ) {
        this.resolver = resolver;
    }

    
    @Override
    public void dispose() {
    }

    
    @Override
    public void inputChanged( @SuppressWarnings("hiding") Viewer viewer, Object oldInput, Object newInput ) {
        this.viewer = (TreeViewer)viewer;
        this.input = newInput;
        flush();
    }

    /**
     * Flush internal cache.
     */
    public void flush() {
        cache.clear();
    }

    
    // ILazyTreeContentProvider ***************************

    @Override
    public void updateChildCount( Object elm, int currentChildCount ) {
        // check cache
        if (elm == LOADING) {
            return;
        }
        Object[] cached = cache.get( elm );
        if (cached != null && 
                (cached.length == currentChildCount || cached == CACHE_LOADING)) {
            return;    
        }
        
        // Collection of IMetadataCatalog
        if (elm instanceof Collection) {
            Object[] children = ((Collection)elm).toArray();
            cache.put( elm, children );
            viewer.setChildCount( elm, children.length );
        }
        // Array of IMetadataCatalog
        else if (elm instanceof IMetadataCatalog[]) {
            cache.put( elm, (IMetadataCatalog[])elm );
            viewer.setChildCount( elm, ((IMetadataCatalog[])elm).length );
        }
        // IMetadataCatalog -> query
        else if (elm instanceof IMetadataCatalog) {
            updateMetadataCatalog( (IMetadataCatalog)elm, currentChildCount );
        }
        // IMetadata -> resolve
        else if (elm instanceof IMetadata) {
            updateMetadata( (IMetadata)elm, currentChildCount );
        }
        // IServiceInfo
        else if (elm instanceof IServiceInfo) {
            updateService( (IServiceInfo)elm, currentChildCount );
        }
        // IResourceInfo
        else if (elm instanceof IResourceInfo) {
            if (currentChildCount != 0) {
                viewer.setChildCount( elm, 0 );
            }
        }
        else {
            throw new RuntimeException( "Unknown element type: " + elm );
        }
    }


    /**
     *
     * @param elm
     * @param currentChildCount
     */
    protected void updateMetadataCatalog( IMetadataCatalog elm, int currentChildCount ) {
        updateChildrenLoading( elm );
        UIJob job = new UIJob( "Query catalog" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                try {
                    Object[] children = ((IMetadataCatalog)elm)
                            .query( catalogQuery.get(), monitor )
                            .maxResults.put( maxResults.get() )
                            .execute().stream().toArray();
                    updateChildren( elm, children, currentChildCount );
                }
                catch (Exception e) {
                    log.warn( e );
                }
            }
        };
        job.scheduleWithUIUpdate();
    }


    /**
     *
     * @param elm
     * @param currentChildCount
     */
    protected void updateMetadata( IMetadata elm, int currentChildCount ) {
        updateChildrenLoading( elm );
        UIJob job = new UIJob( "Resolve service" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                IMetadata metadata = (IMetadata)elm;
                if (resolver.canResolve( metadata )) {
                    try {
                        IServiceInfo service = (IServiceInfo)resolver.resolve( metadata, monitor );
                        // skip service as it has not more info than metadata record
                        IResourceInfo[] children = Iterables.toArray( service.getResources( monitor ), IResourceInfo.class );
                        updateChildren( elm, children, currentChildCount );
                    }
                    catch (Exception e) {
                        // FIXME handle exceptions
                        log.warn( "", e );
                        throw new RuntimeException( e );
                    }
                }
                else {
                    updateChildren( elm, new Object[] {"Unable to resolve the service."}, currentChildCount );                        
                }
            }
        };
        job.scheduleWithUIUpdate();
    }


    /**
     *
     * @param elm
     * @param currentChildCount
     */
    protected void updateService( IServiceInfo elm, int currentChildCount ) {
        updateChildrenLoading( elm );
        UIJob job = new UIJob( "Find resources" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                Object[] children = StreamIterable.of( ((IServiceInfo)elm).getResources( monitor ) ).stream().toArray();
                updateChildren( elm, children, currentChildCount );
            }
        };
        job.scheduleWithUIUpdate();
    }


    /**
     * Updates the {@link #cache} and the child count for this elm in the viewer/tree.
     */
    protected void updateChildren( Object elm, Object[] children, int currentChildCount  ) {
        cache.put( elm, children );

//        if (children.length != currentChildCount) {
        UIThreadExecutor.async( () -> { 
            viewer.setChildCount( elm, children.length );
            viewer.replace( elm, 0, children.length > 0 ? children[0] : null );  // replace the LOADING elm
        }, logErrorMsg( "" ) );
//        }
    }


    /**
     * Marks the given elm as {@link #CACHE_LOADING}. 
     */
    protected void updateChildrenLoading( Object elm ) {
        cache.put( elm, CACHE_LOADING );
        viewer.setChildCount( elm, 1 );         
    }


    @Override
    public void updateElement( Object parent, int index ) {
        Object[] children = cache.get( parent );
        if (children == null) {
            return;
//            updateChildCount( parent, -1 );
//            children = cache.get( parent );
        }
        if (index < children.length) {
            Object child = children[index];
            viewer.replace( parent, index, child );
            boolean hasChildren = !(child instanceof IResourceInfo);
            viewer.setHasChildren( child, hasChildren );
        }
        else {
            viewer.replace( parent, index, NO_CHILDREN );
        }
        
//        updateChildCount( children[index], -1 );
    }


    @Override
    public Object getParent( Object elm ) {
        log.debug( "getParent( " + elm.getClass().getSimpleName() + "): ..." );
        if (elm == input) {
            return null;
        }
        else if (elm instanceof IResourceInfo) {
            // skip service level, see #updateMetadata()
            return ((IResourceInfo)elm).getServiceInfo().getMetadata();
        }
        else if (elm instanceof IServiceInfo) {
            throw new IllegalArgumentException( "Service level is skipped!?" );
            //return ((IServiceInfo)elm).getMetadata();
        }
        else if (elm instanceof IMetadata) {
            return ((IMetadata)elm).getCatalog();
        }
        else if (elm instanceof IMetadataCatalog
                && input instanceof Collection
                /*&& ((Collection)input).contains( elm )*/) {
            return input;
        }
        else {
            throw new IllegalArgumentException( "Unknown element type: " + elm );
        }
    }

    
    public TreePath treePathOf( Object elm ) {
        LinkedList result = new LinkedList();
        for (Object current = elm; current != null; current = getParent( current )) {
            if (current != input) {
                result.addFirst( current );
            }
        }
        return new TreePath( result.toArray() );
    }
    
    
//    // ITreeContentProvider *******************************
//    
//    @Override
//    public boolean hasChildren( Object elm ) {
//        if (elm instanceof IMetadataCatalog) {
//            return true;
//        }
//        else if (elm instanceof IMetadata) {
//            return true;
//        }
//        else if (elm instanceof IServiceInfo) {
//            return true;
//        }
//        else if (elm instanceof IResourceInfo) {
//            return false;
//        }
//        return false;
//    }
//
//    
//    @Override
//    public Object[] getChildren( Object elm ) {
//        if (elm instanceof IMetadataCatalog) {
//            return ((IMetadataCatalog)elm).query( catalogQuery ).execute().stream().toArray();
//        }
//        else if (elm instanceof IMetadata) {
//            Map<String,String> connectionParams = ((IMetadata)elm).getConnectionParams();
//            if (resolver.canResolve( connectionParams )) {
//                resolver.resolve( )
//            }
//            return .
//        }
//        else if (elm instanceof IServiceInfo) {
//            return true;
//        }
//        else if (elm instanceof IResourceInfo) {
//            return false;
//        }
//        return false;
//    }
//
//
//    @Override
//    public Object[] getElements( Object elm ) {
//        return getChildren( elm );
//    }
//
//    
//    @Override
//    public Object getParent( Object elm ) {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }
    
}
