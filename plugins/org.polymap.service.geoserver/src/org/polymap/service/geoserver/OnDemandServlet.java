/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.service.geoserver;

import java.util.Enumeration;
import java.util.function.Supplier;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class OnDemandServlet
        extends HttpServlet {

    private static Log log = LogFactory.getLog( OnDemandServlet.class );
    
    private Lazy<HttpServlet>       delegate;
    
    private ServletConfig           config;

    
    public OnDemandServlet( Supplier<? extends HttpServlet> supplier ) {
        this.delegate = new LockedLazyInit( () -> {
            try {
                HttpServlet result = supplier.get();
                result.init( config );
                return result;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        });
    }

    @Override
    public void init( @SuppressWarnings( "hiding" ) ServletConfig config ) throws ServletException {
        this.config = config;
    }

    public void destroyDelegate() {
        if (delegate.isInitialized()) {
            // clear before destroy so that new request get a new delegate
            HttpServlet local = delegate.get();
            delegate.clear();
            local.destroy();
        }
    }

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        delegate.get().service( req, resp );
    }

    @Override
    public void destroy() {
        delegate.get().destroy();
        delegate = null;
        super.destroy();
    }

    @Override
    public String getInitParameter( String name ) {
        return delegate.get().getInitParameter( name );
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return delegate.get().getInitParameterNames();
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

//    @Override
//    public ServletContext getServletContext() {
//        delegate.get().g
//    }

    @Override
    public String getServletInfo() {
        return delegate.get().getServletInfo();
    }

    @Override
    public void log( String msg ) {
        delegate.get().log( msg );
    }

    @Override
    public void log( String msg, Throwable t ) {
        delegate.get().log( msg, t );
    }

//    @Override
//    public String getServletName() {
//        delegate.get().
//    }

}
