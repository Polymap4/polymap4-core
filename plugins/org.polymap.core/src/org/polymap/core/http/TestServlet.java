/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 *         <li>17.10.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class TestServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( TestServlet.class );

    
    public TestServlet() {
        super();
        log.info( "..." );
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
