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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreCollectionProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CompositeCollectionPropertyImpl<T extends Composite>
        extends CollectionPropertyImpl<T> {

    private static Log log = LogFactory.getLog( CompositeCollectionPropertyImpl.class );

    private EntityRuntimeContext            entityContext;

    
    public CompositeCollectionPropertyImpl( EntityRuntimeContext entityContext, StoreCollectionProperty storeProp ) {
        super( storeProp );
        this.entityContext = entityContext;
    }

    
    @Override
    public T createElement( ValueInitializer<T> initializer ) {
        CompositeState state = (CompositeState)storeProp.createValue();
        InstanceBuilder builder = new InstanceBuilder( entityContext );
        Composite value = builder.newComposite( state, getInfo().getType() );
        
        if (initializer != null) {
            try {
                value = initializer.initialize( (T)value );
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new ModelRuntimeException( e );
            }
        }
        return (T)value;
    }


    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            
            private Iterator    storeIt = storeProp.iterator();

            @Override
            public boolean hasNext() {
                return storeIt.hasNext();
            }

            @Override
            public T next() {
                CompositeState state = (CompositeState)storeIt.next();
                InstanceBuilder builder = new InstanceBuilder( entityContext );
                return (T)builder.newComposite( state, getInfo().getType() );
            }

            @Override
            public void remove() {
                throw new RuntimeException( "not yet implemented." );
            }
        };
    }


    @Override
    public boolean add( T e ) {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public boolean addAll( Collection<? extends T> c ) {
        throw new RuntimeException( "not yet implemented." );
    }



}
