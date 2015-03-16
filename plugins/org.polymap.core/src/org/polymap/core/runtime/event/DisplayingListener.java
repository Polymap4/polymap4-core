/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.event;

import java.util.EventObject;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.swt.widgets.Display;

import org.polymap.core.ui.UIUtils;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DisplayingListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( DisplayingListener.class );

    private Display                         display;
        

    public DisplayingListener( EventListener delegate ) {
        super( delegate );
        this.display = UIUtils.sessionDisplay();
        assert display != null;
    }


    @Override
    public void handleEvent( final EventObject ev ) throws Exception {
        if (display.isDisposed()) {
            log.warn( "Display is disposed!" );
            delegate = null;
        }
        else {
            display.asyncExec( new Runnable() {
                public void run() {
                    try {
                        delegate.handleEvent( ev );
                    }
                    // as this is used by AnnotatedEventListener
                    catch (InvocationTargetException e) {
                        log.warn( "Error during event dispatch: " + e.getTargetException(), e.getTargetException() );
                    }
                    catch (Exception e) {
                        log.warn( "Error during event dispatch: " + e, e );
                    }
                }            
            });
        }
    }

}
