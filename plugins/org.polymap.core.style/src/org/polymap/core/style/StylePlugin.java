/* 
 * polymap.org
 * Copyright 2009-2016, Polymap GmbH. All rights reserved.
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
package org.polymap.core.style;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StylePlugin 
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( StylePlugin.class );
    
	public static final String             PLUGIN_ID = "org.polymap.core.style";

	private static StylePlugin             instance;
	
    private static boolean                 started = false;

	
	public void start( final BundleContext context ) throws Exception {
		super.start( context );
		instance = this;
    }

	
    public void stop( BundleContext context ) throws Exception {
        instance = null;
        super.stop( context );
    }


	public static StylePlugin instance() {
		return instance;
	}
	
}
