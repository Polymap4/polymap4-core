/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.pipeline;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;

/**
 * Provides access the data of an extension of extension point
 * <code>org.polymap.core.data.pipeline.listeners</code>.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class PipelineListenerExtension {

    public static final String          EXTENSION_POINT_NAME = "pipeline.listeners";

    public static PipelineListenerExtension[] allExtensions() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        
        PipelineListenerExtension[] result = new PipelineListenerExtension[ elms.length ];
        for (int i=0; i<elms.length; i++) {
            result[i] = new PipelineListenerExtension( elms[i] );
        }
        return result;
    }
    
    public static PipelineListenerExtension forExtensionId( String id ) {
        IConfigurationElement[] elms = Platform.getExtensionRegistry().getConfigurationElementsFor(
                DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        
        List<PipelineListenerExtension> result = new ArrayList( elms.length );
        for (int i=0; i<elms.length; i++) {
            PipelineListenerExtension ext = new PipelineListenerExtension( elms[i] );
            if (ext.getId().equals( id )) {
                result.add( ext );
            }
        }

        if (result.size() > 1) {
            throw new IllegalStateException( "More than 1 extension: " + elms );
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException( "No extension for id: " + id );
        }
        return !result.isEmpty() ? result.get( 0 ) : null;
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement       ext;

    
    public PipelineListenerExtension( IConfigurationElement ext ) {
        this.ext = ext;
    }
    
    public String getId() {
        return ext.getAttribute( "id" );
    }

    public String getName() {
        return ext.getAttribute( "name" );
    }
    
    public String getDescription() {
        return ext.getAttribute( "description" );
    }
    
    public boolean isTerminal() {
        return ext.getAttribute( "isTerminal" ).equalsIgnoreCase( "true" );
    }

    public IPipelineIncubationListener newListener()
    throws CoreException {
        try {
            return (IPipelineIncubationListener)ext.createExecutableExtension( "class" );
        }
        catch (Exception e) {
            throw new RuntimeException( "Error creating new processor for extension: " + getId(), e );
        }
    }

}
