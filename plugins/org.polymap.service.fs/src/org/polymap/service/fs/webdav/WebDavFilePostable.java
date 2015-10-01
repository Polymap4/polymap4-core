/* 
 * polymap.org
 * Copyright (C) 2011-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.service.fs.webdav;

import io.milton.http.Auth;
import io.milton.http.FileItem;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.PostableResource;
import io.milton.resource.ReplaceableResource;
import io.milton.http.SecurityManager;

import java.util.Date;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentWriteable;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class WebDavFilePostable
        implements PostableResource, ReplaceableResource {

    private static Log log = LogFactory.getLog( WebDavFilePostable.class );

    private ContentManager          contentManager;
    
    private IContentWriteable       node;

    private SecurityManager         securityManager;


    public WebDavFilePostable( ContentManager contentManager, IContentWriteable node, SecurityManager securityManager ) {
        this.contentManager = contentManager;
        this.node = node;
        this.securityManager = securityManager;
    }


    public void replaceContent( InputStream in, Long length )
            throws BadRequestException, ConflictException, NotAuthorizedException {
        log.info( "length: " + length );
        try {
            node.replaceContent( in, length );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
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


    public String processForm( Map<String,String> params, Map<String,FileItem> files )
            throws BadRequestException, NotAuthorizedException, ConflictException {
        log.info( "params: " + params );
        log.info( "files: " + files );
        try {
            return node.processForm( params, files );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
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


    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType )
            throws IOException, NotAuthorizedException, BadRequestException {
        throw new RuntimeException( "Method must not be called." );
    }
    
}
