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
package org.polymap.core.model2.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.polymap.core.model2.PropertyConcern;
import org.polymap.core.model2.PropertyConcernAdapter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class InvocationCountConcern
        extends PropertyConcernAdapter
        implements PropertyConcern {

    public static AtomicInteger     getCount = new AtomicInteger();
    
    public static AtomicInteger     setCount = new AtomicInteger();

    @Override
    public Object get() {
        getCount.incrementAndGet();
        return delegate().get();
    }

    @Override
    public void set( Object value ) {
        setCount.incrementAndGet();
        delegate().set( value );
    }

}
