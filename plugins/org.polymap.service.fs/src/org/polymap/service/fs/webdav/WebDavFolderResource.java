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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class WebDavFolderResource
        extends AbstractResource
        implements CollectionResource {

    private static Log log = LogFactory.getLog( WebDavFolderResource.class );


    public WebDavFolderResource( ContentManager contentManager, IContentFolder node, SecurityManager securityManager ) {
        super( contentManager, node, securityManager );
    }

    
    private IContentFolder delegate() {
        return (IContentFolder)node;
    }

    
    public Resource child( String childName ) {
        String pathString = node.getParentPath().append( childName ).toString();
        IContentNode child = contentManager.getNode( pathString );
        return WebDavResourceFactory.wrapContentNode( child, contentManager, securityManager );
    }


    public List<? extends Resource> getChildren() {
        String pathString = node.getParentPath().toString();
        
        List<Resource> result = new ArrayList();
        for (IContentNode child : contentManager.getChildren( pathString )) {
            result.add( WebDavResourceFactory.wrapContentNode( child, contentManager, securityManager ) );
        }
        return result;
    }

}
