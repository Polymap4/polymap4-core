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

import com.google.common.base.Supplier;

/**
 * Provides a lazily initialized (cache) variable.
 * <p/>
 * Using a LazyInit keeps the client code independent from the initialization
 * logic.
 * <p/>
 * {@link LazyInit} implements {@link Supplier}. This allows to 'stack' several
 * implementations to combine their behaviour. For example a JobLazyInit on a
 * {@link LockedLazyInit} results in a lazily initialized variable that is guaranteed
 * to be initialized just ones *and* that is initialized in a separately running job.
 * 
 * @see Atomically
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class LazyInit<T>
        implements Supplier<T> {

    protected T                 value;
    
    protected Supplier<T>       supplier;
    
    
    public LazyInit() {
    }
    
    public LazyInit( Supplier<T> supplier ) {
    }
    
    public abstract T get();

    @SuppressWarnings("hiding")
    public abstract T get( Supplier<T> supplier );

}
