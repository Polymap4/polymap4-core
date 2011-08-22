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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.ServletRequest;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentDeletable;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentWriteable;

/**
 * A {@link ResourceFactory} that provides the content of several
 * {@link ContentManager}, one per user/sessions, to a {@link WebDavServer}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class WebDavResourceFactory
        implements ResourceFactory {

    private static Log log = LogFactory.getLog( WebDavResourceFactory.class );

    private static final String             NULL_USER_NAME = "null";
    
    /** Maps username into {@link ContentManager}. */
    private Map<String,ContentManager>      contentManagers = new HashMap();

    /** Synchronizes access to {@link #contentManagers}. */
    private ReentrantReadWriteLock          lock = new ReentrantReadWriteLock();

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
        contentManagers = null;
        lock = null;
    }

    
    public Resource getResource( String host, String path ) {
        Request request = WebDavServer.request();
        assert request != null;
        
        Auth auth = request.getAuthorization();
        String user = auth != null ? auth.getUser() : NULL_USER_NAME;
        
        // check/get ContentManager
        ContentManager contentManager = null;
        try {
            lock.readLock().lock();
            
            contentManager = contentManagers.get( user );
            if (contentManager == null) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                
                HttpServletRequest httpRequest = ServletRequest.getRequest();
                Locale locale = httpRequest != null 
                        ? httpRequest.getLocale()
                        : Locale.getDefault();
                
                contentManager = ContentManager.forUser( user, locale );
                contentManagers.put( user, contentManager );
            }
        }
        finally {
            if (lock.writeLock().isHeldByCurrentThread()) {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
            lock.readLock().unlock();
        }
        
        // get content
        path = StringUtils.substringAfter( path, contextPath );
        IContentNode node = contentManager.getNode( contentManager.parsePath( path ) );
        return node != null ? wrapContentNode( node, contentManager, securityManager ) : null;
    }
    
    
    static Resource wrapContentNode( IContentNode node, ContentManager contentManager, SecurityManager securityManager ) {
        if (node == null) {
            throw new RuntimeException( "Node is null." );
        }
        // folder
        else if (node instanceof IContentFolder) {
            return new WebDavFolderResource( contentManager, (IContentFolder)node, securityManager );  
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
     * Provides an InvocationHandler for a proxy that exposes the different
     * interfaces for Getable, Postable, Deletable, etc.
     */
    static class WebDavFileHandler
            implements InvocationHandler {

        private IContentFile        node;
        
        private Map<Class,Object>   handlers = new HashMap();
        
        
        public WebDavFileHandler( ContentManager contentManager, IContentFile node, SecurityManager securityManager ) {
            this.node = node;
            
            WebDavFileResource getable = new WebDavFileResource( contentManager, node, securityManager );       
            handlers.put( Resource.class, getable );
            handlers.put( GetableResource.class, getable );
            handlers.put( PropFindableResource.class, getable );
            
            if (node instanceof IContentWriteable) {
                WebDavFilePostable postable = new WebDavFilePostable( contentManager, (IContentWriteable)node, securityManager );
                handlers.put( PostableResource.class, postable );
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
            log.debug( "invoke(): method= " + method.getName() + ", declaringClass= " + method.getDeclaringClass().getSimpleName() );

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
