/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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

import java.util.ArrayList;
import java.util.Iterator;

import java.lang.ref.WeakReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread-safe implementation of a listener list.
 * <p/>
 * This listener list supports adjustable listener comparators. A listener comparator
 * decides if any two listeners are "dublicates". Dublicates are not added to the list.
 * There are {@link #EQUALITY} and {@link #IDENTITY} as default comparators.
 * <p/>
 * This listener list support different kinds of listener references. There are the
 * {@link #STRONG} and the {@link #WEAK} default reference factories. Different reference
 * types can be mixed in one list.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ListenerList<T>
        implements Iterable<T> {

    private static Log log = LogFactory.getLog( ListenerList.class );

    // comparator *****************************************

    /**
     *
     */
    public interface Comparator {
        public boolean isSame( Object lhs, Object rhs, ListenerList list );
    }

    /**
     * Mode indicating that listeners should be considered the <a
     * href="#same">same</a> if they are equal.
     */
    public static final Comparator EQUALITY = new Comparator() {
        public boolean isSame( Object lhs, Object rhs, ListenerList list ) {
            if (lhs instanceof ListenerReference) {
                lhs = ((ListenerReference)lhs).getDelegate( list );
            }
            if (rhs instanceof ListenerReference) {
                rhs = ((ListenerReference)rhs).getDelegate( list );
            }
            return lhs != null && rhs != null && lhs.equals( rhs );
        }
    };

    /**
     * Mode indicating that listeners should be considered the <a
     * href="#same">same</a> if they are identical.
     */
    public static final Comparator IDENTITY = new Comparator() {
        public boolean isSame( Object lhs, Object rhs, ListenerList list ) {
            if (lhs instanceof ListenerReference) {
                lhs = ((ListenerReference)lhs).getDelegate( list );
            }
            if (rhs instanceof ListenerReference) {
                rhs = ((ListenerReference)rhs).getDelegate( list );
            }
            return lhs == rhs;
        }
    };

    // reference factory **********************************

    /**
     *
     */
    public interface ListenerReferenceFactory {
        public <T> T newReference( T listener );
    }


    /**
     * This factory creates strong references for listeners.
     */
    public static final ListenerReferenceFactory STRONG = new ListenerReferenceFactory() {
        public <T> T newReference( T listener ) {
            return listener;
        }
    };

    /**
     * This factory creates weak references for listeners. Weak references are reclaimed
     * by the GC as soon as there are no strong references pointing to them.
     *
     * @see WeakReference
     */
    public static final ListenerReferenceFactory WEAK = new ListenerReferenceFactory() {
        public <T> T newReference( T listener ) {
            return listener instanceof ListenerReference
                ? listener : WeakListener.forListener( listener );
        }
    };

    private static final Object[] EMPTY = new Object[0];


    // instance *******************************************

    private Comparator                  comparator;

    private T[]                         list = (T[])EMPTY;

    private ListenerReferenceFactory    refFactory;


    /**
     * Creates a new instance with comparator {@link #IDENTITY} and
     * {@link STRONG} factory for listener references.
     */
    public ListenerList() {
        this( IDENTITY, STRONG );
    }


    /**
     * Creates a new instance with the given comparator.
     */
    public ListenerList( Comparator comparator ) {
        this( comparator, STRONG );
    }


    /**
     * Creates a new instance with the given comparator and the given
     * factory fpr listener references.
     */
    public ListenerList( Comparator comparator, ListenerReferenceFactory refFactory ) {
        this.comparator = comparator;
        this.refFactory = refFactory;
    }


    public int size() {
       return list.length;    
    }
    
    public void clear() {
        list = (T[])EMPTY;
    }


    /**
     * Adds a listener to this list. This method has no effect if the <a href="#same">same</a>
     * listener is already registered.
     *
     * @param listener The non-<code>null</code> listener to add.
     */
    public boolean add( T listener ) {
        return add( refFactory, listener );
    }


    /**
     * This method works as the {@link #add(Object)} method, except that the given
     * {@link ListenerReferenceFactory} is used to create the reference for the listener.
     * This can be used to create a {@link #WEAK} reference, so that the listener is automatically
     * reclaimed by GC and deregistered from this list.
     *
     * @param factory
     * @param listener
     */
    public boolean add( ListenerReferenceFactory factory, T listener ) {
        // This method is synchronized to protect against multiple threads adding
        // or removing listeners concurrently. This does not block concurrent readers.
        assert listener != null;

        // Thread safety: create new array to avoid affecting concurrent readers
        ArrayList<T> newList = new ArrayList( list.length + 1 );
        for (T elm : list) {
            if (elm instanceof ListenerReference) {
                elm = (T)((ListenerReference)elm).getDelegate( this );
                // skip reclaimed references
                if (elm == null) {
                    continue;
                }
            }
            // check for duplicates
            if (comparator.isSame( listener, elm, this )) {
                return false;
            }
            newList.add( elm );
        }
        newList.add( factory.newReference( listener ) );

        //atomic assignment
        this.list = (T[])newList.toArray();
        return true;
    }


    /**
     * Removes a listener to this list. This method has no effect if the <a href="#same">same</a>
     * listener was not already registered.
     *
     * @param listener The non-<code>null</code> listener to remove.
     */
    public synchronized boolean remove( T listener ) {
        // This method is synchronized to protect against multiple threads adding
        // or removing listeners concurrently. This does not block concurrent readers.
        assert listener != null;

        // Thread safety: create new array to avoid affecting concurrent readers
        ArrayList<T> newList = new ArrayList( Math.max( list.length - 1, 0 ) );

        boolean found = false;
        for (T elm : list) {
            if (elm instanceof ListenerReference) {
                elm = (T)((ListenerReference)elm).getDelegate( this );
                // skip reclaimed references
                if (elm == null) {
                    continue;
                }
            }
            if (comparator.isSame( listener, elm, this )) {
                found = true;
            }
            else {
                newList.add( elm );
            }
        }
        if (!found) {
            log.warn( "!!! Listener not found to remove: " + listener );
        }
        else {
            //atomic assignment
            this.list = (T[])(newList.isEmpty() ? EMPTY : newList.toArray());
        }

        return found;
    }


    public Iterator<T> iterator() {
        return new ListenerListIterator<T>( list );
    }


    public Iterable<T> getListeners() {
        return this;
    }


    /**
     * An Iterator is needed when {@link WeakReference}s are used to check if an
     * entry is still valid when it is requested.
     */
    class ListenerListIterator<T>
            implements Iterator<T> {

        /** Reference to my version of the list. */
        private T[]                         list;

        /** Iteration counter in the {@link #list}. */
        private int                         index = 0;

        /** Strong reference to the next element. */
        private T                           next;


        ListenerListIterator( T[] list ) {
            this.list = list;
        }

        public boolean hasNext() {
            if (next != null) {
                return true;
            }
            while (next == null && index < list.length) {
                next = list[index++];
                if (next instanceof ListenerReference) {
                    next = (T)((ListenerReference)next).getDelegate( ListenerList.this );
                    if (next == null) {
                        log.info( "GC has reclaimed listener!" );
                    }
                }
            }
            return next != null;
        }

        public T next() {
            try {
                return next;
            }
            finally {
                next = null;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
