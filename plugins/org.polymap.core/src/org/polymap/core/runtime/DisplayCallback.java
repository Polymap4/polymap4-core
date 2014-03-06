/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.internal.lifecycle.LifeCycleUtil;

/**
 * Asyncronously handles to result in {@link Display#asyncExec(Runnable) display
 * thread}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public abstract class DisplayCallback<T>
        implements Callback<T> {

    private Display     display;
    

    public DisplayCallback() {
        display = LifeCycleUtil.getSessionDisplay();
        assert display != null : "Attempt to initialize DisplayCallback outside SessionContext.";
    }


    protected abstract void inDisplayThread( T arg );


    @Override
    public final void handle( final T arg ) {
        display.asyncExec( new Runnable() {
            public void run() {
                inDisplayThread( arg );
            }
        } );
    }

}
