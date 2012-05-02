/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 * $Id: $
 */
package org.polymap.rhei.filter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.rhei.RheiPlugin;

/**
 * Provides access to the extensions of extension point
 * {@link #EXTENSION_POINT_NAME}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
class FilterProviderExtension {

    public static final String          EXTENSION_POINT_NAME = "filter.filterProviders";


    public static FilterProviderExtension[] allExtensions() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( RheiPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        
        FilterProviderExtension[] result = new FilterProviderExtension[ elms.length ];
        for (int i=0; i<elms.length; i++) {
            result[i] = new FilterProviderExtension( elms[i] );
        }
        return result;
    }
    

    // instance *******************************************
    
    private IConfigurationElement       ext;

    
    public FilterProviderExtension( IConfigurationElement ext ) {
        this.ext = ext;
    }
    
    public String getId() {
        return ext.getAttribute( "id" );
    }

    public String getName() {
        return ext.getAttribute( "name" );
    }
    
    public boolean isStandard() {
        String attr = ext.getAttribute( "isStandard" );
        return attr != null && attr.equalsIgnoreCase( "true" );
    }

    public IFilterProvider newFilterProvider()
    throws CoreException {
        return (IFilterProvider)ext.createExecutableExtension( "class" );
    }
    
}
