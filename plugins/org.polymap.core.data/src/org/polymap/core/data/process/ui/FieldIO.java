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
package org.polymap.core.data.process.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.process.FieldInfo;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class FieldIO {
    
    private static final Log log = LogFactory.getLog( FieldIO.class );
    
    protected static final String EXTENSION_POINT_NAME = "process.fieldIO";

    /**
     * 
     *
     * @return
     */
    protected static final List<FieldIO> available() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );

        return Arrays.stream( elms )
                .map( elm -> {
                    try {
                        return (FieldIO)elm.createExecutableExtension( "class" );
                    }
                    catch (CoreException e) {
                        throw new RuntimeException( e );
                    }
                })
                .collect( Collectors.toList() );
    }

    
    public static FieldIO[] forField( FieldViewerSite site ) {
        List<FieldIO> result = new ArrayList();
        for (FieldIO editor : available()) {
            try {
                if (editor.init( site )) {
                    result.add( editor );
                }
            }
            catch (Exception e) {
                log.warn( "", e);
                //throw new RuntimeException( e );
            }
        }
        if (result.isEmpty()) {
            NotSupportedSupplier notSupported = new NotSupportedSupplier();
            notSupported.init( site );
            result.add( notSupported );
        }
        return result.toArray( new FieldIO[result.size()] );
    }
    

    // instance *******************************************
    
    protected FieldViewerSite       site;
    
    
    /**
     * The human readable label/title of this field IO. This is used in a combo box
     * in the UI.
     */
    public abstract String label();

    /**
     * Checks if this field can handle the given {@link FieldInfo#type}.
     *
     * @return True if this supplier can handle the {@link FieldInfo field} in the
     *         given site.
     */
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        this.site = site;
        return true;
    }
    
    /**
     * Creates the UI of this field.
     * <p/>
     * This method might be called several times. This happens when the user switches
     * between options. Implementations have to check and release resources from
     * previous invocations.
     */
    public abstract void createContents( Composite parent );
    
}
