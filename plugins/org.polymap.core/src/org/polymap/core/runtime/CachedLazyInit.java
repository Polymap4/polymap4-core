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

import java.util.function.Supplier;

import java.lang.ref.SoftReference;

import org.polymap.core.runtime.cache.EvictionListener;

/**
 * Provides a pseudo-persistent lazily initialized variable. Ones initialized the
 * value is stored in a {@link SoftReference}. The cache may decide to evict this value
 * at any time if memory is low (or for any other reason). In this case the next access
 * triggers the supplier again.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CachedLazyInit<T>
        extends LazyInit<T> {
    
    private volatile SoftReference<T>       ref;

    /**
     * 
     * @param elementSize The size of the value in the cache in bytes.
     * @param supplier
     */
    public CachedLazyInit( Supplier<T> supplier ) {
        super( supplier );
    }

    /**
     * This ctor allows to preset the value. This can be used if the value is
     * available when initialized but may be reclaimed during processing.
     */
    public CachedLazyInit( T value, Supplier<T> supplier ) {
        super( supplier );
        ref = new SoftReference( value );
    }

    @Override
    @SuppressWarnings("hiding")
    public T get( final Supplier<T> supplier ) {
        // make a strong reference to keep value from GC'ed during method
        T value = ref != null ? ref.get() : null;
        
        if (value == null) {
            synchronized (this) {
                value = ref != null ? ref.get() : null;
                if (value == null) {
                    value = supplier.get();
                    ref = new SoftReference( value );
                }
            }
        }
        return value;
    }

    @Override
    public void clear() {
        ref = null;
    }

    @Override
    public boolean isInitialized() {
        // avoid race cond between the check and the call
        SoftReference<T> localRef = ref;
        return localRef != null && localRef.get() != null;
    }

    @Override
    protected void finalize() throws Throwable {
        clear();
    }
    
    
    /**
     * 
     */
    public static interface EvictionSupplier<T>
            extends Supplier<T> {
        
        EvictionListener evictionListener();
        
    }
    
}
