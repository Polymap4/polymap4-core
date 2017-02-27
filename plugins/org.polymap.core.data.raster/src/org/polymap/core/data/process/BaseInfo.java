/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.data.process;

import java.util.Optional;
import java.util.function.Supplier;

import java.lang.annotation.Annotation;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;

import oms3.annotations.Description;
import oms3.annotations.Label;
import oms3.annotations.Name;

/**
 * Base info of modules and fields.
 *
 * @author Falko Bräutigam
 */
public abstract class BaseInfo {

    /** The value of the {@link Name} annotation. */
    public Lazy<Optional<String>>   name = lazy( () -> annotation( Name.class ).map( a -> a.value() ) );
    
    /** The value of the {@link Label} annotation. */
    public Lazy<Optional<String>>   label = lazy( () -> annotation( Label.class ).map( a -> a.value() ) );
    
    /** The value of the {@link Description} annotation. */
    public Lazy<Optional<String>>   description = lazy( () -> annotation( Description.class ).map( a -> a.value() ) );
    
    
    protected <T> Lazy<T> lazy( Supplier<T> supplier ) {
        return new PlainLazyInit( supplier );
    }
    
    
    protected abstract <A extends Annotation> Optional<A> annotation(Class<A> atype);
    
}
