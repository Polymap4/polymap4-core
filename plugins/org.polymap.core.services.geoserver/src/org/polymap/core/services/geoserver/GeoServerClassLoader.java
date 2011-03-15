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
package org.polymap.core.services.geoserver;

import java.util.Arrays;
import java.util.Enumeration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.osgi.framework.Bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This ClassLoader acts like a WebAppClassLoader for the GeoServer instances.
 * It helps to start several GeoServers in separate (servlet) contexts.
 * 
 * @see "http://www.devdaily.com/java/jwarehouse/jetty-6.1.9/modules/jetty/src/main/java/org/mortbay/jetty/webapp/WebAppClassLoader.java.shtml"
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GeoServerClassLoader
        extends URLClassLoader {

    private static final Log log = LogFactory.getLog( GeoServerClassLoader.class );

    private ClassLoader         parent;
    
    private String              loaderName = "GeoServer";
    
    private boolean             isParentLoaderPriority = false;
    
    
    public GeoServerClassLoader( ClassLoader parent ) {
        super( new URL[] {}, parent );
        this.parent = parent;
        
        Bundle bundle = GeoServerPlugin.getDefault().getBundle();
        for (Enumeration en=bundle.findEntries( "lib/", "*.jar", false ); en.hasMoreElements(); ) {
            URL entry = (URL)en.nextElement();
            log.debug( "JAR found: " + entry );
            addURL( entry );
        }

        //  allow GeoServer/Spring to access my classes (have to be included in the bundle)
        addURL( bundle.getResource( "build/eclipse/" ) );
    }

    
    public void destroy() {
        this.parent = null;
    }

    
    public URL getResource( String name ) {
        //log.debug( "getResource(): path= " + name );
        return super.getResource( name );
    }

    public InputStream getResourceAsStream( String name ) {
        //log.debug( "getResourceAsStream(): path= " + name );
        return super.getResourceAsStream( name );
    }

    public Enumeration<URL> getResources( String name )
            throws IOException {
        //log.debug( "getResources(): path= " + name );
        return super.getResources( name );
    }


    public synchronized Class loadClass( String name )
            throws ClassNotFoundException {
        return loadClass( name, false );
    }


    protected synchronized Class loadClass( String name, boolean resolve )
            throws ClassNotFoundException {
        Class c = findLoadedClass( name );
        ClassNotFoundException ex = null;
        boolean tried_parent = false;
        
        // parent priority?
        if (c == null && parent != null
                && (isParentLoaderPriority || isSystemPath( name ))) {
            
            tried_parent = true;
            try {
                c = parent.loadClass( name );
            }
            catch (ClassNotFoundException e) {
                ex = e;
            }
        }
        // find class
        if (c == null) {
            try {
                c = this.findClass( name );
                //log.debug( "loadClass(): name= " + name );
            }
            catch (ClassNotFoundException e) {
                ex = e;
            }
        }
        // delegate parent
        if (c == null && parent != null && !tried_parent && !isServerPath( name )) {
            c = parent.loadClass( name );
        }
        if (c == null) {
            throw ex;
        }
        if (resolve) {
            resolveClass( c );
        }
        return c;
    }


    public boolean isSystemPath(String name)    {        
        //log.debug( "isSystemPath(): name= " + name );
        name = name.replace( '/', '.' );
        while (name.startsWith( "." )) {
            name = name.substring( 1 );
        }
        return !name.startsWith( "org.geoserver." ) && 
                !name.startsWith( "org.vfny." ) &&
                !name.startsWith( "org.polymap.core.services.geoserver.spring" );
        
//        String[] system_classes = _context.getSystemClasses();
//        if (system_classes != null) {
//            for (int i = 0; i < system_classes.length; i++) {
//                boolean result = true;
//                String c = system_classes[i];
//            }
//            if (c.startsWith( "-" )) {
//                c = c.substring( 1 ); // TODO cache result=false; }
//                if (c.endsWith( "." )) {
//                    if (name.startsWith( c ))
//                        return result;
//                }
//                else if (name.equals( c ))
//                    return result;
//            }
//        }
//        return false;
    }

    
    public boolean isServerPath(String name) {
        //log.debug( "isServerPath(): name= " + name );
        name = name.replace( '/', '.' );
        while (name.startsWith( "." )) {
            name = name.substring( 1 );
        }
        return false;
    }

    
    public String toString() {
        if (log.isDebugEnabled()) {
            return "ContextLoader@" + loaderName + "(" + Arrays.asList( getURLs() ) + ") / "
                    + parent;
        }
        return "ContextLoader@" + loaderName;
    }

}
