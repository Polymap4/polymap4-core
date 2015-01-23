/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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

import java.util.function.Supplier;

/**
 * Provides a lazily initialized variable. Using a {@link Lazy} keeps the client code
 * independent from the actual check/set logic provided by the different
 * implementations.
 * <p/>
 * {@link LazyInit} implements {@link Supplier}. This allows to 'stack' several
 * implementations to combine their behaviour. For example a JobLazyInit on a
 * {@link LockedLazyInit} results in a lazily initialized variable that is guaranteed
 * to be initialized just ones *and* that is initialized in a separately running job.
 * 
 * @see LazyInit
 * @see LockedLazyInit
 * @see PlainLazyInit
 * @see Atomically
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Lazy<T> {

    /**
     * Gets the value of this variable. The value is created/loaded if necessary
     * using the {@link Supplier} given to the constructor.
     * 
     * @throws AssertionError If no supplier was given to the constructor.
     */
    public T get();


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
    public T get( Supplier<T> supplier );


    /**
     * 
     */
    public void clear();

    
    /**
     * Returns true if the variable is currently initialized. That is, the next call
     * of {@link #get()} is <b>supposed</b> to return without calling the supplier.
     */
    public boolean isInitialized();

}
