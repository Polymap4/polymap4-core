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

import com.google.common.base.Supplier;

import org.eclipse.swt.widgets.Display;

/**
 * Calls to {@link Supplier} inside the {@link Display} thread.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DisplayLazyInit<T>
        extends LazyInit<T> {

    private volatile T          value;
    
    
    public DisplayLazyInit( Supplier<T> supplier ) {
        super( supplier );
    }

    public T get( @SuppressWarnings("hiding") Supplier<T> supplier ) {
        this.supplier = supplier;
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    Polymap.getSessionDisplay().syncExec( new Runnable() {
                        public void run() {
                            value = DisplayLazyInit.this.supplier.get();
                        }
                    });
                }
            }
        }
        return value;
    }

    public void clear() {
        value = null;
    }

    public boolean isInitialized() {
        return value != null;
    }
    
}
