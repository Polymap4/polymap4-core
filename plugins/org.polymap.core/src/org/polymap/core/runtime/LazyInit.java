/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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

import com.google.common.base.Supplier;

/**
 * Provides a lazily initialized variable. Using a LazyInit keeps the client code
 * independent from the actual check/set logic provided by the different
 * implementations.
 * <p/>
 * {@link LazyInit} implements {@link Supplier}. This allows to 'stack' several
 * implementations to combine their behaviour. For example a JobLazyInit on a
 * {@link LockedLazyInit} results in a lazily initialized variable that is guaranteed
 * to be initialized just ones *and* that is initialized in a separately running job.
 * 
 * @see LockedLazyInit
 * @see PlainLazyInit
 * @see Atomically
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public abstract class LazyInit<T>
        implements Supplier<T> {

    protected Supplier<T>       supplier;
    
    
    protected LazyInit() {
    }


    /**
     * Constructs a new instance.
     * <p/>
     * The code of the given {@link Supplier} has to be thread-safe as it may be
     * called from different threads concurrently. It is also possible that the
     * Supplier is called (concurrently) for the same key.
     * 
     * @param supplier The supplier to use to load the value.
     */
    protected LazyInit( Supplier<T> supplier ) {
        this.supplier = supplier;
    }

    
    /**
     * Gets the value of this variable. The value is created/loaded if necessary
     * using the {@link Supplier} given to the constructor.
     * 
     * @throws AssertionError If no supplier was given to the constructor.
     */
    public T get() {
        assert this.supplier != null : "No supplier specified.";
        return get( this.supplier );
    }


    /**
     * Gets the value of this variable. The value is created/loaded if necessary
     * using the given {@link Supplier}.
     * <p/>
     * The code of the Supplier has to be <b>thread-safe</b>. It also has to be aware
     * that different Suppliers initialized from different threads are called for the
     * <b>same key</b>! It is up to the specific implementations of the LazyInit to
     * decide to use this to avoid synchronizing/blocking threads. However, just one
     * of the created values is returned to all threads.
     * 
     * @param supplier The supplier to use to load the value if nessecary.
     * @return The value of this variable.
     */
    @SuppressWarnings("hiding")
    public abstract T get( Supplier<T> supplier );


    /**
     * 
     */
    public abstract void clear();

    
    /**
     * Returns true if the variable is currently initialized. That is, the next call
     * of {@link #get()} is <b>supposed</b> to return without calling the supplier.
     */
    public abstract boolean isInitialized();
    
}
