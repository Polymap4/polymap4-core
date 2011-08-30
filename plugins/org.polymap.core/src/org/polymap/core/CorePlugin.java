/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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

package org.polymap.core;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.util.logging.Logging;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.http.HttpServiceRegistry;
import org.polymap.core.runtime.SessionContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>24.06.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class CorePlugin
        extends AbstractUIPlugin {

	private static Log log = LogFactory.getLog( CorePlugin.class );

	// The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.core";

	// The shared instance
	private static CorePlugin  plugin;

	private static boolean     httpServiceRegistryStarted = false;


	public static ImageDescriptor imageDescriptor( String path ) {
        return imageDescriptorFromPlugin( PLUGIN_ID, path );
    }


	static {
	    try {
            Logging.GEOTOOLS.setLoggerFactory( "org.geotools.util.logging.CommonsLoggerFactory" );
            System.out.print( "GEOTOOLS logger set to: " + "CommonsLogger" );
        }
        catch (Exception e) {
            System.out.println( "No GEOTOOLS logger: " + e );
        }

        // horrible log configuration system...
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.help", "debug" );

        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.jdbc", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.data", "trace" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.data.wfs", "trace" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.data.communication", "trace" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.services.geoserver", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.mapeditor.RenderManager", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.mapeditor.services.SimpleWmsServer", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.mapeditor.edit", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.project.operations.NewLayerOperation", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.project.impl.MapImpl", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.qi4j", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.eu.hydrologis.jgrass.csv2shape.importwizard", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.qi4j.entitystore.lucene.LuceneQueryParserImpl", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.rhei.data", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.rhei.navigator", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.biotop", "debug" );
        
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.data.feature.buffer", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.com.ettrema.http", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.com.bradmcevoy", "info" );
	}

	
	// instance *******************************************
	
    private RapSessionContextProvider rapSessionContextProvider;


	public CorePlugin() {
		log.debug( "Hello from the first POLYMAP3 plugin! :)" );

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IPath path = root.getLocation();
        log.info( "Workspace: " + path.toOSString() );
	}


    public void start( final BundleContext context )
    throws Exception {
        super.start( context );
        log.debug( "start..." );
        plugin = this;
        
        //
        this.rapSessionContextProvider = new RapSessionContextProvider();
        SessionContext.addProvider( rapSessionContextProvider );

        // start HttpServiceRegistry
        context.addBundleListener( new BundleListener() {
            public void bundleChanged( BundleEvent ev ) {
                // check all bundles if HttpService appears
                if (ev.getType() == BundleEvent.STARTED && !httpServiceRegistryStarted) {
                    try {
                        ServiceReference[] httpReferences = context.getServiceReferences( HttpService.class.getName(), null );
                        if (httpReferences != null) {
                            HttpServiceRegistry.init();
                            httpServiceRegistryStarted = true;
                        }
                    }
                    catch (InvalidSyntaxException e) {
                        throw new RuntimeException( e.getMessage(), e );
                    }
                }
                // stop
                else if (ev.getType() == BundleEvent.STOPPED && ev.getBundle().equals( getBundle() )) {
                    HttpServiceRegistry.dispose();
                }
            }
        });
    }


    public void stop( BundleContext context )
    throws Exception {
        log.debug( "stop..." );
        plugin = null;
        super.stop( context );
        
        SessionContext.removeProvider( rapSessionContextProvider );
        rapSessionContextProvider = null;
    }


	public static CorePlugin getDefault() {
		return plugin;
	}


    public static void logInfo( String msg ) {
        getDefault().getLog().log( new Status( IStatus.INFO, PLUGIN_ID, msg ) );
    }


    public static void logError( String msg ) {
        getDefault().getLog().log( new Status( IStatus.ERROR, PLUGIN_ID, msg ) );
    }


    /**
     * XXX we need a central log facility; first shot for API; should be
     * refactored into log service package/classes
     *
     * @param msg
     * @param callerLog
     * @param e
     */
    public static void logError( String msg, Log callerLog, Throwable e ) {
        if (callerLog != null) {
            log.error( msg, e );
        }
        // FIXME does this make sense in RAP???
        getDefault().getLog().log( new Status( IStatus.ERROR, PLUGIN_ID, msg ) );
    }

}
