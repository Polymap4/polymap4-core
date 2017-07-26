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
package org.polymap.core.data.process;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.GridCoverage2DReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.CoreException;

import org.polymap.core.runtime.session.SessionSingleton;
import org.polymap.core.ui.StatusDispatcher;

/**
 * Entry point to the processing API. 
 *
 * @author Falko Bräutigam
 */
public class Modules
        extends SessionSingleton {

    private static final Log log = LogFactory.getLog( Modules.class );
    
    public static Modules instance() {
        return instance( Modules.class );
    }
    
    
    // instance *******************************************
    
    public List<ModuleInfo> rasterExecutables() {
        logMemory();            
        List<ModuleInfo> result = new ArrayList( 256 );

        // providers
        for (ModuleProviderExtension ext : ModuleProviderExtension.allExtensions()) {
            try {
                ext.newProvider().createModuleInfos().stream()
                        .filter( hasInputField( GridCoverage2D.class ).or( hasInputField( GridCoverage2DReader.class ) ) )
                        .forEach( info -> result.add( info ) );
            }
            catch (CoreException e) {
                log.error( "", e );
                StatusDispatcher.handleError( "Error while creating processing modules.", e );
            }
        }

        logMemory();
        return result;
    }


    protected Predicate<ModuleInfo> hasInputField( Class<?> type ) {
        return (ModuleInfo candidate) -> {
            for (FieldInfo field : candidate.inputFields()) {
                if (field.type().isAssignableFrom( type ) ) {
                    return true;
                }
            }
            return false;
        };
    }
    
    
    protected void logMemory() {
        System.gc();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();            
        log.info( "Memory used: " + (total-free) / (1024*1024) + "MB");
    }

}
