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
package org.polymap.core.runtime;

import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * EXPERIMENTAL test code to implement a {@link BlockingQueue} based on a
 * {@link ConcurrentMap}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapBlockingQueue<T>
        implements BlockingQueue<T> {

    private static Log log = LogFactory.getLog( MapBlockingQueue.class );

    /**
     * 
     */
    final class AtomicCounter {
        
        private volatile int    value = 0;
        
        private volatile int    waiters = 0;
        
        public Object get() {
            return value;
        }

        public int inc() {
            int result = ++value;
            if (waiters > 0) {
                synchronized (this) { notifyAll(); }
            }
            return result;
        }
        
        public void await( Condition cond ) {
            if (!cond.isMet()) {
                synchronized (this) {
                    while (!cond.isMet()) {
                        try {
                            waiters ++;
                            wait();
                        }
                        catch (InterruptedException e) {
                        }
                        finally {
                            waiters --;
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * 
     */
    interface Condition {
        
        boolean isMet();
    }
    

    // instance *******************************************
    
    private ConcurrentMap<Integer,T>    map;
    
    private Collection<T>       collDelegate;

    private AtomicCounter       head = new AtomicCounter();
    
    private AtomicCounter       tail = new AtomicCounter();

    private final int           capacity;

    private Condition           notFull;

    private Condition           notEmpty;
    
    
    public MapBlockingQueue( int capacity ) {
        this.map = new ConcurrentHashMap( capacity*2, 0.75f, 8 );
        this.capacity = capacity;
        this.collDelegate = map.values();
        
        notFull = new Condition() {
            public boolean isMet() {
                return size() < MapBlockingQueue.this.capacity;
            }
        };
        notEmpty = new Condition() {
            public boolean isMet() {
                return !isEmpty();
            }
        };
    }
    
    public boolean offer( T elm ) {
        if (notFull.isMet()) {
            map.put( head.inc(), elm );
            return true;
        }
        else {
            return false;
        }
    }

    public boolean offer( T e, long timeout, TimeUnit unit )
    throws InterruptedException {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean add( T elm ) {
        if (!offer( elm )) {
            throw new IllegalStateException( "Capacity reached: " + size() + "/" + capacity );
        }
        return true;
    }

    public boolean remove( Object o ) {
        throw new UnsupportedOperationException( "remove()" );
    }

    public T remove() {
        T result = poll();
        if (result == null) {
            throw new EmptyStackException();
        }
        return result;
    }

    public void put( T elm ) throws InterruptedException {
        tail.await( notFull );
        map.put( head.inc(), elm );
    }

    public T poll() {
        return notEmpty.isMet() ? map.remove( tail.inc() ) : null;
    }

    public T element() {
        T result = peek();
        if (result == null) {
            throw new IllegalStateException( "Queue is empty." );
        }
        return result;
    }

    public T peek() {
        return notEmpty.isMet() ? map.get( tail.get() ) : null;
    }

    public T take() throws InterruptedException {
        head.await( notEmpty );
        return map.remove( tail.inc() );
    }

    public T poll( long timeout, TimeUnit unit ) throws InterruptedException {
        throw new RuntimeException( "not yet implemented." );
    }

    public int remainingCapacity() {
        return capacity - size();
    }

    public int drainTo( Collection<? super T> c ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public int drainTo( Collection<? super T> c, int maxElements ) {
        throw new RuntimeException( "not yet implemented." );
    }

    
    // Collection ***************************************************
    
    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains( Object o ) {
        return collDelegate.contains( o );
    }

    public Iterator<T> iterator() {
        return collDelegate.iterator();
    }

    public Object[] toArray() {
        return collDelegate.toArray();
    }

    public <A> A[] toArray( A[] a ) {
        return collDelegate.toArray( a );
    }

    public boolean containsAll( Collection<?> c ) {
        return collDelegate.containsAll( c );
    }

    public boolean addAll( Collection<? extends T> c ) {
        return collDelegate.addAll( c );
    }

    public boolean removeAll( Collection<?> c ) {
        return collDelegate.removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return collDelegate.retainAll( c );
    }

    public void clear() {
        map.clear();
    }

}
