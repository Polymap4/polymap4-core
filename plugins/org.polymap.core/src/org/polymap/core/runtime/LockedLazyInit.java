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
 * This {@link LazyInit} makes sure that the variable is initialized by exactly one
 * thread. Concurrent access is synchronized via:
 * <pre>
 *     if (value == null) {
 *         synchronized (this) {
 *            if (value == null) {
 *                value = this.supplier.get();
 *            }
 *          }
 *      }
 * </pre>
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class LockedLazyInit<T>
        extends LazyInit<T> {

    public LockedLazyInit() {
        super();
    }

    public LockedLazyInit( Supplier<T> supplier ) {
        super( supplier );
    }

    public T get() {
        assert this.supplier != null : "No supplier specified.";
        return get( this.supplier );
    }

    @SuppressWarnings("hiding")
    public T get( Supplier<T> supplier ) {
        this.supplier = supplier;
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = this.supplier.get();
                }
            }
        }
        return value;
    }

}
