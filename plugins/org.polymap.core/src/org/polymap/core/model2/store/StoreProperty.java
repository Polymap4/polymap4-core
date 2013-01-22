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
package org.polymap.core.model2.store;

import org.polymap.core.model2.runtime.PropertyInfo;

/**
 *
 * @param <T> Simple types: primitive type, String or Date; or {@link CompositeState}.
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface StoreProperty<T> {

    public T get();
    
    public void set( Object value );


    /**
     * Creates a new value for this property. For simple properties usually this is
     * just {@link PropertyInfo#getDefaultValue()}. For {@link CompositeState} value this
     * is a new {@link CompositeState}.
     * 
     * @return Newly created value for this property.
     */
    public T newValue();

    public PropertyInfo getInfo();
}
