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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;

import org.polymap.core.runtime.Polymap;

/**
 * Adapter to the POLYMAP authentication. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SecurityManagerAdapter
        implements SecurityManager {

    private static Log log = LogFactory.getLog( SecurityManagerAdapter.class );

    private String          realm;
    
    
    public SecurityManagerAdapter( String realm ) {
        this.realm = realm;
    }
   
    public String getRealm( String host ) {
        return realm;
    }

    public boolean isDigestAllowed() {
        return false;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public Object authenticate( String user, String passwd ) {
        log.info( "authenticate(): " + user + "/" + passwd );
        log.info( "authenticate(): " + Polymap.instance() );
        return (user != null && user.length() > 3) ? user : null;
    }

    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
        return auth != null && auth.getTag() != null;
    }
    
}
