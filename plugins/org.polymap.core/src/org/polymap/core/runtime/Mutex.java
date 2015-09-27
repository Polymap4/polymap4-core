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
package org.polymap.core.runtime;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A non-reentrant mutual exclusion lock class that uses the value zero to represent
 * the unlocked state, and one to represent the locked state. While a non-reentrant
 * lock does not strictly require recording of the current owner thread, this class
 * does so anyway to make usage easier to monitor. It also supports conditions and
 * exposes one of the instrumentation methods.
 *
 * @author {@link AbstractQueuedSynchronizer} documentation.
 */
public class Mutex 
        implements Lock, java.io.Serializable {

    // Our internal helper class
    private static class Sync
            extends AbstractQueuedSynchronizer {

        // Reports whether in locked state
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }


        // Acquires the lock if state is zero
        public boolean tryAcquire( int acquires ) {
            assert acquires == 1; // Otherwise unused
            if (compareAndSetState( 0, 1 )) {
                setExclusiveOwnerThread( Thread.currentThread() );
                return true;
            }
            return false;
        }


        // Releases the lock by setting state to zero
        protected boolean tryRelease( int releases ) {
            assert releases == 1; // Otherwise unused
            if (getState() == 0)
                throw new IllegalMonitorStateException();
            setExclusiveOwnerThread( null );
            setState( 0 );
            return true;
        }


        // Provides a Condition
        Condition newCondition() {
            return new ConditionObject();
        }


        // Deserializes properly
        private void readObject( ObjectInputStream s ) throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState( 0 ); // reset to unlocked state
        }
    }

    // The sync object does all the hard work. We just forward to it.
    private final Sync sync = new Sync();


    public void lock() {
        sync.acquire( 1 );
    }


    public boolean tryLock() {
        return sync.tryAcquire( 1 );
    }


    public void unlock() {
        sync.release( 1 );
    }


    public Condition newCondition() {
        return sync.newCondition();
    }


    public boolean isLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }


    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }


    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly( 1 );
    }


    public boolean tryLock( long timeout, TimeUnit unit )
            throws InterruptedException {
        return sync.tryAcquireNanos( 1, unit.toNanos( timeout ) );
    }
}