/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.service.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.cache.EvictionAware;
import org.polymap.core.runtime.cache.EvictionListener;

/**
 * On-demand initialize (and shutdown) a given delegate servlet.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LazyLoadingServlet<T extends HttpServlet>
        extends HttpServlet
        implements EvictionListener {

    private static Log log = LogFactory.getLog( LazyLoadingServlet.class );
    
    private T                   delegate;
    
    private CachedLazyInit      lowMemIndicator = new CachedLazyInit( 1000*1024 );
    
    /** True if the delegate is initialized and running. */
    private boolean             started;

    private boolean             destroyOnDemand;

    
    /**
     * 
     * @param delegate The delegate servlet to work with.
     * @param destroyOnDemand True indicates that the delegate can be destroyed (and
     *        later re-initialized) if memory pressure is high.
     */
    public LazyLoadingServlet( T delegate, boolean destroyOnDemand ) {
        assert delegate != null;
        this.delegate = delegate;
        this.destroyOnDemand = destroyOnDemand;
    }

    
    public T getDelegate() {
        return delegate;
    }

    
    @Override
    public void destroy() {
        assert delegate != null;
        if (started) {
            delegate.destroy();
        }
        delegate = null;
    }

    
    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    // init delegate
                    delegate.init( getServletConfig() );
                    started = true;
         
                    // init lowMemoryIndicator
                    if (destroyOnDemand) {
                        lowMemIndicator.get( new Supplier<EvictionAware>() {
                            public EvictionAware get() {
                                return new EvictionAware() {
                                    public EvictionListener newListener() {
                                        return LazyLoadingServlet.this;
                                    }
                                };
                            }
                        });
                    }
                }
            }
        }
        delegate.service( req, resp );
    }

    
    //@Override
    public void onEviction( Object key ) {
        if (started) {
            synchronized (this) {
                if (started) {
                    log.info( "Shutting down service: " + CorePlugin.servletAlias( this ) );
                    delegate.destroy();
                    started = false;
                }
            }
        }
    }
    
}
