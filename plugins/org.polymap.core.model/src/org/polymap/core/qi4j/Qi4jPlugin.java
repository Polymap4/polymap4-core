/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.qi4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;

import com.google.common.base.Supplier;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.runtime.LazyInit;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.SessionSingleton;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class Qi4jPlugin 
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( Qi4jPlugin.class );
    
    public static final String PLUGIN_ID = "org.polymap.core.model";

	private static Qi4jPlugin               plugin;
	
    private static LazyInit<Energy4Java>    qi4j = 
            new LockedLazyInit( new Supplier<Energy4Java>() {
                public Energy4Java get() {
                    return new Energy4Java();
                }
    });
    
    private static LazyInit<List<QiModuleAssembler>> assemblers = 
            new LockedLazyInit( new ApplicationLoader() );

    
    /**
     * 
     */
    static class ApplicationLoader 
            implements Supplier<List<QiModuleAssembler>> {

        @Override
        public List<QiModuleAssembler> get() {
            try {
                final List<QiModuleAssembler> result = new ArrayList();
                
                ApplicationSPI app = qi4j.get().newApplication( new ApplicationAssembler() {
                    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                    throws AssemblyException {
                        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();

                        // plugin extensions
                        IExtensionRegistry reg = Platform.getExtensionRegistry();
                        IConfigurationElement[] extensions = reg.getConfigurationElementsFor( "org.polymap.core.model.moduleAssemblers" );
                        for (IConfigurationElement ext : extensions) {
                            try {
                                QiModuleAssembler assembler = (QiModuleAssembler)ext.createExecutableExtension( "class" );
                                result.add( assembler );
                                
                                assembler.assemble( assembly );
                            }
                            catch (Exception e) {
                                log.error( "Error while initializing module: " + ext.getName(), e );
                            }
                        }
                        return assembly;
                    }
                } );
                app.activate();
                
                //
                for (QiModuleAssembler assembler : result) {
                    assembler.setApp( app );
                    assembler.createInitData();
                }
                return result;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }        
    }
    
    
    public Qi4jPlugin() {
	}


    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
    }

    
    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop( context );
    }

    
    public static Qi4jPlugin getDefault() {
        return plugin;
    }


    public static boolean isInitialized() {
        return assemblers.isInitialized();
    }

    
	/**
     * This is the user session specific part of the API of the plugin instance.
     */
	public static final class Session
	        extends SessionSingleton {
	
	    private Map<Class,QiModule>        modules;
	    
	    
        /**
         * Get or create the instance for the current user session.
         */
        public static final Session instance() {
            return instance( Session.class );
        }


	    Session() {
	    	// build the modules of the session
	    	if (modules == null) {
	    	    modules = new HashMap();
	    	    for (QiModuleAssembler assembler : assemblers.get()) {
	    	        QiModule module = assembler.newModule();
	    	        modules.put( module.getClass(), module );
	    	    }
	    	    for (QiModule module : modules.values()) {
	    	        module.init( this );
	    	    }
	    	}
	    }
	    
	    public <T extends QiModule> T module( Class<T> type ) {
	        QiModule result = modules.get( type );
	        return (T)result;
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
	            return modules.get( appliesTo );
	        }
	        // Entity
	        else if (appliesTo instanceof QiEntity) {
                for (QiModule module : modules.values()) {
                    QiEntity entity = (QiEntity)appliesTo;
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
