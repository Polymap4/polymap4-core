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
package org.polymap.core.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ProjectPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( ProjectPlugin.class );
    
    // The plug-in ID
    public static final String      PLUGIN_ID = "org.polymap.core.project";

    // The shared instance
    private static ProjectPlugin    instance;
    
    //
    private static IGeoResourceResolver resolver;
    

    public ProjectPlugin() {
        super();
        instance = this;
    }


    /**
     * The map that was last selected in the UI. It may no longer be selected,
     * however operations and commands should work wirth this 'current' map.
     * <p>
     * Shortcut for {@link MapEditorPluginSession#getSelectedMap()}.
     */
    public static IMap getSelectedMap() {
        return ProjectPluginSession.instance().getSelectedMap();
    }


    public void start( BundleContext context )
            throws Exception {
        super.start( context );
        log.info( "start..." );
    }
    
    
    public static ProjectPlugin getDefault() {
        return instance;
    }


    /**
     *
     * @param layer
     * @return The resolver for the given layer.
     */
    public static IGeoResourceResolver geoResourceResolver( ILayer layer ) {
        if (resolver == null) {
            resolver = new DefaultGeoResourceResolver();
        }
        return resolver;
    }
    

//    public static MDomain loadDomain( String name )
//            throws IOException, ModelRuntimeException, JAXBException, ClassNotFoundException {
//        File domainFile = new File( 
//                Polymap.getWorkspacePath().toFile(), 
//                name + "_config.xml" );        
//        if (!domainFile.exists()) {
//            throw new IOException( "Domain file does not exist: " + domainFile.toString() );
//        }
//
//        InputStream in = null;
//        try {
//            log.debug( "Loading domain: " + name + " ..............." );
//            PlainMDomain domain = new PlainMDomain( new ProjectFactoryImpl() );
//            in = new BufferedInputStream( new FileInputStream( domainFile ) );
//            XmlSerializer serializer = new XmlSerializer( domain.createSerializerContext() );
//            serializer.load( in );
//            in.close();
//            return domain;
//        }
//        finally {
//            in.close();
//        }
//    }
//
//    
//    public static void saveDomain( MDomain domain, String name) {
//        File domainFile = new File( 
//                Polymap.getWorkspacePath().toFile(), 
//                name + "_config.xml" );        
//
//        OutputStream out = null;
//        try {
//            log.debug( "Saving domain: " + name + " ..............." );
//            out = new BufferedOutputStream( new FileOutputStream( domainFile ) );
//            XmlSerializer serializer = new XmlSerializer( domain.createSerializerContext() );
//            serializer.store( out );
//            out.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            try {
//                out.close();
//                domainFile.delete();
//            }
//            catch (IOException e1) {
//                // ignore
//            }
//        }
//    }

    
//    protected void initializeImageRegistry(ImageRegistry registry) {
//        super.initializeImageRegistry(registry);
//        Bundle bundle = 
//
//
//        ImageDescriptor myImage = ImageDescriptor.createFromURL(
//              FileLocator.find(bundle,
//                               new Path("icons/myImage..gif"),
//                                        null));
//        registry.put(MY_IMAGE_ID, myImage);
//    }


    public static ImageDescriptor getImageDescriptor( String path ) {
        ImageRegistry registry = instance.getImageRegistry();
        ImageDescriptor result = registry.getDescriptor( path );
        if (result == null) {
            Bundle bundle = instance.getBundle();
            result = ImageDescriptor.createFromURL(
                    FileLocator.find( bundle, new Path( path ), null ) );
            registry.put( path, result );
        }
        return result;
    }

    
    public static Image getImage( String path ) {
        // create and cache
        getImageDescriptor( path );
        return instance.getImageRegistry().get( path );
    }

    
    public static void logInfo( String msg ) {
        getDefault().getLog().log( new Status( IStatus.INFO, PLUGIN_ID, msg ) );    
    }
    

    public static void logError( String msg ) {
        try {
            getDefault().getLog().log( new Status( IStatus.ERROR, PLUGIN_ID, msg ) );
        }
        catch (Exception e) {
            // ignore
        }    
    }    

}
