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

import io.milton.http.FileItem;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.PostableResource;
import io.milton.http.SecurityManager;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentFile;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WebDavWriteableFileResource
        extends WebDavFileResource
        implements PostableResource {

    private static Log log = LogFactory.getLog( WebDavWriteableFileResource.class );

    
    public WebDavWriteableFileResource( ContentManager contentManager, IContentFile node,
            SecurityManager securityManager ) {
        super( contentManager, node, securityManager );
    }

    
    public String processForm( Map<String, String> params, Map<String, FileItem> files )
    throws BadRequestException, NotAuthorizedException, ConflictException {
        log.info( "params: " + params );
        log.info( "files: " + files );
        return null;
    }

}
