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
package org.polymap.core.runtime;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.DefaultInt;

/**
 * Extends {@link SubProgressMonitor} as follows:
 * <ul>
 * <li></li>
 * </ul> 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SubMonitor
        extends SubProgressMonitor {

    private static Log log = LogFactory.getLog( SubMonitor.class );

    public static SubMonitor on( IProgressMonitor monitor, int ticks ) {
        return new SubMonitor( monitor, ticks );
    }
    
    // instance *******************************************
    
    @DefaultInt( 1 )
    public Config2<SubMonitor,Integer>  updateDelayCount;
    
    private String                      mainTaskName;
    
    private int                         worked = 0;
    
    
    public SubMonitor( IProgressMonitor monitor, int ticks ) {
        super( monitor, ticks );
    }


    public void beginTask( String name, int totalWork ) {
        super.beginTask( name, totalWork );
        mainTaskName = name;
        super.subTask( mainTaskName );
    }


    public void subTask( String name ) {
        super.subTask( mainTaskName + " - " + name );
    }
    
    
    @Override
    public void worked( int work ) {
        worked += work;
        if ((++worked % updateDelayCount.get()) == 0) {
            super.worked( updateDelayCount.get() );
            //setTaskName( "Objekte: " + worked );
        }
    }

    
    public <E extends Exception> void complete( Task<E> task ) throws Exception {
        try { 
            task.execute( this );
        }
        finally {
            done();
        }
    }
    
    
    @FunctionalInterface
    public interface Task<E extends Exception> {
        
        public void execute( SubMonitor monitor ) throws E;
        
    }
}
