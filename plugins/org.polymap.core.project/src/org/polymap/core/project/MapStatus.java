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
 * This special status is used to provide feedback for a Map's status.
 * 
 * @see RenderStatus
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MapStatus
        extends Status {

    /**
     * Status type severity (bit mask, value 128) indicating that an unspecified
     * error has occured. See the status message for more detail.
     */
    public static final int         UNSPECIFIED = 128;

    /** A standard OK status with an "ok"  message. */
    public static final MapStatus   STATUS_OK = new MapStatus( OK, OK, Messages.get("LayerStatus_ok"), null ); //$NON-NLS-1$

    private List<MapStatus>         children = new LinkedList();
    
    
    public MapStatus( int severity, int code, String message, Throwable exception ) {
        super( severity, ProjectPlugin.PLUGIN_ID, code, message, exception );
    }

    
    public void add( MapStatus child ) {
        children.add( child );
    }
    
    
    public boolean isMultiStatus() {
        return !children.isEmpty();
    }


    public Status[] getChildren() {
        return (Status[])children.toArray();
    }

}
