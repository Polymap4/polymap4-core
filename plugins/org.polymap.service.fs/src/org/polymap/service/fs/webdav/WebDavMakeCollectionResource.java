/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IMakeFolder;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WebDavMakeCollectionResource
        extends WebDavFolderResource
        implements MakeCollectionableResource {

    public WebDavMakeCollectionResource( ContentManager contentManager, IContentFolder node,
            SecurityManager securityManager ) {
        super( contentManager, node, securityManager );
    }

    public CollectionResource createCollection( String newName )
            throws NotAuthorizedException, ConflictException, BadRequestException {
        IContentFolder newNode = ((IMakeFolder)node).createFolder( newName );
        return (CollectionResource)WebDavResourceFactory.wrapContentNode( newNode, contentManager, securityManager );
    }

}
