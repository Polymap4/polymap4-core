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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.FluentIterable;

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

    private static final Log log = LogFactory.getLog( OperationConcernExtension.class );

    static final String         EXTENSION_POINT_ID = CorePlugin.PLUGIN_ID + ".operation.concerns";
    
    public static Iterable<OperationConcernExtension> all() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( EXTENSION_POINT_ID );
        log.info( "Operation concern extensions found: " + elms.length ); //$NON-NLS-1$
        return FluentIterable.of( elms ).transform( elm -> new OperationConcernExtension( elm ) );
    }

    
    // instance *******************************************

    private IConfigurationElement       elm;
    

    OperationConcernExtension( IConfigurationElement elm ) {
        this.elm = elm;
        log.info( "    " + elm.getAttribute( "class" ) );
    }

    public IOperationConcernFactory newInstance() {
        try {
            return (IOperationConcernFactory)elm.createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }
    
}
