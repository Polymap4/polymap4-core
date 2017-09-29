/* 
 * polymap.org
 * Copyright (C) 2011-2017, Polymap GmbH. All rights reserved.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.runtime.config.ConfigurationFactory;

/**
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SubMonitor
        extends SubProgressMonitor {

    public static SubMonitor on( IProgressMonitor monitor, int ticks ) {
        return new SubMonitor( monitor, ticks );
    }
    
    // instance *******************************************
    
    /**
     * Creates a new sub-progress monitor for the given monitor. The sub progress
     * monitor uses the given number of work ticks from its parent monitor.
     *
     * @param monitor The parent progress monitor.
     * @param ticks The number of work ticks allocated from the parent monitor.
     */
    public SubMonitor( IProgressMonitor monitor, int ticks ) {
        super( monitor, ticks );
        ConfigurationFactory.inject( this );
    }


    /**
     * Creates a new sub-progress monitor for the given monitor. The sub progress
     * monitor uses the given number of work ticks from its parent monitor.
     *
     * @param monitor The parent progress monitor.
     * @param ticks The number of work ticks allocated from the parent monitor.
     * @param taskName Call {@link #beginTask(String, int)}.
     * @param totalWork Call {@link #beginTask(String, int)}.
     */
    public SubMonitor( IProgressMonitor monitor, int ticks, String taskName, int totalWork ) {
        super( monitor, ticks );
        ConfigurationFactory.inject( this );
        beginTask( taskName, totalWork );
    }

    
    @Override
    public void beginTask( String name, int totalWork ) {
        super.beginTask( name, totalWork );
        super.subTask( name );
    }


    public void subTask( String name ) {
        super.subTask( name );
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
