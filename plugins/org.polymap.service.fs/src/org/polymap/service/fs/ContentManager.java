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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;

/**
 * A ContentManager represents a session corresponding to one user. It provides the
 * {@link IContentSite context} for the content nodes created by the different
 * {@link IContentProvider content providers}. On the other hand it provides
 * a facade to the several content providers. This facade is used by the front end
 * systems like the WebDAV service.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContentManager {

    private static Log log = LogFactory.getLog( ContentManager.class );
    
    
    public static ContentManager forUser( String username, Locale locale ) {
        return new ContentManager( username, locale );    
    }
    
    
    // instance *******************************************

    private String                      username;
    
    private Locale                      locale;
    
    private ContentSite                 site;
    
    /** 
     * Holds the tree structure of the already created nodes. It maps
     * parent path into child nodes mapped by their node names. 
     */
    private Map<IPath,Map<String,IContentNode>> nodes = new HashMap( 256 );
    
    private DefaultContentFolder        rootNode;
    
    
    protected ContentManager( String username, Locale locale ) {
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
            IPath parentPath = path.removeLastSegments( 1 );
            String nodeName = path.lastSegment();
            Map<String, IContentNode> parentChildren = nodes.get( parentPath );
            result = parentChildren.get( nodeName );
        }
        
        return result;
    }
    
    
    public Iterable<IContentNode> getChildren( IPath path ) {
        
        checkInitContent( path );
        
        Map<String,IContentNode> result = nodes.get( path );
        return result.values();
    }


    /**
     * Initialize the content tree up to the given path, including the children of
     * the last node.
     * 
     * @param path
     */
    private void checkInitContent( IPath path ) {
        assert path != null;

        IPath initPath = rootNode.getPath();
        
        for (int i=-1; i < path.segmentCount(); i++) {

            // first loop for the root
            initPath = (i >= 0) ? initPath.append( path.segment( i ) ) : initPath;

            Map<String, IContentNode> result = nodes.get( initPath );
            if (result == null) {
                result = new HashMap( 64 );

                for (ContentProviderExtension ext : ContentProviderExtension.all()) {

                    List<? extends IContentNode> children = ext.getProvider().getChildren( path, site );

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


    public IPath parsePath( String pathString ) {
        return Path.fromOSString( pathString );
    }


    /*
     * 
     */
    class ContentSite
            implements IContentSite {

        private Map<String,Object>          data = new HashMap();
        
        
        public IContentFolder getFolder( IPath path ) {
            log.info( "path=" + path );
            assert path != null;
            assert path.segmentCount() >= 1;
            
            IPath parentPath = path.removeLastSegments( 1 );
            String nodeName = path.lastSegment();
            Map<String, IContentNode> parentChildren = nodes.get( parentPath );
            return (parentChildren != null)
                    ? (IContentFolder)parentChildren.get( nodeName )
                    : null;
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
        
    }
    
}
