/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import java.net.URL;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;

import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.runtime.session.SessionSingleton;

/**
 * The security context of the a {@link SessionContext}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SecurityContext
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( SecurityContext.class );
    
    /**
     * The default name of dialog/UI login.
     */
    public static final String      APPLICATION_CONFIG_NAME = "Application";
    
    /**
     * The default name of the service login config without UI dialog.
     * @see ServicesCallbackHandler
     */
    public static final String      SERVICES_CONFIG_NAME = "Services";
    
    private static List<Supplier<Configuration>>    configSuppliers = new ArrayList();

    /**
     * 
     */
    public static interface Configuration {
        
        /**
         * The JAAS login config name to be used to
         * {@link LoginContextFactory#createContext(String, URL)}.
         * <p/>
         * This should be {@link SecurityContext#APPLICATION_CONFIG_NAME} or
         * {@link SecurityContext#SERVICES_CONFIG_NAME}. For other config names a
         * corresponding login callbackHandler and mapping has to be declared via
         * extension.
         */
        public String getConfigName();
        
        /**
         * The JAAS login config file to be used to
         * {@link LoginContextFactory#createContext(String, URL)}
         */
        public URL getConfigFile();
        
    }

    /**
     * 
     *
     * @see StandardConfiguration
     */
    public static void registerConfiguration( Supplier<Configuration> config ) {
        configSuppliers.add( config );
    }
    
    protected Configuration findConfiguration() {
        return configSuppliers.stream()
                .map( supplier -> supplier.get() )
                .filter( config -> config != null )
                .findFirst()
                .orElseThrow( () -> new RuntimeException( "No SecurityContext.Configuration found. See SecurityContext#register()." ) );
    }
    
    /**
     * The instance for the current {@link SessionContext}. 
     */
    public static SecurityContext instance() {
        return instance( SecurityContext.class );
    }
    
    
    // instance *******************************************
    
    private ILoginContext           sc;

    private Subject                 subject;

    private Set<Principal>          principals;

    private Principal               user;
    
    
    protected SecurityContext() {
        Configuration config = findConfiguration();
        sc = LoginContextFactory.createContext( config.getConfigName(), config.getConfigFile() );        
    }
    
    
    /**
     * Logging with dialog/UI provided credentials using the JAAS config named
     * {@link #APPLICATION_CONFIG_NAME}.
     * 
     * @see #APPLICATION_CONFIG_NAME
     */
    public boolean login( int maxAttempts ) {
        if (isLoggedIn()) {
            throw new IllegalStateException( "Already logged in for this SessionContext." );
        }
        for (int i=0; i<maxAttempts; i++) {
            if (tryLogin()) {
                return true;
            }
        }
        return false;
    }

    
    /**
     * Logging with given credentials using the JAAS config named
     * {@link #SERVICES_CONFIG_NAME}.
     */
    public boolean login( String username, String passwd ) {
        ServicesCallbackHandler.challenge( username, passwd );
        return tryLogin();
    }

    
    public boolean tryLogin() {
        if (isLoggedIn()) {
            throw new IllegalStateException( "Already logged in for this SessionContext." );
        }
        try {
            sc.login();

            subject = sc.getSubject();
            principals = new HashSet( subject.getPrincipals() );

            // find user
            user = principals.stream()
                    .filter( p -> p instanceof UserPrincipal ).findAny()
                    .orElseThrow( () -> new LoginException( "Es wurde kein Nutzer in der Konfiguration gefunden" ) );

            // allow to access the instance directly via current session (find user for example)
            SessionContext.current().setAttribute( "user", user );

            // add roles of user to principals
            Set<AuthorizationModule> authModules = subject.getPrivateCredentials( AuthorizationModule.class );
            if (authModules.size() != 1) {
                throw new RuntimeException( "No AuthorizationModule specified. Is jaas_config.txt correct?" );
            }
            principals.addAll( authModules.iterator().next().rolesOf( subject ) );
            return true;
        }
// org.eclipse.equinox.internal.security.auth.SecurityContext wraps FaildLoginException        
//        catch (FailedLoginException e) {
//            return false;
//        }
        catch (LoginException e) {
            // no matter if wrong username/password or technical problem
            // login failed
            return false;
        }
    }


    public UserPrincipal loginTrusted( String username ) {
        if (isLoggedIn()) {
            throw new IllegalStateException( "Already logged in for this SessionContext." );
        }
        user = new UserPrincipal( username );
        return (UserPrincipal)user;
    }

    
//    public boolean validatePassword( String username, String passwd ) {
//        try {
//            ServicesCallbackHandler.challenge( username, passwd );
//            sc.login();
//            return true;
//        }
//        catch (LoginException e) {
//            return false;
//        }
//    }

    
    public void logout() {
        if (subject != null) {
            try {
                sc.logout();
                subject = null;
                principals = null;
                user = null;
            }
            catch (LoginException e) {
                log.warn( "Login error: " + e.getLocalizedMessage(), e );
            }
        }
    }
    
    
    public void addPrincipal( Principal principal ) {
        checkLoggedIn();
        principals.add( principal );
        if (principal instanceof UserPrincipal) {
            user = (UserPrincipal)principal;
        }
    }
    
    
    public Set<Principal> getPrincipals() {
        checkLoggedIn();
        return principals;
    }

    
    public Principal getUser() {
        checkLoggedIn();
        return user;
    }


    public Subject getSubject() {
        checkLoggedIn();
        return subject;    
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    protected void checkLoggedIn() {
        if (!isLoggedIn()) {
            throw new IllegalStateException( "Not logged in in this SessionContext." );
        }
    }
    
}
