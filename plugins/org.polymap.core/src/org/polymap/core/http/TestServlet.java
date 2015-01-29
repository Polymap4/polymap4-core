/* 
 * polymap.org
 * Copyright 2009-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.core.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a> 
 *         <li>17.10.2009: created</li>
 */
public class TestServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( TestServlet.class );

    
    public TestServlet() {
        super();
    }


    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        log.info( "..." );
        
        System.out.println( "Request: " + request.getQueryString() );
//        Map kvp = KvpUtils.parseKvpSet( request.getQueryString() );
//        for (Object elm : kvp.entrySet()) {
//            Map.Entry entry = (Map.Entry)elm;
//            System.out.println( "    key= " + entry.getKey() + ", value= " + entry.getValue() );
//        }
        
        response.setContentType( "text/html" );
        response.setStatus( HttpServletResponse.SC_OK );
        PrintWriter out = response.getWriter();
        out.println( "<h1>Hello TestServlet</h1>" );
        out.println( "session= " + request.getSession( true ).getId() + "<br/>" );
//        ServletRuntimeContext context = HttpServerFactory.getServer().getServletContext( this );
//        out.println( "context= " + context + "<br/>" );
//        out.println( "display= " + PlatformUI.get + "<br/>" );
    }

}
