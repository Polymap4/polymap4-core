/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2;

import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * Bases class a computed properties. See {@link Computed} annotation. 
 *
 * @see Computed
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ComputedProperty<T>
        implements Property<T> {

    protected PropertyInfo      info;
    
    protected Composite         composite;
    
    
    public ComputedProperty( PropertyInfo info, Composite composite ) {
        this.composite = composite;
        this.info = info;
    }

    @Override
    public T createValue( ValueInitializer<T> initializer ) {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public PropertyInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        T value = get();
        return "ComputedProperty[name:" + getInfo().getName() + ",value=" + (value != null ? value.toString() : "null") + "]";
    }

}
