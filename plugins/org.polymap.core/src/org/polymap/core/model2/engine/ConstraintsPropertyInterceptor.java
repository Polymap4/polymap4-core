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

import org.polymap.core.model2.Property;
import org.polymap.core.model2.engine.EntityRepositoryImpl.EntityRuntimeContextImpl;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ConstraintsPropertyInterceptor<T>
        extends ConstraintsInterceptor<T>
        implements Property<T> {

    public ConstraintsPropertyInterceptor( Property<T> delegate, EntityRuntimeContextImpl context ) {
        super( delegate, context );
    }

    
    protected Property<T> delegate() {
        return (Property<T>)delegate;
    }
    
    
    @Override
    public T get() {
        T value = delegate().get();
        
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

    
    @Override
    public void set( T value ) {
        context.checkEviction();
        
        if (isImmutable) {
            throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
        }
        if (!isNullable && value == null) {
            throw new ModelRuntimeException( "Property is not @Nullable: " + fullPropName() );
        }
        delegate().set( value );
        
        context.raiseStatus( EntityStatus.MODIFIED );
    }

    
    @Override
    public T createValue( ValueInitializer<T> initializer ) {
        return delegate().createValue( initializer );
    }


    @Override
    public String toString() {
        return delegate().toString();
    }

}
