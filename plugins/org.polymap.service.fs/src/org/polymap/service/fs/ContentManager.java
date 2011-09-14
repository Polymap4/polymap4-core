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
package org.polymap.service.fs;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.polymap.core.runtime.SessionContext;

import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;

/**
 * A ContentManager represents a content session corresponding to one user. It
 * provides the {@link IContentSite context} for the content nodes created by the
 * different {@link IContentProvider content providers}. On the other hand it
 * provides a facade to the several content providers. This facade is used by the
 * front end systems like the WebDAV service.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContentManager {

    private static Log log = LogFactory.getLog( ContentManager.class );

    private static ConcurrentHashMap<String,ContentManager> managers = new ConcurrentHashMap( 64, 0.75f, 4 );

    /**
     * As the content providers don't know about (HTTP) sessions, there is just one
     * {@link ContentManager} per user. Several sessions may share one manager and
     * the generated/cached content.
     * 
     * @param username
     * @param locale
     * @param sessionContext
     * @return The ContentManager for the given user.
     */
    public static ContentManager forUser( String username, Locale locale, SessionContext sessionContext ) {
        ContentManager newManager = new ContentManager( username, locale, sessionContext );
        ContentManager result = managers.putIfAbsent( username, newManager ); 
        result = result != null ? result : newManager; 
        int count = result.sessionCount.incrementAndGet();
        log.info( "ContentManager: user=" + username + ", sessions=" + count );
        return result;
    }
    
    /**
     * Hint that a session for the given user was closed.
     * 
     * @param username
     */
    public static boolean releaseSession( String username ) {
        ContentManager manager = managers.get( username );
        if (manager != null && manager.sessionCount.decrementAndGet() <= 0) {
            return managers.remove( username, manager );
        }
        return false;
    }
    
    
    // instance *******************************************

    private SessionContext              sessionContext;
    
    private String                      username;
    
    private AtomicInteger               sessionCount = new AtomicInteger();
    
    private Locale                      locale;
    
    private ContentSite                 site;
    
    /** 
     * Holds the tree structure of the already created nodes. It maps
     * parent path into child nodes mapped by their node names. 
     */
    private Map<IPath,Map<String,IContentNode>> nodes = new HashMap( 256 );
    
    private ReentrantReadWriteLock      nodesLock = new ReentrantReadWriteLock();
    
    private DefaultContentFolder        rootNode;
    
    
    protected ContentManager( String username, Locale locale, SessionContext sessionContext ) {
        this.sessionContext = sessionContext;
        this.username = username;
        this.locale = locale;
        this.site = new ContentSite();
        
        // root node
        this.rootNode = new DefaultContentFolder( "[root]", null, null, null ) {

            private IPath       path = parsePath( "/" );
            
            public IPath getPath() {
                return path;
            }

            public String getDescription( String contentType ) {
                return "Das ist das Basisverzeichnis des <b>POLYMAP3 Webdav Servers</b>.";
            }
            
        };
    }
    
    
    public IContentNode getNode( IPath path ) {
        
        checkInitContent( path );

        IContentNode result = null;
        if (path.segmentCount() == 0) {
            result = rootNode;
        }
        else {
            try {
                nodesLock.readLock().lock();

                IPath parentPath = path.removeLastSegments( 1 );
                String nodeName = path.lastSegment();
                Map<String, IContentNode> parentChildren = nodes.get( parentPath );
                result = parentChildren.get( nodeName );
            }
            finally {
                nodesLock.readLock().unlock();
            }
        }
        
        return result;
    }
    
    
    public Iterable<IContentNode> getChildren( IPath path ) {
        
        checkInitContent( path );
        
        try {
            nodesLock.readLock().lock();
            Map<String,IContentNode> result = nodes.get( path );
            return result.values();
        }
        finally {
            nodesLock.readLock().unlock();
        }
    }


    /**
     * Initialize the content tree up to the given path, including the children of
     * the last node.
     * 
     * @param path
     */
    private void checkInitContent( IPath path ) {
        assert path != null;

        try {
            nodesLock.readLock().lock();

            IPath initPath = rootNode.getPath();
            for (int i=-1; i < path.segmentCount(); i++) {

                // first loop for the root
                initPath = (i >= 0) ? initPath.append( path.segment( i ) ) : initPath;

                Map<String, IContentNode> result = nodes.get( initPath );
                if (result == null) {
                    if (!nodesLock.writeLock().isHeldByCurrentThread()) {
                        nodesLock.readLock().unlock();
                        nodesLock.writeLock().lock();
                    }
                    
                    result = new HashMap( 64 );

                    for (ContentProviderExtension ext : ContentProviderExtension.all()) {

                        List<? extends IContentNode> children = ext.getProvider().getChildren( initPath, site );

                        if (children != null) {
                            for (IContentNode child : children) {
                                IContentNode old = result.put( child.getName(), child );
                                if (old != null) {
                                    log.warn( "Child node name already exists: " + child.getName() );
                                }
                            }
                        }
                    }
                    nodes.put( initPath, result );
                }
            }
        }
        finally {
            if (nodesLock.writeLock().isHeldByCurrentThread()) {
                nodesLock.readLock().lock();
                nodesLock.writeLock().unlock();
            }
            nodesLock.readLock().unlock();
        }
    }


    public IPath parsePath( String pathString ) {
        pathString = StringUtils.replace( pathString, "//", "/" );
        return Path.fromOSString( pathString );
    }


    /*
     * 
     */
    class ContentSite
            implements IContentSite {

        private Map<String,Object>          data = new HashMap();
        
        
        public IContentFolder getFolder( IPath path ) {
            assert path != null;
            
            if (path.segmentCount() == 0) {
                return rootNode;
            }
            else {
                try {
                    nodesLock.readLock().lock();

                    IPath parentPath = path.removeLastSegments( 1 );
                    String nodeName = path.lastSegment();
                    Map<String, IContentNode> parentChildren = nodes.get( parentPath );

                    if (parentChildren != null) {
                        IContentNode node = parentChildren.get( nodeName );
                        return node instanceof IContentFolder ? (IContentFolder)node : null;
                    }
                    else {
                        return null;
                    }
                }
                finally {
                    nodesLock.readLock().unlock();
                }
            }
        }
        
        public void invalidateNode( IContentNode node ) {
            if (sessionContext != null) {
                FsPlugin.getDefault().invalidateSession( sessionContext );
                sessionContext = null;
            }
        }

        public Object put( String key, Object value ) {
            return data.put( key, value );
        }
        
        public Object get( String key ) {
            return data.get( key );
        }

        public Locale getLocale() {
            return locale;
        }
        
        public String getUserName() {
            return username;
        }
    }
    
}
