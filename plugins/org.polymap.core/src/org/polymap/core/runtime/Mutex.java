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

import java.util.Optional;
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

    /* The sync object does all the hard work. We just forward to it. */
    private final Sync sync = new Sync();

    @FunctionalInterface
    public interface Task<R,E extends Exception> {
        public R perform() throws E;
    }
    
    public <R,E extends Exception> Optional<R> tryLocked( long time, TimeUnit unit, Task<R,E> task ) 
            throws E, InterruptedException {
        try {
            return tryLock( time, unit )
                    ? Optional.ofNullable( task.perform() )
                    : Optional.empty();
        }
        finally {
            unlock();
        }    
    }
    
    public <R,E extends Exception> R lockedInterruptibly( Task<R,E> task ) 
            throws E, InterruptedException {
        try {
            lockInterruptibly();
            return task.perform();
        }
        finally {
            unlock();
        }    
    }
    
    public <R,E extends Exception> R locked( Task<R,E> task ) 
            throws E, InterruptedException {
        try {
            lock();
            return task.perform();
        }
        finally {
            unlock();
        }    
    }
    
    @Override
    public void lock() {
        sync.acquire( 1 );
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire( 1 );
    }

    @Override
    public void unlock() {
        sync.release( 1 );
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    public boolean isLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }

    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly( 1 );
    }

    @Override
    public boolean tryLock( long timeout, TimeUnit unit )
            throws InterruptedException {
        return sync.tryAcquireNanos( 1, unit.toNanos( timeout ) );
    }

    
    /*
     * The sync object does all the hard work. We just forward to it.
     */
    private static class Sync
            extends AbstractQueuedSynchronizer {
    
        // Reports whether in locked state
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }
    
        // Acquires the lock if state is zero
        @Override
        public boolean tryAcquire( int acquires ) {
            assert acquires == 1; // Otherwise unused
            if (compareAndSetState( 0, 1 )) {
                setExclusiveOwnerThread( Thread.currentThread() );
                return true;
            }
            return false;
        }
    
        // Releases the lock by setting state to zero
        @Override
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
}