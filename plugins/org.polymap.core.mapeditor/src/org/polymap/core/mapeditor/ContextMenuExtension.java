/* 
 * polymap.org
 * Copyright 2011-2014, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;

import org.polymap.rhei.batik.BatikPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContextMenuExtension 
        implements IContextMenuProvider {

    private static Log log = LogFactory.getLog( ContextMenuExtension.class );
    
    public static final String          EXTENSION_POINT_NAME = "mapContextMenu";

    
    public static ContextMenuExtension[] all() {
        // find all extensions
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( BatikPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );

        // check all providers
        ContextMenuExtension[] result = new ContextMenuExtension[ elms.length ];
        int i = 0;
        for (IConfigurationElement elm : elms) {
            result[i++] = new ContextMenuExtension( elm );
        }
        return result;
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement       elm;

    
    public ContextMenuExtension( IConfigurationElement elm ) {
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
        return path != null ? DataPlugin.imageDescriptorFromPlugin( DataPlugin.PLUGIN_ID, path ) : null;
    }
    
    public String getDescription() {
        return elm.getAttribute( "description" );
    }
    
    public String getTooltip() {
        return elm.getAttribute( "tooltip" );
    }
    
    @Override
    public IContextMenuContribution createContribution() {
        try {
            return (IContextMenuContribution)elm.createExecutableExtension( "class" );
        }
        catch (Exception e) {
            throw new RuntimeException( "Error creating new provider for extension: " + getId(), e );
        }
    }
   
}
