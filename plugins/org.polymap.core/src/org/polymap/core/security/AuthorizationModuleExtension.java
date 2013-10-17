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
package org.polymap.core.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AuthorizationModuleExtension {

    private static Log log = LogFactory.getLog( AuthorizationModuleExtension.class );
    
    public static final String          ID = "org.polymap.core.security.AuthorizationModule";
    
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
    
}
