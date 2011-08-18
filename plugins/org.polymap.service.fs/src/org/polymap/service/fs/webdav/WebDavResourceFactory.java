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
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.SecurityManager;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;

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
    
    
    protected WebDavResourceFactory( SecurityManager securityManager ) {
        this.securityManager = securityManager;
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
                
                contentManager = ContentManager.forUser( user );
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
        IContentNode node = contentManager.getNode( path );
        return wrapContentNode( node, contentManager, securityManager );
    }
    
    
    static Resource wrapContentNode( IContentNode node, ContentManager contentManager, SecurityManager securityManager ) {
        if (node == null) {
            throw new RuntimeException( "Node is null." );
        }
        else if (node instanceof IContentFolder) {
            return new WebDavFolderResource( contentManager, (IContentFolder)node, securityManager );  
        }
        else if (node instanceof IContentFile) {
            return new WebDavFileResource( contentManager, (IContentFile)node, securityManager );  
        }
        else {
            throw new IllegalStateException( "Unknown node type: " + node );
        }        
    }
    
}
