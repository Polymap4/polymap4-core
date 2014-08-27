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
package org.polymap.core.model2.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.StoreProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class AssociationImpl<T extends Entity>
        implements Property<T> {

    private static Log log = LogFactory.getLog( AssociationImpl.class );
    
    private EntityRuntimeContext        context;
    
    private StoreProperty<Object>       storeProp;
    
    
    public AssociationImpl( EntityRuntimeContext context, StoreProperty storeProp ) {
        this.context = context;
        this.storeProp = storeProp;
    }


    @Override
    public String toString() {
        return "Association[name:" + getInfo().getName() + ",id=" + storeProp.get().toString() + "]";
    }
    
    
    @Override
    public T get() {
        Object id = storeProp.get();
        return (T)context.getUnitOfWork().entity( getInfo().getType(), id );
    }

    
    @Override
    public T getOrCreate( ValueInitializer<T> initializer ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void set( T value ) {
        Object id = value.id();
        storeProp.set( id );
    }


    @Override
    public PropertyInfo getInfo() {
        return storeProp.getInfo();
    }
    
}
