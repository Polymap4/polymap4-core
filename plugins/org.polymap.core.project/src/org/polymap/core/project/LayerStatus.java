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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Status;

/**
 * This special status is used to provide feedback for a Layers status. The
 * {@link #WAITING} severity is added in addition to base {@link Status}.
 * 
 * @see RenderStatus
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class LayerStatus
        extends Status {

    /** 
     * Status type severity (bit mask, value 16) indicating this status represents a
     * layer is waiting for (external) information.
     */
    public static final int         WAITING = 16;

    /**
     * Status type severity (bit mask, value 32) indicating cannot locate a
     * GeoResource for this layer.
     */
    public static final int         MISSING = 32;

    /**
     * Status type severity (bit mask, value 64) indicating cannot locate a
     * Style for this layer.
     */
    public static final int         STYLE_MISSING = 64;

    /**
     * Status type severity (bit mask, value 128) indicating that an unspecified
     * error has occured. See the status message for more detail.
     */
    public static final int         UNSPECIFIED = 128;

    /** A standard OK status with an "ok"  message. */
    public static final LayerStatus STATUS_OK = new LayerStatus( OK, OK, Messages.get("LayerStatus_ok"), null ); //$NON-NLS-1$

    /** A standard WAITING status. */
    public static final LayerStatus STATUS_WAITING = new LayerStatus( OK, WAITING, Messages.get("LayerStatus_waiting"), null ); //$NON-NLS-1$

    /** A standard MISSING status. */
    public static final LayerStatus STATUS_MISSING = new LayerStatus( ERROR, MISSING, Messages.get("LayerStatus_missing"), null ); //$NON-NLS-1$

    /** A standard STYLE_MISSING status. */
    public static final LayerStatus STATUS_STYLE_MISSING = new LayerStatus( ERROR, STYLE_MISSING, Messages.get("LayerStatus_styleMissing"), null ); //$NON-NLS-1$


    private List<LayerStatus>       children = new LinkedList();
    
    
    public LayerStatus( int severity, int code, String message, Throwable exception ) {
        super( severity, ProjectPlugin.PLUGIN_ID, code, message, exception );
    }

    
//    public LayerStatus( int severity, String pluginId, String message, Throwable exception ) {
//        super( severity, pluginId, message, exception );
//    }

    
//    public LayerStatus( int severity, String pluginId, String message ) {
//        super( severity, pluginId, message );
//    }
    
    
    public void add( LayerStatus child ) {
        children.add( child );
    }
    
    
    
    public boolean isMultiStatus() {
        return !children.isEmpty();
    }


    public Status[] getChildren() {
        return (Status[])children.toArray();
    }
}
