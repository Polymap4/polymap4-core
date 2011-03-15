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
package org.polymap.core.services.http;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServlet;

import org.eclipse.swt.widgets.Display;

import org.polymap.core.project.IMap;

/**
 * The base SPI of a HTTP data service.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class HttpService
        extends HttpServlet {

    protected String            pathSpec;
    
    protected String            url;
    
    protected IMap              map;
    
    
    protected void init(
    		String _pathSpec, IMap _map )
            throws MalformedURLException {
        this.pathSpec = _pathSpec;
        this.map = _map;
        this.url =  pathSpec ;    
    }

    public String getPathSpec() {
        return pathSpec;
    }
    
    public String getURL() {
        return url;    
    }
    
    public IMap getMap() {
        return map;
    }


    /**
     * Provides access to the runtime display of the server.
     *  
     * @return The display, or null if the server was started outside a session.
     */
    protected Display getDisplay() {
//        ServletRuntimeContext src = HttpServerFactory.getServer().getServletContext( this );
//        return src != null ? src.getDisplay() : null;
    	return null;
    }
    
}
