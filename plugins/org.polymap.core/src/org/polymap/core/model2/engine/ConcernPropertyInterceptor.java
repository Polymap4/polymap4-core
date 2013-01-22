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
package org.polymap.core.model2.engine;

import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyConcern;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
final class ConcernPropertyInterceptor<T>
        implements Property<T> {

    private Property<T>             delegate;
    
    private PropertyConcern         concern;
    
    
    public ConcernPropertyInterceptor( Property<T> delegate, PropertyConcern concern ) {
        this.delegate = delegate;
        this.concern = concern;
    }

    public T get() {
        return (T)concern.doGet( delegate );
    }

    public void set( T value ) {
        concern.doSet( delegate, value );
    }

    public T getOrCreate( ValueInitializer<T> initializer ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    public PropertyInfo getInfo() {
        return concern.doGetInfo( delegate );
    }

}
