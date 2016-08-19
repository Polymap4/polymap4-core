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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.session.DefaultSessionContextProvider;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.security.SecurityContext;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ServiceContext2 {

    private static Log log = LogFactory.getLog( ServiceContext2.class );

    private static final DefaultSessionContextProvider contextProvider;
    
    static {
        contextProvider = new DefaultSessionContextProvider();
        SessionContext.addProvider( contextProvider );
    }
    
    /**
     * 
     */
    @FunctionalInterface
    interface Task<E extends Exception> {
        public void call() throws E;
    }

    
    // interface ******************************************
    
    protected String                serviceId;

    protected String                sessionKey;


    public ServiceContext2( String serviceId ) {
        this.serviceId = serviceId;
        this.sessionKey = "GeoServer:" + serviceId + "-" + hashCode();
//        try {
////            log.info( "mapping sessionKey: " + sessionKey );
//            boolean mapped = contextProvider.mapContext( sessionKey, true );
//            assert mapped : "sessionKey already mapped: " + sessionKey;
//            Polymap.instance().addPrincipal( new ServicesPlugin.AdminPrincipal() );
//        }
//        finally {
//            contextProvider.unmapContext();
//        }
    }


    public void destroy() {
        assert sessionKey != null : "Session context already destroyed";
        try {
            contextProvider.destroyContext( sessionKey );
        }
        finally {
            sessionKey = null;
        }
    }
    
    
    public <E extends Exception> void execute( Task<E> task ) throws E {
        // XXX no GeoServerClassLoader; just one instance per JVM
//        assert context.cl != null;
//        ClassLoader orig = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader( context.cl );
//        assert Thread.currentThread().getContextClassLoader() == context.cl;

        SessionContext current = SessionContext.current();
        assert current == null : "Thread already mapped to a SessionContext: " + current.getSessionKey();
        try {
            boolean mapped = contextProvider.mapContext( sessionKey, true );
            log.info( "SessionContext: " + SessionContext.current() );
            assert mapped : "Thread already mapped to a SessionContext: " + SessionContext.current().getSessionKey();
      
            SecurityContext securityContext = SecurityContext.instance();
            if (!securityContext.isLoggedIn()) {
                // XXX this user is used to authenticate upstream mapzone services
                securityContext.loginTrusted( "admin" );              
            }

            task.call();
        }
        finally {
//          Thread.currentThread().setContextClassLoader( orig );
            contextProvider.unmapContext();
        }
    }

}
