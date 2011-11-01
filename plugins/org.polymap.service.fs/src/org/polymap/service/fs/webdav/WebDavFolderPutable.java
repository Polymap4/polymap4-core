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

import java.util.Date;
import java.util.List;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentPutable;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WebDavFolderPutable
        implements PutableResource {

    private static Log log = LogFactory.getLog( WebDavFolderPutable.class );

    private ContentManager          contentManager;
    
    private IContentPutable         node;

    private SecurityManager         securityManager;


    public WebDavFolderPutable( ContentManager contentManager, IContentPutable node, SecurityManager securityManager ) {
        this.contentManager = contentManager;
        this.node = node;
        this.securityManager = securityManager;
    }

    
    public void delete()
    throws NotAuthorizedException, ConflictException, BadRequestException {
    }


    public Resource createNew( String newName, InputStream in, Long length, String contentType )
    throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        try {
            IContentFile result = node.createNew( newName, in, length, contentType );
            return WebDavResourceFactory.wrapContentNode( result, contentManager, securityManager );
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

    public Resource child( String childName ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public List<? extends Resource> getChildren() {
        throw new RuntimeException( "not yet implemented." );
    }


    public Object authenticate( String user, String password ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public boolean authorise( Request request, Method method, Auth auth ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public String checkRedirect( Request request ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public Date getModifiedDate() {
        throw new RuntimeException( "not yet implemented." );
    }


    public String getName() {
        throw new RuntimeException( "not yet implemented." );
    }


    public String getRealm() {
        throw new RuntimeException( "not yet implemented." );
    }


    public String getUniqueId() {
        throw new RuntimeException( "not yet implemented." );
    }

}
