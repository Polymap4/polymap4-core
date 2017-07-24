/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.data.process;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;

/**
 * 
 *
 * @author Falko Bräutigam
 */
class ModuleProviderExtension {

    public static final String          EXTENSION_POINT_NAME = "process.providers";

    
    public static ModuleProviderExtension[] allExtensions() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        
        ModuleProviderExtension[] result = new ModuleProviderExtension[ elms.length ];
        for (int i=0; i<elms.length; i++) {
            result[i] = new ModuleProviderExtension( elms[i] );
        }
        return result;
    }

    
    // instance *******************************************
    
    private IConfigurationElement       ext;

    
    public ModuleProviderExtension( IConfigurationElement ext ) {
        this.ext = ext;
    }
    
    public ModuleProvider newProvider() throws CoreException {
        try {
            return (ModuleProvider)ext.createExecutableExtension( "class" );
        }
        catch (Exception e) {
            throw new RuntimeException( "Error creating new processor for extension: " + ext, e );
        }
    }

}
