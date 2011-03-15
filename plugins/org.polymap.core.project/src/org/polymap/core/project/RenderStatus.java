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

import org.eclipse.core.runtime.Status;

/**
 * This special status is used to provide feedback for the rendering status of
 * an {@link ILayer} or an {@link IMap}. The {@link #WORKING} severity is added
 * in addition to base {@link Status}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class RenderStatus
        extends Status {

    /** A standard OK status with an "ok"  message. */
    public static final RenderStatus STATUS_OK = new RenderStatus( OK, ProjectPlugin.PLUGIN_ID, OK, "ok", null );

    /** A standard CANCEL status with no message. */
    public static final RenderStatus STATUS_CANCEL = new RenderStatus( CANCEL, ProjectPlugin.PLUGIN_ID, CANCEL, "canceled", null );

    /** 
     * Status type severity (bit mask, value 16) indicating this status represents a
     * layer rendering in progress.
     */
    public static final int WORKING = 16;

    
    public RenderStatus( int severity, String pluginId, int code, String message,
            Throwable exception ) {
        super( severity, pluginId, code, message, exception );
    }

    public RenderStatus( int severity, String pluginId, String message, Throwable exception ) {
        super( severity, pluginId, message, exception );
    }

    public RenderStatus( int severity, String pluginId, String message ) {
        super( severity, pluginId, message );
    }
    
}
