/*
 * polymap.org Copyright 2009, Polymap GmbH, and individual contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 * 
 * $Id: $
 */
package org.polymap.core.mapeditor;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MapEditorPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( MapEditorPlugin.class );

    // The plug-in ID
    public static final String      PLUGIN_ID = "org.polymap.core.mapeditor";

    // The shared instance
    private static MapEditorPlugin  plugin;


    /**
     * The constructor
     */
    public MapEditorPlugin() {
    }

    
    public void start( BundleContext context )
            throws Exception {
        super.start( context );
        plugin = this;
    }


    public void stop( BundleContext context )
            throws Exception {
        plugin = null;
        super.stop( context );
    }


    public static MapEditorPlugin getDefault() {
        return plugin;
    }

    /**
     * Creates a {@link ImageDescriptor} from the given resource path.
     *
     * @param resource The path the the resource inside the bundle.
     * @return Newly created or cached instance.
     */
    public static ImageDescriptor imageDescriptor( String resource ) {
        ImageRegistry registry = getDefault().getImageRegistry();
        synchronized (registry) {
            ImageDescriptor result = registry.getDescriptor( resource );
            if (result == null) {
                Bundle bundle = getDefault().getBundle();
                result = ImageDescriptor.createFromURL(
                        FileLocator.find( bundle, new Path( resource ), null ) );
                registry.put( resource, result );
            }
            return result;
        }
    }

    
    public static Image image( String resource ) {
        // create and cache
        imageDescriptor( resource );
        return getDefault().getImageRegistry().get( resource );
    }

}
