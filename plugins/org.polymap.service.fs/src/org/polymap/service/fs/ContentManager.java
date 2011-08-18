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
    
    
    public static ContentManager forUser( String username ) {
        return new ContentManager( username );    
    }
    
    
    // instance *******************************************

    private String                      username;
    
    private ContentSite                 site;
    
    /** 
     * Holds the tree structure of the already created nodes. It maps
     * parent path into child nodes mapped by their node names. 
     */
    private Map<IPath,Map<String,IContentNode>> nodes = new HashMap( 256 );
    
    
    protected ContentManager( String username ) {
        this.username = username;
        this.site = new ContentSite();
    }
    
    
    public IContentNode getNode( String pathString ) {
        IPath path = createPath( pathString );
        if (path.segmentCount() == 0) {
            return new DefaultContentFolder( "root", null, null, null );
        }
        else {
            IPath parentPath = path.removeLastSegments( 1 );
            String nodeName = path.lastSegment();
            return nodes.get( parentPath ).get( nodeName );
        }
    }
    
    
    public Iterable<IContentNode> getChildren( String pathString ) {
        IPath path = createPath( pathString );
        Map<String,IContentNode> result = nodes.get( path );

        if (result == null) {
            result = new HashMap( 64 );
            
            for (ContentProviderExtension ext : ContentProviderExtension.all()) {
                List<IContentNode> candidates = ext.getProvider().getChildren( path, site );
                if (candidates != null) {
                    for (IContentNode candidate : candidates) {
                        IContentNode old = result.put( candidate.getName(), candidate );
                        if (old != null) {
                            log.warn( "Child node name already exists: " + candidate.getName() );
                        }
                    }
                }
            }
            
            if (result.isEmpty()) {
                return null;
            }
            nodes.put( path, result );
        }
        return result.values();
    }
    
    
    private IPath createPath( String pathString ) {
        return Path.fromOSString( pathString );
    }


    /*
     * 
     */
    class ContentSite
            implements IContentSite {

        private Map<String,Object>          data = new HashMap();
        
        
        public IContentFolder parentFolder( IPath path ) {
            log.info( "path=" + path );
            throw new RuntimeException( "not yet implemented" );
        }
        
        public Object put( String key, Object value ) {
            return data.put( key, value );
        }
        
        public Object get( String key ) {
            return data.get( key );
        }
        
    }
    
}
