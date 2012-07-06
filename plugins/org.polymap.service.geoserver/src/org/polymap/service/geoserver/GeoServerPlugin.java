/* 
 * polymap.org
 * Copyright 2009-2012 Polymap GmbH. All rights reserved.
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
package org.polymap.service.geoserver;

import java.io.File;

import org.osgi.framework.BundleContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class GeoServerPlugin 
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( GeoServerPlugin.class );

    // The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.service.geoserver";

	// The shared instance
	private static GeoServerPlugin plugin;
	
	private File                   cacheDir;
	
	
    /**
     * Returns the cache directory of this plugin.
     */
    public File getCacheDir() {
        return cacheDir;
    }
    
    
	public void start( BundleContext context ) throws Exception {
		super.start( context );
		plugin = this;

		cacheDir = new File( Polymap.getCacheDir(), PLUGIN_ID );
        if (cacheDir.exists()) {
            log.info( "Cleaning cache dir: " + cacheDir );
            FileUtils.deleteDirectory( cacheDir );
            cacheDir.mkdir();
        }
        else {
            log.info( "Creating cache dir: " + cacheDir );
            cacheDir.mkdir();            
        }
	}

	
	public void stop( BundleContext context ) throws Exception {
		plugin = null;
		super.stop(context);

		if (cacheDir.exists()) {
            log.info( "Cleaning cache dir: " + cacheDir );
            FileUtils.deleteDirectory( cacheDir );
        }
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GeoServerPlugin getDefault() {
		return plugin;
	}

}
