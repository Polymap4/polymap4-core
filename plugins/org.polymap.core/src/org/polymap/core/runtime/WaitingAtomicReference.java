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

import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WaitingAtomicReference<T>
        extends AtomicReference<T> {

    /**
     * Wait until the reference is available.
     *
     * @return The reference.
     */
    public T waitAndGet() {
        T result = get();
        if (result == null) {
            synchronized (this) {
                while ((result = get()) == null) {
                    try { wait( 1000 ); } catch (InterruptedException e) { }
                }
                return get();
            }
        }
        return result;
    }

    /**
     * Sets the given reference value and notifies waiting threads.
     *
     * @param value
     */
    public void setAndNotify( T value ) {
        set( value );
        synchronized (this) {
            notifyAll();
        }
    }
}
