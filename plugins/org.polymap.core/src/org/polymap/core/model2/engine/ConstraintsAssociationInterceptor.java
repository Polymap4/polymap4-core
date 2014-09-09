/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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

import org.polymap.core.model2.Association;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.EntityRepositoryImpl.EntityRuntimeContextImpl;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.runtime.ModelRuntimeException;

/**
 *
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ConstraintsAssociationInterceptor<T extends Entity>
        extends ConstraintsInterceptor<T>
        implements Association<T> {

    public ConstraintsAssociationInterceptor( Association<T> delegate, EntityRuntimeContextImpl context ) {
        super( delegate, context );
    }

    
    protected Association<T> delegate() {
        return (Association<T>)delegate;
    }

    
    @Override
    public T get() {
        T value = delegate().get();
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

}
