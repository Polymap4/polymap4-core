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
package org.polymap.core.qi4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.SessionSingletonBase;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.model.Entity;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class Qi4jPlugin 
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( Qi4jPlugin.class );
    
	// The plug-in ID

    public static final String PLUGIN_ID = "org.polymap.core.qi4j";

	// The shared instance
	private static Qi4jPlugin               plugin;
	
    private static Energy4Java              qi4j;
    
    private static ApplicationSPI           application;

    private static List<QiModuleAssembler>  assemblers = new ArrayList();
    
    private static boolean                  initialized = false;
    
    
    public Qi4jPlugin() {
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

    public static Qi4jPlugin getDefault() {
        return plugin;
    }


    private static synchronized void init()
            throws Exception {
        if (qi4j != null) {
            return;
        }
        qi4j = new Energy4Java();
        application = qi4j.newApplication( new ApplicationAssembler() {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                    throws AssemblyException {

                ApplicationAssembly app = applicationFactory.newApplicationAssembly();

                // plugin extensions
                IExtensionRegistry reg = Platform.getExtensionRegistry();
                IConfigurationElement[] extensions = reg.getConfigurationElementsFor( "org.polymap.core.qi4j.moduleAssemblers" );
                for (IConfigurationElement ext : extensions) {
                    try {
                        QiModuleAssembler assembler = (QiModuleAssembler)ext.createExecutableExtension( "class" );
                        assemblers.add( assembler );
                        
                        assembler.assemble( app );
                    }
                    catch (Exception e) {
                        log.error( "Error while initializing module: " + ext.getName(), e );
                    }
                }
                return app;
            }
        } );
        application.activate();
        
        //
        for (QiModuleAssembler assembler : assemblers) {
            assembler.setApp( application );
            assembler.createInitData();
        }
        
        initialized = true;
    }

    
    public static boolean isInitialized() {
        return initialized;
    }

    
	/**
     * This is the user session specific part of the API of the plugin instance.
     */
	public static final class Session
	        extends SessionSingletonBase {
	
	    private Map<Class,QiModule>        modules = new HashMap();
	    
	    
        /**
         * Get or create the instance for the current user session.
         */
        public static final Session instance() {
            return (Session)getInstance( Session.class );
        }


        /**
         * The global instance used outside any user session.
         * 
         * @return A newly created {@link Session} instance. It is up to the
         *         caller to store and re-use if necessary.
         */
        public static final synchronized Session globalInstance() {
            return new Session();
        }
        
        
	    Session() {
	    	// lazily init the plugin
	        try {
				init();
			} 
	    	catch (Exception e) {
				throw new RuntimeException( e );
			}
	    	
	    	// build the modules of the session
	        for (QiModuleAssembler assembler : Qi4jPlugin.assemblers) {
	            QiModule module = assembler.newModule();
	            modules.put( module.getClass(), module );
	        }
	    }
	    
	    public QiModule module( Class type ) {
	        QiModule result = modules.get( type );
	        return result;
	    }

//	    /**
//	     * Provides Concerns and SideEffects access to the registered modules.
//	     */
//	    public Collection<QiModule> modules() {
//	        return modules.values();
//	    }
	    
	    
	    /**
	     * Resolves the module for the given object it applies to. The
	     * following object types are supported:
	     * <ul>
         * <li>org.qi4j.api.structure.Module</li>
         * <li>Class<? extends QiModule></li>
         * <li>org.polymap.core.model.Entity</li>
	     * </ul>
	     * 
	     * @param appliesTo
	     * @return The found module or null.
	     */
	    public QiModule resolveModule( Object appliesTo ) {
	        // called from SideEffects and Concerns
	        if (appliesTo instanceof org.qi4j.api.structure.Module) {
	            for (QiModule module : modules.values()) {
	                if (module.appliesTo( (org.qi4j.api.structure.Module)appliesTo )) {
	                    return module;
	                }
	            }
	            return null;
	        }
	        // called by the modules
	        else if (appliesTo instanceof Class) {
	            return modules.get( (Class)appliesTo );
	        }
	        // Entity
	        else if (appliesTo instanceof Entity) {
                for (QiModule module : modules.values()) {
                    Entity entity = (Entity)appliesTo;
                    try {
                        module.findEntity( entity.getCompositeType(), entity.id() );
                        return module;
                    }
                    catch (Exception e) {
                        // try next
                    }
                }
                return null;
	        }
	        else {
	            throw new IllegalArgumentException( "Unhandled object type:" + appliesTo );
	        }
	    }
	}
	
}
