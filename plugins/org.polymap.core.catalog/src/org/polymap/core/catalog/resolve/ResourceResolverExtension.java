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
package org.polymap.core.catalog.resolve;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.FluentIterable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ResourceResolverExtension {

    private static final Log log = LogFactory.getLog( ResourceResolverExtension.class );
    
    public static final String          ID = "org.polymap.core.catalog.resolverProviders";

    
    public static List<ResourceResolverExtension> all() {
        return FluentIterable.of( Platform.getExtensionRegistry().getConfigurationElementsFor( ID ) )
                .transform( ext -> new ResourceResolverExtension( ext ) )
                .toList();
    }

    
    public static List<ResourceResolverProvider> allProviders() {
        return FluentIterable.from( all() )
                .transform( ext -> ext.createInstance() )
                .toList();
    }
    
    
    public static List<IMetadataResourceResolver> createAllResolvers() {
        return FluentIterable.from( all() )
                .transform( ext -> ext.createInstance().get() )
                .filter( provider -> provider != null )
                .toList();
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement       ext;
    
    
    protected ResourceResolverExtension( IConfigurationElement ext ) {
        this.ext = ext;
    }


    public ResourceResolverProvider createInstance() {
        try {
            return (ResourceResolverProvider)ext.createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }
    
}
