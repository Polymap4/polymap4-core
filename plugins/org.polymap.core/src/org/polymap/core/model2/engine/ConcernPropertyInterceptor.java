/* 
 * polymap.org
 * Copyright (C) 2012-2013, Falko Bräutigam. All rights reserved.
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

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyConcern;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ConcernPropertyInterceptor<T>
        implements Property<T> {

    private Property<T>             delegate;
    
    private PropertyConcern         concern;

    private Composite               composite;
    
    
    public ConcernPropertyInterceptor( Property<T> delegate, PropertyConcern concern, Composite composite ) {
        this.delegate = delegate;
        this.concern = concern;
        this.composite = composite;
    }

    public T get() {
        return (T)concern.doGet( composite, delegate );
    }

    public void set( T value ) {
        concern.doSet( composite, delegate, value );
    }

    public T getOrCreate( ValueInitializer<T> initializer ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    public PropertyInfo getInfo() {
        return concern.doGetInfo( delegate );
    }

}
