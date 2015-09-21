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
import io.milton.http.SecurityManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.security.SecurityContext;
import org.polymap.core.security.UserPrincipal;

/**
 * Adapter to the {@link SecurityContext} authentication.
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
        HttpServletRequest req = io.milton.servlet.ServletRequest.getRequest();
        final HttpSession session = req.getSession();

        UserPrincipal sessionUser = (UserPrincipal)session.getAttribute( "sessionUser" );
        if (sessionUser == null) {
            log.info( "WebDAV login: " + user /*+ "/" + passwd*/ );
            SecurityContext sc = SecurityContext.instance();
            if (sc.isLoggedIn()) {
                log.info( "Already logged in as: " + sc.getUser().getName() );                
                sessionUser = (UserPrincipal)sc.getUser();
                session.setAttribute( "sessionUser", sessionUser );
                return WebDavServer.createNewSession( sessionUser );
            }
            else if (sc.login( user, passwd )) {
                sessionUser = (UserPrincipal)sc.getUser();
                session.setAttribute( "sessionUser", sessionUser );
                return WebDavServer.createNewSession( sessionUser );
            }
            else {
                log.warn( "Login failed." );
                return null;
            }
        }
        return sessionUser != null && sessionUser.getName().equals( user ) 
                ? sessionUser : null;
    }
    
    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
        return auth != null && auth.getTag() != null;
    }
    
}
