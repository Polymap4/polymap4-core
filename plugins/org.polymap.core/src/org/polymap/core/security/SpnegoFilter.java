/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.polymap.core.security.spnego.Base64;
import org.polymap.core.security.spnego.SpnegoPrincipal;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class SpnegoFilter implements Filter{
    private static Log log = LogFactory.getLog( SpnegoFilter.class );

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init( FilterConfig filterConfig ) throws ServletException {
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String header = httpRequest.getHeader( "Authorization" );

        /**
         * Guard clause to check for Negotiate header.
         * 
         * If the server receives a request for an access-protected object, and
         * if an acceptable Authorization header has not been sent, the server
         * responds with a "401 Unauthorized" status code, and a "WWW-Authenticate:"
         * header as per the framework described in [RFC 2616].  The initial
         * WWW-Authenticate header will not carry any gssapi-data.
         */
        if ( header == null || header.length() < 10 || !header.startsWith( "Negotiate " ) )
        {
            httpResponse.setHeader( "WWW-Authenticate", "Negotiate" );
            httpResponse.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            log.debug( "Proper authorization header not found, returning challenge." );
            return;
        }

        /**
         * A client may initiate a connection to the server with an
         * "Authorization" header containing the initial token for the server.
         * This form will bypass the initial 401 error from the server when the
         * client knows that the server will accept the Negotiate HTTP
         * authentication type.
         */
        log.debug( "Authorization header found, continuing negotiation." );


        try (FileOutputStream fileWriter = new FileOutputStream(new File(System.getProperty( "user.home" ) + "/mapzone_token.txt"))) {
            fileWriter.write( header.substring( 10 ).getBytes() );
        }
        
        httpResponse.setStatus( HttpServletResponse.SC_OK );

//        simple( httpRequest, httpResponse );
//        complex( httpResponse, header );     
    }

    private void complex( HttpServletResponse httpResponse, String header ) throws ServletException {
        /**
         * The data following the word Negotiate is the GSS-API data to process. 
         */
        byte gssapiData[] = new byte[0];
        try {
            gssapiData = com.sun.org.apache.xml.internal.security.utils.Base64.decode( header.substring( 10 ).getBytes() );
        }
        catch (Base64DecodingException e) {
            e.printStackTrace();
        }
        
        /**
         * Guard clause to check for the unsupported NTLM authentication mechanism.
         */
        if ( isNtlmMechanism( gssapiData ) )
        {
            log.warn( "Got request for unsupported NTLM mechanism, aborting negotiation." );
            return;
        }

        /**
         * The server attempts to establish a security context.  Establishment may result in
         * tokens that the server must return to the client.  Tokens are BASE-64 encoded
         * GSS-API data.
         */
        GSSContext context = null;
        String outToken = null;

        try
        {
            GSSManager manager = GSSManager.getInstance();

            Oid spnegoOid = new Oid( "1.3.6.1.5.5.2" );
            GSSCredential serverCreds = manager.createCredential( null, GSSCredential.DEFAULT_LIFETIME, spnegoOid,
                GSSCredential.ACCEPT_ONLY );

            context = manager.createContext( serverCreds );

            byte tokenBytes[] = context.acceptSecContext( gssapiData, 0, gssapiData.length );
            outToken = new String( Base64.encode( tokenBytes ) );
        }
        catch ( GSSException gsse )
        {
            gsse.printStackTrace();
            log.error( "GSSException:       " + gsse.getMessage() );
            log.error( "GSSException major: " + gsse.getMajorString() );
            log.error( "GSSException minor: " + gsse.getMinorString() );
            throw new ServletException( gsse );
        }

        /**
         * If the context is established, we can attempt to retrieve the name of the "context
         * initiator."  In the case of the Kerberos mechanism, the context initiator is the
         * Kerberos principal of the client.  Additionally, the client may be delegating
         * credentials.
         */
        if ( context != null && context.isEstablished() )
        {
            log.debug( "Context established, attempting Kerberos principal retrieval." );

            try
            {
                Subject subject = new Subject();
                GSSName clientGSSName = context.getSrcName();
                Principal clientPrincipal = new KerberosPrincipal( clientGSSName.toString() );
                subject.getPrincipals().add( clientPrincipal );
                log.info( "Got client Kerberos principal: " + clientGSSName );

                if ( context.getCredDelegState() )
                {
                    GSSCredential delegateCredential = context.getDelegCred();
                    GSSName delegateGSSName = delegateCredential.getName();
                    Principal delegatePrincipal = new KerberosPrincipal( delegateGSSName.toString() );
                    subject.getPrincipals().add( delegatePrincipal );
                    subject.getPrivateCredentials().add( delegateCredential );
                    log.info( "Got delegated Kerberos principal: " + delegateGSSName );
                }

                // TODO
                //getSpnegoSession().setUser( clientGSSName.toString() );

                /**
                 * A status code 200 status response can also carry a "WWW-Authenticate"
                 * response header containing the final leg of an authentication.  In
                 * this case, the gssapi-data will be present.
                 */
                if ( outToken != null && outToken.length() > 0 )
                {
                    httpResponse.setHeader( "WWW-Authenticate", "Negotiate " + outToken.getBytes() );
                    httpResponse.setStatus( HttpServletResponse.SC_OK );
                    log.debug( "Returning final authentication data to client to complete context." );
                    return;
                }
            }
            catch ( GSSException gsse )
            {
                log.error( "GSSException:       " + gsse.getMessage() );
                log.error( "GSSException major: " + gsse.getMajorString() );
                log.error( "GSSException minor: " + gsse.getMinorString() );

                httpResponse.addHeader( "Client-Warning", gsse.getMessage() );
                httpResponse.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            }
        }
        else
        {
            /**
             * Any returned code other than a success 2xx code represents an
             * authentication error.  If a 401 containing a "WWW-Authenticate"
             * header with "Negotiate" and gssapi-data is returned from the server,
             * it is a continuation of the authentication request.
             */
            if ( outToken != null && outToken.length() > 0 )
            {
                httpResponse.setHeader( "WWW-Authenticate", "Negotiate " + outToken.getBytes() );
                httpResponse.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                log.debug( "Additional authentication processing required, returning token." );
                return;
            }
            else
            {
                httpResponse.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                log.warn( "Kerberos negotiation failed." );
            }
        }

        log.debug( "Negotiation completed." );
    }
    
    /**
     * Constant for the header lead for the unsupported NTLM mechanism.
     */
    private static final byte NTLMSSP[] =
        { ( byte ) 0x4E, ( byte ) 0x54, ( byte ) 0x4C, ( byte ) 0x4D, ( byte ) 0x53, ( byte ) 0x53, ( byte ) 0x50 };

    protected boolean isNtlmMechanism( byte[] gssapiData )
    {
        byte leadingBytes[] = new byte[7];
        System.arraycopy( gssapiData, 0, leadingBytes, 0, 7 );
        if ( Arrays.equals( leadingBytes, NTLMSSP ) )
        {
            return true;
        }

        return false;
    }

    private void simple( HttpServletRequest httpRequest, HttpServletResponse httpResponse ) {
        String header = httpRequest.getHeader( "Authorization" );
        
        Principal principal = httpRequest.getUserPrincipal();
        if(principal instanceof SpnegoPrincipal) {
            SpnegoPrincipal spnegoPrincipal = (SpnegoPrincipal)principal;
            GSSCredential credentials = spnegoPrincipal.getDelegatedCredential();
//            if(credentials != null) {
//                response.getWriter().println("Found group SIDs: " + Arrays.toString(groupSIDs));
//            } else {
//                response.getWriter().println("No logon info available for principal.");
//            }
        } else {
            sendUnauthorized(httpResponse, true);
        }
    }
    
    private void sendUnauthorized(final HttpServletResponse response, final boolean close) {
        try {
            if (close) {
                response.setHeader("Connection", "close");
            } else {
                response.setHeader("Connection", "keep-alive");
            }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            response.flushBuffer();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }    

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }
}
