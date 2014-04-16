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
package org.polymap.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Redirects each and every request to the Workbench (polymap). 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WorkbenchRedirectServlet
        extends HttpServlet {

    private static Log log = LogFactory.getLog( WorkbenchRedirectServlet.class );

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException, IOException {
        log.info( "REDIRECT: request=" + req.getPathInfo() );
        resp.sendRedirect( "polymap" );
    }

}
