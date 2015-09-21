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
package org.polymap.service.fs.webdav;

import io.milton.http.Request;
import io.milton.http.ResourceFactory;
import io.milton.resource.CollectionResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PostableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.ReplaceableResource;
import io.milton.resource.Resource;
import io.milton.http.SecurityManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.session.SessionContext;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentDeletable;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentMoveable;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.IMakeFolder;

/**
 * A {@link ResourceFactory} that provides the content of several
 * {@link ContentManager}, one per user/sessions, to a {@link WebDavServer}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class WebDavResourceFactory
        implements ResourceFactory {

    private static Log log = LogFactory.getLog( WebDavResourceFactory.class );

    private SecurityManager                 securityManager;

    private String                          contextPath;


    /**
     * 
     * @param securityManager
     * @param contextPath - this is the leading part of URL's to ignore. For example
     *        if you're application is deployed to http://localhost:8080/webdav-fs,
     *        the context path should be webdav-fs
     */
    protected WebDavResourceFactory( SecurityManager securityManager, String contextPath ) {
        this.securityManager = securityManager;
        this.contextPath = contextPath;
    }

    
    public void dispose() {
    }

    
    public String getContextPath() {
        return contextPath;
    }


    public Resource getResource( String host, String path ) {
        Request request = WebDavServer.request();
        assert request != null;
        
        ContentManager contentManager = (ContentManager)
                SessionContext.current().getAttribute( "contentManager" );
        
        // get content
        path = StringUtils.substringAfter( path, contextPath );
        IContentNode node = contentManager.getNode( contentManager.parsePath( path ) );
        return node != null ? wrapContentNode( node, contentManager, securityManager ) : null;
    }
    
    
    static Resource wrapContentNode( IContentNode node, ContentManager contentManager, SecurityManager securityManager ) {
        if (node == null) {
            log.warn( "wrapContentNode(): node is null." );
            return null;
        }
        // folder
        else if (node instanceof IContentFolder) {
            WebDavFolderHandler folderHandler = new WebDavFolderHandler( contentManager, (IContentFolder)node, securityManager);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return (Resource)Proxy.newProxyInstance( cl, folderHandler.getInterfaces(), folderHandler );
        }
        // file
        else if (node instanceof IContentFile) {
            WebDavFileHandler fileHandler = new WebDavFileHandler( contentManager, (IContentFile)node, securityManager);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return (Resource)Proxy.newProxyInstance( cl, fileHandler.getInterfaces(), fileHandler );
        }
        // unknown
        else {
            throw new IllegalStateException( "Unknown node type: " + node );
        }        
    }
    
    
    /*
     * InvocationHandler of a proxy that exposes the different
     * interfaces for Getable, Postable, Deletable, etc.
     */
    static class WebDavFileHandler
            implements InvocationHandler {

        private IContentFile        node;
        
        private Map<Class,Object>   handlers = new HashMap();
        
        
        public WebDavFileHandler( ContentManager contentManager, IContentFile node, SecurityManager securityManager ) {
            this.node = node;
            
            WebDavFileResource getable = new WebDavFileResource( contentManager, node, securityManager );       
            handlers.put( ContentNodeResource.class, getable );
            handlers.put( Resource.class, getable );
            handlers.put( GetableResource.class, getable );
            handlers.put( PropFindableResource.class, getable );
            
            if (node instanceof IContentWriteable) {
                WebDavFilePostable postable = new WebDavFilePostable( contentManager, (IContentWriteable)node, securityManager );
                handlers.put( PostableResource.class, postable );
                handlers.put( ReplaceableResource.class, postable );
            }
            if (node instanceof IContentDeletable) {
                WebDavFileDeletable deletable = new WebDavFileDeletable( contentManager, (IContentDeletable)node, securityManager );
                handlers.put( DeletableResource.class, deletable );
            }
            if (node instanceof IContentMoveable) {
                WebDavMoveableResource res = new WebDavMoveableResource( contentManager, node, securityManager );
                handlers.put( MoveableResource.class, res );
            }
        }


        public Class[] getInterfaces() {
            Set<Class> result = handlers.keySet();
            return result.toArray( new Class[ result.size()] );
        }
        
        
        public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable {
            //log.debug( "invoke(): method= " + method.getName() + ", declaringClass= " + method.getDeclaringClass().getSimpleName() );

            Object handler = handlers.get( method.getDeclaringClass() );
            
            if (handler == null) {
                throw new IllegalStateException( "No handler for declaringClass: " + method.getDeclaringClass().getName() );
            }
            
            try {
                return method.invoke( handler, args );
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
        
    }
    

    /*
     * InvocationHandler of a proxy that exposes the different
     * interfaces for Getable, Postable, Deletable, etc.
     */
    static class WebDavFolderHandler
            implements InvocationHandler {

        private IContentFolder      node;
        
        private Map<Class,Object>   handlers = new HashMap();
        
        
        public WebDavFolderHandler( ContentManager contentManager, IContentFolder node, SecurityManager securityManager ) {
            this.node = node;
            
            WebDavFolderResource getable = new WebDavFolderResource( contentManager, node, securityManager );       
            handlers.put( ContentNodeResource.class, getable );
            handlers.put( Resource.class, getable );
            handlers.put( GetableResource.class, getable );
            handlers.put( CollectionResource.class, getable );
            handlers.put( PropFindableResource.class, getable );
            
            if (node instanceof IContentPutable) {
                WebDavFolderPutable putable = new WebDavFolderPutable( contentManager, (IContentPutable)node, securityManager );
                handlers.put( PutableResource.class, putable );
            }
            if (node instanceof IContentWriteable) {
                WebDavFilePostable postable = new WebDavFilePostable( contentManager, (IContentWriteable)node, securityManager );
                handlers.put( PostableResource.class, postable );
                handlers.put( ReplaceableResource.class, postable );
            }
            if (node instanceof IMakeFolder) {
                WebDavMakeCollectionResource res = new WebDavMakeCollectionResource( contentManager, node, securityManager );
                handlers.put( MakeCollectionableResource.class, res );
            }
            if (node instanceof IContentMoveable) {
                WebDavMoveableResource res = new WebDavMoveableResource( contentManager, node, securityManager );
                handlers.put( MoveableResource.class, res );
            }
            if (node instanceof IContentDeletable) {
                WebDavFileDeletable deletable = new WebDavFileDeletable( contentManager, (IContentDeletable)node, securityManager );
                handlers.put( DeletableResource.class, deletable );
            }
        }


        public Class[] getInterfaces() {
            Set<Class> result = handlers.keySet();
            return result.toArray( new Class[ result.size()] );
        }
        
        
        public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable {
            //log.debug( "invoke(): method= " + method.getName() + ", declaringClass= " + method.getDeclaringClass().getSimpleName() );

            Object handler = handlers.get( method.getDeclaringClass() );
            
            if (handler == null) {
                throw new IllegalStateException( "No handler for declaringClass: " + method.getDeclaringClass().getName() );
            }
            
            try {
                return method.invoke( handler, args );
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
        
    }
    
}
