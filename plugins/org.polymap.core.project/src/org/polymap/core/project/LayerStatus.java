/* 
 * polymap.org
 * Copyright (C) 2009-2014, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.i18n.IMessages;

/**
 * This special status is used to provide feedback for a Layers status. The
 * {@link #WAITING} severity is added in addition to base {@link Status}.
 * 
 * @see RenderStatus
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class LayerStatus
        extends Status {

    public static final IMessages   i18n = Messages.forPrefix( "LayerStatus" ); //$NON-NLS-1$

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
    public static final LayerStatus STATUS_OK() { return new LayerStatus( OK, OK, i18n.get("ok"), null ); } //$NON-NLS-1$

    /** A standard WAITING status. */
    public static final LayerStatus STATUS_WAITING() { return new LayerStatus( OK, WAITING, i18n.get("waiting"), null ); } //$NON-NLS-1$

    /** A standard MISSING status. */
    public static final LayerStatus STATUS_MISSING( Throwable e ) { return new LayerStatus( ERROR, MISSING, i18n.get("missing"), null ); } //$NON-NLS-1$

    /** A standard STYLE_MISSING status. */
    public static final LayerStatus STATUS_STYLE_MISSING() { return new LayerStatus( ERROR, STYLE_MISSING, i18n.get("styleMissing"), null ); } //$NON-NLS-1$


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
