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

import java.util.Objects;

/**
 * Handle {@link Immutable} properties. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImmutableConcern<T>
        extends DefaultPropertyConcern<T>
        implements PropertyConcern<T> {

    private boolean     modified;
    
    
    @Override
    public T doSet( Object obj, Config<T> prop, T value ) {
        if (modified) {
            throw new ConfigurationException( "Property is @Immutable: " + prop.info().getName() + " current value: " + value );
        }
        if (!Objects.equals( prop.get(), value )) {
            modified = true;
        }
        return value;
    }
    
}
