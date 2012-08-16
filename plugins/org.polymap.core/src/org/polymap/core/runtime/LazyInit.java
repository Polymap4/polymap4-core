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
 * Provides a lazily initialized variable. Using a LazyInit keeps the client code
 * independent from the actual check/set logic provided by this class or sub classes.
 * <p/>
 * This default implementation does no checks before set whatoever. Consider using
 * sub classes for specific behaviour.
 * <p/>
 * {@link LazyInit} implements {@link Supplier}. This allows to 'stack' several
 * implementations to combine their behaviour. For example a JobLazyInit on a
 * {@link LockedLazyInit} results in a lazily initialized variable that is guaranteed
 * to be initialized just ones *and* that is initialized in a separately running job.
 * 
 * @see LockedLazyInit
 * @see Atomically
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LazyInit<T>
        implements Supplier<T> {

    protected T                 value;
    
    protected Supplier<T>       supplier;
    
    
    public LazyInit() {
    }
    
    public LazyInit( Supplier<T> supplier ) {
        this.supplier = supplier;
    }

    public T get() {
        assert this.supplier != null : "No supplier specified.";
        return get( this.supplier );
    }

    @SuppressWarnings("hiding")
    public T get( Supplier<T> supplier ) {
        this.supplier = supplier;
        if (value == null) {
            value = this.supplier.get();
        }
        return value;
    }

}
