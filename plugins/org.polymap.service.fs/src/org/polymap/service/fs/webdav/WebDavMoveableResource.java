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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentMoveable;
import org.polymap.service.fs.spi.IContentNode;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WebDavMoveableResource
        extends WebDavResource
        implements MoveableResource {

    private static Log log = LogFactory.getLog( WebDavMoveableResource.class );

    
    public WebDavMoveableResource( ContentManager contentManager, IContentNode node,
            SecurityManager securityManager ) {
        super( contentManager, node, securityManager );
    }

    public void moveTo( CollectionResource dest, String newName )
            throws ConflictException, NotAuthorizedException, BadRequestException {
        try {
            IPath destPath = ((ContentNodeResource)dest).getNode().getPath();
            ((IContentMoveable)node).moveTo( destPath, newName );
        }
        catch (IOException e) {
            log.warn( "", e );
            throw new BadRequestException( this, e.getMessage() );
        }        
        catch (org.polymap.service.fs.spi.BadRequestException e) {
            throw new BadRequestException( this, e.getMessage() );
        }        
    }
    
}
