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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A reference that blocks on {@link #waitAndGet()} until another thread has
 * initialized the value.
 * <p/>
 * This implementation uses a {@link ReentrantLock}. Consider
 * {@link BlockingReference2} for a simpler and faster (?) implementation.
 * 
 * @see BlockingReference2
 * @see AtomicReference
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BlockingReference<T> {
    
    private ReentrantLock   lock = new ReentrantLock();
    
    private Condition       notNull = lock.newCondition();
    
    private volatile T      value = null;

    
    /**
     * Returns <code>true</code> if subsequent {@link #waitAndGet()} would not wait.
     * @see #isEventuallyInitialized()
     */
    public boolean isInitialized() {
        return value != null;
    }

    
//    /**
//     * Returns <code>true</code> if subsequent {@link #waitAndGet()} would eventually
//     * return a value. That is, returns true if the value is already initialized or
//     * another {@link Thread} is already about to initialize the value.
//     */
//    public boolean isEventuallyInitialized() {
//    }

    
    /**
     * Waits until the reference is initialized and available.
     * @throws InterruptedException 
     */
    public T waitAndGet() {
        try {
            lock.lock();
            while (value == null) {
                notNull.awaitUninterruptibly();
            }
            return value;
        }
        finally {
            lock.unlock();
        }
    }

    
    /**
     * Sets the given reference value and notifies waiting threads.
     *
     * @param value
     * @throws InterruptedException 
     */
    public void set( T value ) {
        assert value != null && this.value == null;
        lock.lock();
        try {
            this.value = value;
            notNull.signalAll();
        }
        finally {
            lock.unlock();
        }
    }
    
}
