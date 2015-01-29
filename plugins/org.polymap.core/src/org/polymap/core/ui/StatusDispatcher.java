/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.ui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.CorePlugin;

/**
 * Common API to log and/or display status information to the UI.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StatusDispatcher {

    private static Log log = LogFactory.getLog( StatusDispatcher.class );
    
    public enum Style {
        /**
         * A style indicating that the status should not be acted on. This is used
         * by objects such as log listeners that do not want to report a status
         * twice.
         */
        NONE,
        /**
         * A style indicating that handlers should show a problem to an user without
         * blocking the calling method while awaiting user response. This is
         * generally done using a non modal {@link Dialog}.
         */
        SHOW,
        /**
         * A style indicating that the status should be logged only.
         */
        LOG,
        /**
         * A style indicating that the handling should block the calling thread until
         * the status has been handled.
         * <p>
         * A typical usage of this would be to ensure that the user's actions are
         * blocked until they've dealt with the status in some manner. It is
         * therefore likely but not required that the <code>StatusHandler</code>
         * would achieve this through the use of a modal dialog.
         * </p>
         * <p>
         * Due to the fact that use of <code>BLOCK</code> will block any thread, care
         * should be taken in this use of this flag.
         * </p>
         */
        BLOCK
    }
    
    private static List<Adapter>        adapters = new CopyOnWriteArrayList();
    
    public static void registerAdapter( Adapter adapter ) {
        adapters.add( adapter );
    }
    
    public static void handle2() {
        StatusUtil.handleStatus( "", 0 );
        StatusManager.getManager();
    }
    
    public static void handleError( String pluginId, Object src, String msg, Throwable e ) {
        handle( new Status( IStatus.ERROR, pluginId, msg, e ), Style.SHOW, Style.LOG );
    }

    public void handleError( String msg, Throwable e ) {
        handle( new Status( IStatus.ERROR, CorePlugin.PLUGIN_ID, msg, e ), Style.SHOW, Style.LOG );        
    }

    public static void handle( IStatus status, Style... styles ) {
        if (adapters.isEmpty()) {
            log.warn( "No StatusDispatcher.Adapter registered!" );
        }
        
        if (status.getSeverity() == IStatus.ERROR) {
            log.error( status.getMessage(), status.getException() );
        }
        
        adapters.stream().forEach( adapter -> {
            try {
                adapter.handle( status, styles );
            }
            catch (Throwable e) {
                log.error( "Unhandled exception while processing status: ", e );
            }
        });
    }
    
    
    /**
     * 
     */
    public interface Adapter {
    
        public void handle( IStatus status, Style... styles );
        
    }
    
}
