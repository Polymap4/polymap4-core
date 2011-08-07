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

package org.polymap.core.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.ui.OffThreadProgressMonitor;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.UIJob;

/**
 * Provides access to the progress monitor of the current thread. 
 *
 * @deprecated Use {@link UIJob} instead.
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class JobMonitors {
    
    private static Log log = LogFactory.getLog( JobMonitors.class );
    
    private static ThreadLocal      monitors = new ThreadLocal();
    
    
    public static void set( OffThreadProgressMonitor monitor ) {
        if (monitors.get() != null) {
            log.warn( "Thread has monitor registered already." );
        }
        monitors.set( monitor );
    }

    
    public static void remove() {
        if (monitors.get() == null) {
            log.warn( "No monitor registered for this thread." );
        }
        monitors.remove();
    }
    
    
    /**
     * Returns the progress monitor of the calling thread.
     * 
     * @return The previously registered monitor or a {@link NullProgressMonitor}.
     */
    public static OffThreadProgressMonitor get() {
        OffThreadProgressMonitor result = (OffThreadProgressMonitor)monitors.get();
        return result != null
                ? result
                : new OffThreadProgressMonitor( new NullProgressMonitor() );
    }
    
}
