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
package org.polymap.core.data.operation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class FeatureOperationExtension {

    private static Log log = LogFactory.getLog( FeatureOperationExtension.class );
    
    public static final String          EXTENSION_POINT_NAME = "featureOperations";

    
    static List<FeatureOperationExtension> all() {
        // find all extensions
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );

        // check all providers
        List<FeatureOperationExtension> result = new ArrayList();
        for (IConfigurationElement elm : elms) {
            result.add( new FeatureOperationExtension( elm ) );
        }
        return result;
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement       elm;

    
    public FeatureOperationExtension( IConfigurationElement elm ) {
        this.elm = elm;
    }
    
    public String getId() {
        return elm.getAttribute( "id" );
    }

    public String getLabel() {
        return elm.getAttribute( "label" );
    }
    
    public ImageDescriptor getIcon() {
        String path = elm.getAttribute( "icon" );
        if (path == null) {
            return null;
        }
        
        String contributor = elm.getDeclaringExtension().getContributor().getName();
//        Bundle bundle = Platform.getBundle( contributor );
//        String pluginId = bundle.getSymbolicName();
        
        ImageRegistry images = DataPlugin.getDefault().getImageRegistry();
        String key = contributor + "." + path;
        ImageDescriptor result = images.getDescriptor( key );
        if (result == null) {
            result = DataPlugin.imageDescriptorFromPlugin( contributor, path );
            images.put( key, result );
        }
        return result;
    }
    
    public String getDescription() {
        return elm.getAttribute( "description" );
    }
    
    public String getTooltip() {
        return elm.getAttribute( "tooltip" );
    }
    
    IFeatureOperation newOperation() {
        try {
            return (IFeatureOperation)elm.createExecutableExtension( "class" );
        }
        catch (Exception e) {
            throw new RuntimeException( "Error creating new processor for extension: " + getId(), e );
        }
    }
   
}
