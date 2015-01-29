/* 
 * polymap.org
 * Copyright (C) 2013-2014, Falko Bräutigam. All rights reserved.
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
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * Creates and initializes {@link AuthorizationModule} instances defined via
 * extension point {@value #ID}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AuthorizationModuleExtension {

    private static Log log = LogFactory.getLog( AuthorizationModuleExtension.class );
    
    public static final String          ID = "org.polymap.core.security.AuthorizationModule";
    
    public static final String          OPTIONS_KEY = "authorizationExtensionId";
    
    
    public static List<AuthorizationModuleExtension> all() {
        IConfigurationElement[] exts = Platform.getExtensionRegistry().getConfigurationElementsFor( ID );
        List<AuthorizationModuleExtension> result = new ArrayList();
        for (IConfigurationElement ext : exts) {
            result.add( new AuthorizationModuleExtension( ext ) );
        }
        return result;
    }
    
    
    public static AuthorizationModuleExtension forId( final String id ) {
        assert id != null;
        return Iterables.find( all(), new Predicate<AuthorizationModuleExtension>() {
            public boolean apply( AuthorizationModuleExtension input ) {
                return id.equals( input.ext.getDeclaringExtension().getUniqueIdentifier() );
            }
        }, null );
    }
    
    
    public static AuthorizationModuleExtension forOptions( Map<String,?> options ) {
        for (Object elm : options.entrySet()) {
            Map.Entry<String,String> option = (Map.Entry)elm;
            if (option.getKey().equals( OPTIONS_KEY )) {
                return forId( option.getValue() );
            }
        }
        return null;
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement       ext;

    
    protected AuthorizationModuleExtension( IConfigurationElement ext ) {
        this.ext = ext;
    }
    
    
    public AuthorizationModule createClass() {
        try {
            return (AuthorizationModule)ext.createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public AuthorizationModule initialize( LoginModule loginModule, Subject subject, 
            CallbackHandler callbackHandler, Map<String,?> sharedState, Map<String,?> options ) {
        AuthorizationModule result = createClass();
        result.init( loginModule );
        result.initialize( subject, callbackHandler, sharedState, options );
        return result;
    }
    
}
