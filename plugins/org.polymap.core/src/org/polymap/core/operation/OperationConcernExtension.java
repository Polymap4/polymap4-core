/* 
 * polymap.org
 * Copyright 2010-2012, Falko Bräutigam. All rights reserved.
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
 */
package org.polymap.core.operation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.CorePlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class OperationConcernExtension {

    private static Log log = LogFactory.getLog( OperationConcernExtension.class );

    static final String         EXTENSION_POINT_ID = CorePlugin.PLUGIN_ID + ".operation.concerns";
    
    static List<OperationConcernExtension> extensions = new ArrayList();

    
    static {
        IConfigurationElement[] exts = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( EXTENSION_POINT_ID );
        log.info( "Operation concern extensions found: " + exts.length ); //$NON-NLS-1$
        
        for (IConfigurationElement ext : exts) {
            try {
                extensions.add( new OperationConcernExtension( ext ) );
            }
            catch (CoreException e) {
                log.warn( "Failed to init extension: ", e );
            }
        }
    }

    
    // instance *******************************************

    private IConfigurationElement       elm;
    

    OperationConcernExtension( IConfigurationElement elm ) 
    throws CoreException {
        this.elm = elm;
    }

    public IOperationConcernFactory newFactory() 
    throws CoreException {
        return (IOperationConcernFactory)elm.createExecutableExtension( "class" );
    }
    
}
