/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.security.spnego;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.PrivilegedActionException;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ietf.jgss.GSSException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.polymap.core.security.spnego.SpnegoHttpFilter.Constants;

/**
 * 
 * @see <a href="http://spnego.sourceforge.net/">http://spnego.sourceforge.net/</a>
 * @see SpnegoHttpFilter
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SpnegoHttpServlet
        extends HttpServlet {

    private static Log log = LogFactory.getLog( SpnegoHttpServlet.class );

    /** Object for performing Basic and SPNEGO authentication. */
    private transient SpnegoAuthenticator authenticator = null;

    
    @Override
    public void init() throws ServletException {
        try {
            // set some System properties
            final SpnegoFilterConfig config = SpnegoFilterConfig.getInstance( getServletConfig() );
            // pre-authenticate
            this.authenticator = new SpnegoAuthenticator( config );
        } 
        catch (final LoginException le) {
            throw new ServletException(le);
        } 
        catch (final GSSException gsse) {
            throw new ServletException(gsse);
        } 
        catch (final PrivilegedActionException pae) {
            throw new ServletException(pae);
        } 
        catch (final FileNotFoundException fnfe) {
            throw new ServletException(fnfe);
        } 
        catch (final URISyntaxException uri) {
            throw new ServletException(uri);
        }
    }


    @Override
    public void destroy() {
        if (null != this.authenticator) {
            this.authenticator.dispose();
            this.authenticator = null;
        }
    }

    
    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException, IOException {

        final HttpServletRequest httpRequest = req;
        final SpnegoHttpServletResponse spnegoResponse = new SpnegoHttpServletResponse( resp );

        // client/caller principal
        final SpnegoPrincipal principal;
        try {
            principal = this.authenticator.authenticate( httpRequest, spnegoResponse );
        }
        catch (GSSException gsse) {
            log.info( "HTTP Authorization Header=" + httpRequest.getHeader( Constants.AUTHZ_HEADER ) );
            throw new ServletException( gsse );
        }

        // context/auth loop not yet complete
        if (spnegoResponse.isStatusSet()) {
            return;
        }

        // assert
        if (null == principal) {
            log.info( "Principal was null." );
            spnegoResponse.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, true );
            return;
        }
        else {
            log.info( "principal=" + principal );
            //spnegoResponse.sendRedirect( "http://google.de" );

//        chain.doFilter( new SpnegoHttpServletRequest( httpRequest, principal ), response );
        }
    }
    
    
}
