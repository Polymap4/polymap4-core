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
 * This simple implementation can be used for a single thread only.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PlainLazyInit<T>
        extends LazyInit<T> {

    protected volatile T        value;
    
    
    public PlainLazyInit() {
        super();
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
    public PlainLazyInit( Supplier<T> supplier ) {
        super( supplier );
    }

    
    @SuppressWarnings("hiding")
    public T get( Supplier<T> supplier ) {
        this.supplier = supplier;
        if (value == null) {
            value = this.supplier.get();
        }
        return value;
    }

    
    public void clear() {
        value = null;
    }

}
