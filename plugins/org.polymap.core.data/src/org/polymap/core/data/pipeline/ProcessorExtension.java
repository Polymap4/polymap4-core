/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.pipeline;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;

/**
 * Provides access the data of an extension of extension point
 * <code>org.polymap.core.data.pipeline.processors</code>.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProcessorExtension {

    public static final String          EXTENSION_POINT_NAME = "pipeline.processors";

    /**
     * Return all currently known extensions.
     */
    public static List<ProcessorExtension> all() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        return Arrays.stream( elms ).map( elm -> new ProcessorExtension( elm ) ).collect( Collectors.toList() );
    }

    
    public static Optional<ProcessorExtension> forType( String processorClassname ) {
        return all().stream()
                .filter( ext -> ext.getClassname().equals( processorClassname ) )
                .findAny();
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement       ext;

    private Lazy<Class<? extends PipelineProcessor>> type = new LockedLazyInit( () -> newProcessor().getClass() );
    
    
    public ProcessorExtension( IConfigurationElement ext ) {
        this.ext = ext;
    }
    
    public String getId() {
        return ext.getAttribute( "id" );
    }

    public String getName() {
        return ext.getAttribute( "name" );
    }
    
    public String getClassname() {
        return ext.getAttribute( "class" );
    }
    
    public Optional<String> getDescription() {
        return Optional.ofNullable( ext.getAttribute( "description" ) );
    }
    
//    public boolean isTerminal() {
//        return ext.getAttribute( "isTerminal" ).equalsIgnoreCase( "true" );
//    }

// does not seem to load through proper ClassLoader
//    public Class getProcessorClass() 
//    throws InvalidRegistryObjectException, ClassNotFoundException {
//        return (Class<? extends PipelineProcessor>)
//                Thread.currentThread().getContextClassLoader().loadClass( ext.getAttribute( "class" ) );
//    }
    
    public PipelineProcessor newProcessor() throws RuntimeException {
        try {
            return (PipelineProcessor)ext.createExecutableExtension( "class" );
        }
        catch (Exception e) {
            throw new RuntimeException( "Error creating new processor for extension: " + getId(), e );
        }
    }

    public Class<? extends PipelineProcessor> getProcessorType() {
        return type.get();
    }
    
}
