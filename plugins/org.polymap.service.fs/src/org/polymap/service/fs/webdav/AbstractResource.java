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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.Request.Method;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentNode;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class AbstractResource
        implements Resource {

    private static Log log = LogFactory.getLog( AbstractResource.class );

    protected ContentManager        contentManager;
    
    protected IContentNode          node;

    protected SecurityManager       securityManager;


    public AbstractResource( ContentManager contentManager, IContentNode node, SecurityManager securityManager ) {
        this.contentManager = contentManager;
        this.node = node;
        this.securityManager = securityManager;
    }

    
    public String getName() {
        return node.getName();
    }


    public Date getModifiedDate() {
        return node.getModifiedDate();
    }


    public String getUniqueId() {
        return String.valueOf( node.hashCode() );
    }


    public String getRealm() {
        Request request = WebDavServer.request();
        return securityManager.getRealm( request.getHostHeader() );
    }


    public Object authenticate( String user, String password ) {
        return securityManager.authenticate( user, password );
    }


    public boolean authorise( Request request, Method method, Auth auth ) {
        return securityManager.authorise( request, method, auth, this );
    }


    public String checkRedirect( Request request ) {
        return null;
    }
    
}
