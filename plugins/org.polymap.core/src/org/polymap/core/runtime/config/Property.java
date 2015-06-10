/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A configuration property.
 * <p/>
 * T - The type of the value of this property. 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Property<T> {

    /**
     * Attempts to set the value of this Property.
     *
     * @param newValue
     * @return The previous value of this Property.
     */
    public T set( T newValue );

    public T get();
    
    public boolean isPresent();
    
    public void ifPresent( Consumer<T> consumer );
    
    public T orElse( T other );
    
    public T orElse( Supplier<T> supplier );
    
    public PropertyInfo info();
    
}
