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
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import org.eclipse.core.runtime.IPath;
import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
class WebDavFolderResource
        extends WebDavResource
        implements CollectionResource, GetableResource, PropFindableResource {

    private static Log log = LogFactory.getLog( WebDavFolderResource.class );


    public WebDavFolderResource( ContentManager contentManager, IContentFolder node, SecurityManager securityManager ) {
        super( contentManager, node, securityManager );
    }

    
    public Resource child( String childName ) {
        IPath childPath = node.getPath().append( childName );
        IContentNode child = contentManager.getNode( childPath );
        return WebDavResourceFactory.wrapContentNode( child, contentManager, securityManager );
    }


    public List<? extends Resource> getChildren() {
        List<Resource> result = new ArrayList();
        for (IContentNode child : contentManager.getChildren( node.getPath() )) {
            result.add( WebDavResourceFactory.wrapContentNode( child, contentManager, securityManager ) );
        }
        return result;
    }


    public String getContentType(String accepts) {
        return "text/html";
    }


    public Long getContentLength() {
        return null;
    }


    public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
    throws IOException, NotAuthorizedException, BadRequestException {
        try {
            org.polymap.service.fs.spi.Range fsRange = range != null
                    ? new org.polymap.service.fs.spi.Range( range.getStart(), range.getFinish() )
                    : null;

            ((IContentFolder)node).sendDescription( out, fsRange, params, contentType );

//            if (contentType.toLowerCase().contains( "html" )) {
//                new OutputStreamWriter( out, Charset.forName( "UTF-8" ) )
//                        .append( "<hr><em>Generated by POLYMAP3 WebDAV</hr></em>" ).flush();
//            }
        }
        catch (IOException e) {
            throw e;
        }
        catch (org.polymap.service.fs.spi.BadRequestException e) {
            throw new BadRequestException( this, e.getMessage() );
        }
    }

    
    public Date getCreateDate() {
        return null;
    }

}
