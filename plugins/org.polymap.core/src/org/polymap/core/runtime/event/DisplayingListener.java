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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DisplayingListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( DisplayingListener.class );

    private Display                         display;
        

    public DisplayingListener( EventListener delegate ) {
        super( delegate );
        this.display = Polymap.getSessionDisplay();
        assert display != null;
    }


    @Override
    public void handleEvent( final Event ev ) throws Exception {
        display.asyncExec( new Runnable() {
            public void run() {
                try {
                    delegate.handleEvent( ev );
                }
                // as this is used by AnnotatedEventListener
                catch (InvocationTargetException e) {
                    PolymapWorkbench.handleError( CorePlugin.PLUGIN_ID, delegate, "Error during event dispatch.", e.getTargetException() );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( CorePlugin.PLUGIN_ID, delegate, "Error during event dispatch.", e );
                }
            }            
        });
    }

}
