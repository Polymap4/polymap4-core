/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import javax.annotation.Nullable;

import org.polymap.core.model2.DefaultValue;
import org.polymap.core.model2.Immutable;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.engine.EntityRepositoryImpl.EntityRuntimeContextImpl;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.runtime.LazyInit;

/**
 * Handles property contraints: {@link DefaultValue}, {@link DefaultValues},
 * {@link Immutable}, {@link Nullable}. This also raises the status of the
 * Entity when a property is modified.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class ConstraintsInterceptor<T>
        implements Property<T> {

    protected static final Object   UNINITIALIZED = new Object();
    
    private EntityRuntimeContextImpl context;
    
    private Property<T>             delegate;

    /** Store in variable for fast access. */
    private boolean                 isImmutable;
    
    private boolean                 isNullable;

    /**
     * Lazily init variable for fast access when frequently used. Don't use cool
     * {@link LazyInit} in order to save memory (one more Object per property
     * instance).
     */
    private Object                  defaultValue = UNINITIALIZED;
    
    
    public ConstraintsInterceptor( Property<T> delegate, EntityRuntimeContextImpl context ) {
        this.delegate = delegate;
        this.context = context;
        PropertyInfo info = delegate.getInfo();
        this.isImmutable = info.isImmutable();
        this.isNullable = info.isNullable();
    }

    protected String fullPropName() {
        return context.getInfo().getName() + "." + getInfo().getName();
    }
    
    
    public T get() {
        T value = delegate.get();
        
        // check/init default value
        if (value == null) {
            if (defaultValue == UNINITIALIZED) {
                // not synchronized; concurrent inits are ok here 
                defaultValue = delegate.getInfo().getDefaultValue();
            }
            value = (T)defaultValue;
        }
        // check Nullable
        if (value == null && !isNullable) {
            throw new ModelRuntimeException( "Property is not @Nullable: " + fullPropName() );
        }
        return value;
    }

    
    public void set( T value ) {
        context.checkEviction();
        
        if (isImmutable) {
            throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
        }
        if (!isNullable && value == null) {
            throw new ModelRuntimeException( "Property is not @Nullable: " + fullPropName() );
        }
        delegate.set( value );
        
        context.raiseStatus( EntityStatus.MODIFIED );
    }

    
    public T getOrCreate( ValueInitializer<T> initializer ) {
        return delegate.getOrCreate( initializer );
    }


    public PropertyInfo getInfo() {
        return delegate.getInfo();
    }

}
