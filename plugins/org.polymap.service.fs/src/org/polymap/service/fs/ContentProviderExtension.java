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
package org.polymap.service.fs;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.service.fs.spi.IContentProvider;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ContentProviderExtension {

    private static Log log = LogFactory.getLog( ContentProviderExtension.class );

    public static final String      EXTENSION_POINT_NAME = "contentProviders";

    private static ContentProviderExtension[]  extensions;
    
    
    static {
        BundleContext bundleContext = FsPlugin.getDefault().getBundle().getBundleContext();
        bundleContext.addBundleListener( new BundleListener() {
            
            public void bundleChanged( BundleEvent event ) {
                // force reloading extensions after bundle has changed
                extensions = null;
            }
        });
    }
    

    public static ContentProviderExtension[] all() {
        if (extensions == null) {
            // no synch, double init is ok
            
            IConfigurationElement[] elms = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor( FsPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );

            extensions = new ContentProviderExtension[ elms.length ];
            for (int i=0; i<elms.length; i++) {
                extensions[i] = new ContentProviderExtension( elms[i] );
            }
        }
        return extensions;
    }

    
    // instance *******************************************
    
    private IConfigurationElement       ext;
    
    
    public ContentProviderExtension( IConfigurationElement ext ) {
        this.ext = ext;
    }
    
    public String getId() {
        return ext.getAttribute( "id" );
    }

    public String getName() {
        return ext.getAttribute( "name" );
    }

    public IContentProvider newProvider() {
        try {
            return (IContentProvider)ext.createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            log.warn( e.getLocalizedMessage(), e );
            throw new RuntimeException(  e );
        }
    }

}
