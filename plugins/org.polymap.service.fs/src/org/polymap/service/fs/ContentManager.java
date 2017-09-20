/* 
 * polymap.org
 * Copyright 2011-2017, Polymap GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.runtime.cache.EvictionAware;
import org.polymap.core.runtime.cache.EvictionListener;
import org.polymap.core.runtime.session.SessionContext;

import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;

/**
 * The ContentManager represents a content session corresponding to one user. Several
 * (HTTP) sessions of the same user may share one manager and the generated/cached
 * content.
 * <p/>
 * A ContentManager provides the {@link IContentSite context} for the content nodes
 * created by the different {@link IContentProvider content providers}. On the other
 * hand it provides a facade to the several content providers. This facade is used by
 * the front end systems like the WebDAV service.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContentManager {

    private static final Log log = LogFactory.getLog( ContentManager.class );

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
        ContentManager result = managers.get( username );
        if (result == null) {
            ContentManager newManager = new ContentManager( username, locale, sessionContext );
            result = managers.putIfAbsent( username, newManager ); 
            result = result != null ? result : newManager; 
            int count = result.sessionCount.incrementAndGet();
            log.info( "ContentManager: user=" + username + ", sessions=" + count );
        }
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
            boolean success = managers.remove( username, manager );
            if (success) {
                manager.dispose();
            }
            return success;
        }
        return false;
    }
    
    
    // instance *******************************************

    private SessionContext              sessionContext;
    
    private String                      username;
    
    private AtomicInteger               sessionCount = new AtomicInteger();
    
    private Locale                      locale;
    
    private ContentSite                 site = new ContentSite();
    
    /** 
     * Holds the tree structure of the already created nodes. It maps
     * parent path into child nodes mapped by their node names. 
     */
    private Cache<IPath,CachedNode>     nodes;
    
    private DefaultContentFolder        rootNode;

    private List<IContentProvider>      providers;
    
    
    /**
     * 
     */
    static class CachedNode
            extends HashMap<String,IContentNode>
            implements EvictionAware {

        public CachedNode() {
            super( 64 );
        }

        public boolean isValid() {
            for (IContentNode node : values()) {
                if (!node.isValid()) {
                    return false;
                }
            }
            return true;
        }
        
        public EvictionListener newListener() {
            return new CachedNodeEvictionListener();
        }
        
        static class CachedNodeEvictionListener
                implements EvictionListener {

            public void onEviction( Object key ) {
                log.debug( "*** EVICTED *************************************" );
            }
        }
        
    }

    
    protected ContentManager( String username, Locale locale, SessionContext sessionContext ) {
        this.sessionContext = sessionContext;
        this.username = username;
        this.locale = locale;

        this.nodes = CacheConfig.defaults().initSize( 256 ).concurrencyLevel( 4 ).createCache();

//        // eviction listener -> node.dispose()
//        nodes.addEvictionListener( new CacheEvictionListener<IPath,Map<String,IContentNode>>() {
//            public void onEviction( IPath path, final Map<String,IContentNode> children ) {
//                ContentManager.this.sessionContext.execute( new Runnable() {
//                    public void run() {
//                        if (children != null) {
//                            for (IContentNode child : children.values()) {
//                                try {
//                                    child.dispose();
//                                }
//                                catch (Exception e) {
//                                    log.warn( "Error during dispose for eviction.", e );
//                                }
//                            }
//                        }
//                    }
//                });
//            }
//        });
        
        // init providers
        this.providers = new ArrayList();
        for (ContentProviderExtension ext : ContentProviderExtension.all()) {
            IContentProvider provider = ext.newProvider();
            provider.init( site );
            providers.add( provider );
        }
        
        // root node (with fake provider)
        this.rootNode = new DefaultContentFolder( "[root]", null, null, null ) {

            private IPath       path = parsePath( "/" );
            
            public IPath getPath() {
                return path;
            }

            public String getDescription( String contentType ) {
                return "Das ist das Basisverzeichnis des <b>POLYMAP3 Webdav Servers</b>.";
            }

            public IContentProvider getProvider() {
                return new DefaultContentProvider() {

                    public IContentSite getSite() {
                        return site;
                    }

                    public List<? extends IContentNode> getChildren( IPath parentPath ) {
                        // must never be called
                        throw new RuntimeException( "not yet implemented." );
                    }
                };
            }
        };
    }
    
    
    public void dispose() {
        for (IContentProvider provider : providers) {
            provider.dispose();
        }
        providers = null;
        
        for (Map<String,IContentNode> children : nodes.values()) {
            for (IContentNode child : children.values()) {
                child.dispose();
            }
        }
        nodes.clear();
        nodes.dispose();
        nodes = null;
    }
    
    
    public IContentNode getNode( IPath path ) {
        if (path.segmentCount() == 0) {
            return rootNode;
        }
        else {
            IPath parentPath = path.removeLastSegments( 1 );
            String nodeName = path.lastSegment();
            CachedNode parentChildren = checkInitContent( parentPath );
            return parentChildren.get( nodeName );
        }
    }
    
    
    public Iterable<IContentNode> getChildren( IPath path ) {
        CachedNode result = checkInitContent( path );
        assert result != null : "No result for path: " + path;
        return result.values();
    }


    /**
     * Initialize the content tree up to the given path, including the children of
     * the last node.
     * 
     * @param path
     * @return 
     */
    private CachedNode checkInitContent( IPath path ) {
        assert path != null;

        IPath initPath = rootNode.getPath();
        CachedNode lastResult = null;
        
        for (int i=-1; i<path.segmentCount(); i++) {

            // first loop for the root
            initPath = (i >= 0) ? initPath.append( path.segment( i ) ) : initPath;

            // check cache expiration
            if (lastResult != null) {
                IContentNode node = lastResult.get( initPath.lastSegment() );
                if (node instanceof IContentFolder && !node.isValid()) {
                    invalidateFolder( (IContentFolder)node );
                }
            }
            
            // get/create
            lastResult = nodes.get( initPath, new CacheLoader<IPath,CachedNode,RuntimeException>() {
                private int memSize = 1024;
                
                @Override
                public CachedNode load( IPath key ) throws RuntimeException {
                    CachedNode result = new CachedNode();

                    for (IContentProvider provider : providers) {
                        List<? extends IContentNode> children = provider.getChildren( key );
                        if (children != null) {
                            for (IContentNode child : children) {
                                IContentNode old = result.put( child.getName(), child );
                                if (old != null) {
                                    log.warn( "!!! Child node name already exists: " + child.getName() + "!!!" );
                                }
                                memSize += child.getSizeInMemory();
                            }
                        }
                    }
                    return result;
                }

                @Override
                public int size() throws RuntimeException {
                    return memSize;
                }
            });
        }
        return lastResult;
    }


    public IPath parsePath( String pathString ) {
        pathString = StringUtils.replace( pathString, "//", "/" );
        return Path.fromOSString( pathString );
    }

    public void invalidateFolder( IContentFolder node ) {
        assert node != null;
        //      IPath path = node.getPath();
        //      IPath parentPath = path.removeLastSegments( 1 );
        //      String nodeName = path.lastSegment();
        //      Map<String,IContentNode> parentChildren = nodes.get( parentPath );
        //
        //      if (parentChildren != null) {
        //          IContentNode found = parentChildren.get( nodeName );
        //          if (found == node) {
        //              parentChildren.remove( nodeName );
        //          }
        //      }
        Map<String,IContentNode> children = nodes.remove( node.getPath() );
        if (children != null) {
            // XXX do it recursively?
            for (IContentNode child : children.values()) {
                nodes.remove( child.getPath() );
                child.dispose();
            }
        }
        node.dispose();
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
                IPath parentPath = path.removeLastSegments( 1 );
                String nodeName = path.lastSegment();
                Map<String, IContentNode> parentChildren = checkInitContent( parentPath );

                if (parentChildren != null) {
                    IContentNode node = parentChildren.get( nodeName );
                    return node instanceof IContentFolder ? (IContentFolder)node : null;
                }
                else {
                    return null;
                }
            }
        }
        
        public Iterable<IContentNode> getChildren( IPath path ) {
            return ContentManager.this.getChildren( path );
        }
        
        public void invalidateFolder( IContentFolder node ) {
            ContentManager.this.invalidateFolder( node );
        }
        
        public void invalidateSession() {
            if (sessionContext != null) {
                FsPlugin.getDefault().invalidateSession( sessionContext );
                sessionContext = null;
            }
        }

        public SessionContext getSessionContext() {
            return sessionContext;
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
