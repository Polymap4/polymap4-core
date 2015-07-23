/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IMetadataCatalog;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResolvableInfo;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.runtime.StreamIterable;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.ui.UIThreadExecutor;

/**
 * Content provider for {@link IMetadataCatalog} (or Collection or Array thereof as input),
 * {@link IMetadata}, {@link IServiceInfo} and {@link IResourceInfo} objects.
 * <p/>
 * XXX check databinding ObservableListContentProvider
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MetadataContentProvider
        extends Configurable
        implements /*ITreeContentProvider,*/ ILazyTreeContentProvider {

    private static Log log = LogFactory.getLog( MetadataContentProvider.class );
    
    private TreeViewer                  viewer;
    
    private Object                      input;
    
    private String                      catalogQuery = "";
    
    private IMetadataResourceResolver   resolver;
    
    /**
     * XXX make this evictable?
     */
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
    }


    // ILazyTreeContentProvider ***************************

    @Override
    public void updateChildCount( Object elm, int currentChildCount ) {
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
            UIJob job = new UIJob( "Query catalog" ) {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    Object[] children = ((IMetadataCatalog)elm).query( catalogQuery ).execute().stream().toArray();
                    cache.put( elm, children );
                    if (children.length != currentChildCount) {
                        UIThreadExecutor.async( 
                                () -> viewer.setChildCount( elm, children.length ),
                                UIThreadExecutor.logErrorMsg( "" ) );
                    }
                }
            };
            job.schedule();
        }
        // IMetadata -> resolve
        else if (elm instanceof IMetadata) {
            UIJob job = new UIJob( "Resolve service" ) {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    Map<String,String> connectionParams = ((IMetadata)elm).getConnectionParams();
                    if (resolver.canResolve( connectionParams )) {
                        IResolvableInfo resource = resolver.resolve( connectionParams, monitor );
                        Object[] children = new Object[] {resource};
                        cache.put( elm, children );
                        UIThreadExecutor.async( 
                                () -> viewer.setChildCount( elm, children.length ), 
                                UIThreadExecutor.logErrorMsg( "" ) );                        
                    }
                }
            };
            job.schedule();
        }
        // IServiceInfo
        else if (elm instanceof IServiceInfo) {
            UIJob job = new UIJob( "Find resources" ) {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    Object[] children = StreamIterable.of( ((IServiceInfo)elm).getResources() ).stream().toArray();
                    cache.put( elm, children );
                    if (children.length != currentChildCount) {
                        UIThreadExecutor.async( 
                                () -> viewer.setChildCount( elm, children.length ), 
                                UIThreadExecutor.logErrorMsg( "" ) );                                                
                    }
                }
            };
            job.schedule();
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


    @Override
    public void updateElement( Object parent, int index ) {
        Object[] children = cache.get( parent );
        viewer.replace( parent, index, children[index] );
        updateChildCount( children[index], -1 );
    }


    @Override
    public Object getParent( Object element ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
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
