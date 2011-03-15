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

package org.polymap.core.style;

import java.io.IOException;
import java.io.PrintWriter;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.styling.Style;


import net.refractions.udig.catalog.ID;

public class SLDDownloadServlet
        extends HttpServlet {

    private static final long serialVersionUID = 8340634060357577367L;
	
    private static final Log log = LogFactory.getLog(SLDDownloadServlet.class);
    
    public SLDDownloadServlet() {
    	
    }
    
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {

        String path_info = request.getPathInfo();

        path_info = path_info.replace( "/", "" );

        log.info("SLD Download requested for" + path_info);
        
        response.setContentType( "application/sld" );
        PrintWriter out = response.getWriter();

        ID id = StylePlugin.getDefault().getIDbyServletPath(path_info);
        
        IStyle style= StylePlugin.getStyleCatalog().getById(id,null );
        style.resolve(Style.class, null);
        out.write(style.createSLD(new NullProgressMonitor()));

    }

}
