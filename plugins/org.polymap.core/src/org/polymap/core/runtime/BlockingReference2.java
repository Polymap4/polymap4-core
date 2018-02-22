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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A reference that blocks on {@link #waitAndGet()} until another thread has
 * initialized the value.
 * 
 * @see BlockingReference
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BlockingReference2<T>
        extends BlockingReference<T> {

    private static final Log log = LogFactory.getLog( BlockingReference2.class );

    private volatile T      value = null;

    private Object          lock = new Object();
    
    
    /**
     * Returns <code>true</code> if subsequent {@link #waitAndGet()} will not wait.
     */
    @Override
    public boolean isInitialized() {
        return value != null;
    }

    
    /**
     * Wait until the reference is available.
     *
     * @return The reference.
     */
    @Override
    public T waitAndGet() {
        if (value == null) {
            synchronized (lock) {
                while (value == null) {
                    try { 
                        lock.wait( 1000 );
                        log.debug( "waiting..." );
                    } 
                    catch (InterruptedException e) {
                        // XXX Hmmm... ??
                        throw new RuntimeException( e );
                    }
                }
            }
        }
        return value;
    }

    
    /**
     * Sets the given reference value and notifies waiting threads.
     *
     * @param value
     */
    @Override
    public void set( T value ) {
        synchronized (lock) {
            assert value != null && this.value == null;
            this.value = value;
            lock.notifyAll();
        }
    }
    
}
