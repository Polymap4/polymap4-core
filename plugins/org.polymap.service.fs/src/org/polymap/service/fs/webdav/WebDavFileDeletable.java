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

import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.DeletableResource;
import io.milton.http.SecurityManager;

import java.util.Date;
import java.util.Map;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentDeletable;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class WebDavFileDeletable
        implements DeletableResource {

    private static Log log = LogFactory.getLog( WebDavFileDeletable.class );

    private ContentManager          contentManager;
    
    private IContentDeletable       node;

    private SecurityManager         securityManager;


    public WebDavFileDeletable( ContentManager contentManager, IContentDeletable node, SecurityManager securityManager ) {
        this.contentManager = contentManager;
        this.node = node;
        this.securityManager = securityManager;
    }

    
    public void delete()
    throws NotAuthorizedException, ConflictException, BadRequestException {
        try {
            node.delete();
        }
        catch (org.polymap.service.fs.spi.BadRequestException e) {
            log.warn( "", e );
            throw new BadRequestException( this, e.getLocalizedMessage() );
        }
        catch (org.polymap.service.fs.spi.NotAuthorizedException e) {
            log.warn( "", e );
            throw new NotAuthorizedException( this );
        }
    }


    // these methods are never called; implementations are provided by WebDavFileResource

    public Object authenticate( String user, String password ) {
        throw new RuntimeException( "Method must not be called." );
    }


    public boolean authorise( Request request, Method method, Auth auth ) {
        throw new RuntimeException( "Method must not be called." );
    }


    public String checkRedirect( Request request ) {
        throw new RuntimeException( "Method must not be called." );
    }


    public Date getModifiedDate() {
        throw new RuntimeException( "Method must not be called." );
    }


    public String getName() {
        throw new RuntimeException( "Method must not be called." );
    }


    public String getRealm() {
        throw new RuntimeException( "Method must not be called." );
    }


    public String getUniqueId() {
        throw new RuntimeException( "Method must not be called." );
    }


    public Long getContentLength() {
        throw new RuntimeException( "Method must not be called." );
    }


    public String getContentType( String accepts ) {
        throw new RuntimeException( "Method must not be called." );
    }


    public Long getMaxAgeSeconds( Auth auth ) {
        throw new RuntimeException( "Method must not be called." );
    }


    public void sendContent( OutputStream out, Range range, Map<String, String> params,
            String contentType )
            throws IOException, NotAuthorizedException, BadRequestException {
        throw new RuntimeException( "Method must not be called." );
    }
    
}
