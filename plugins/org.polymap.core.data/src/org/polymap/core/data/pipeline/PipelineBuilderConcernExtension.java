/* 
 * polymap.org
 * Copyright 2011-2018, Polymap GmbH. All rights reserved.
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
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.FluentIterable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;

/**
 * Provides access the data of an extension of extension point
 * <code>org.polymap.core.data.pipeline.builderConcerns</code>.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public final class PipelineBuilderConcernExtension {

    public static final String          EXTENSION_POINT_NAME = "pipeline.builderConcerns";

    /**
     * 
     */
    public static Iterable<PipelineBuilderConcernExtension> all() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        
        return FluentIterable.from( Arrays.asList( elms ) )
                .transform( elm -> new PipelineBuilderConcernExtension( elm ) );
    }

    /**
     * 
     */
    public static PipelineBuilderConcernExtension forExtensionId( String id ) {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        
        List<PipelineBuilderConcernExtension> result = new ArrayList( elms.length );
        for (int i=0; i<elms.length; i++) {
            PipelineBuilderConcernExtension ext = new PipelineBuilderConcernExtension( elms[i] );
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

    
    public PipelineBuilderConcernExtension( IConfigurationElement ext ) {
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
    
    public PipelineBuilderConcern newInstance() {
        try {
            return (PipelineBuilderConcern)ext.createExecutableExtension( "class" );
        }
        catch (Exception e) {
            throw new RuntimeException( "Error creating new processor for extension: " + getId(), e );
        }
    }

}
