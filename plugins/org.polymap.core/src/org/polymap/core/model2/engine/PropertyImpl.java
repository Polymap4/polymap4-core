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
package org.polymap.core.model2.engine;

import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.StoreProperty;

/**
 * Property implementation for simple (non-Composite) values.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class PropertyImpl<T>
        implements Property<T> {

    private StoreProperty<T>        delegate;

    
    protected PropertyImpl( StoreProperty<T> underlying ) {
        this.delegate = underlying;
    }

    protected StoreProperty<T> delegate() {
        return delegate;
    }
    
    @Override
    public T get() {
        // no cache here; the store should decide when and what to cache.
        return delegate.get();
    }

    @Override
    public T getOrCreate( ValueInitializer<T> initializer ) {
        T result = get();
        if (result == null) {
            synchronized (this) {
                result = get();
                if (result == null) {
                    result = delegate.newValue();
                    if (initializer != null) {
                        try {
                            result = initializer.initialize( result );
                        }
                        catch (RuntimeException e) {
                            throw e;
                        }
                        catch (Exception e) {
                            throw new ModelRuntimeException( e );
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void set( T value ) {
        delegate.set( value );
    }

    @Override
    public PropertyInfo getInfo() {
        return delegate.getInfo();
    }

}
