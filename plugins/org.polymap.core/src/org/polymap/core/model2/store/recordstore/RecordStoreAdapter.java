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
package org.polymap.core.model2.store.recordstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.Property.PropertyInfo;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.store.PropertyDescriptor;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.model2.store.StoreSPI;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RecordStoreAdapter
        implements StoreSPI {

    private static Log log = LogFactory.getLog( RecordStoreAdapter.class );
    
    protected EntityRepository      repo;
    
    protected IRecordStore          store;

    private StoreRuntimeContext     context;
    
    
    public RecordStoreAdapter( IRecordStore store ) {
        this.store = store;
    }


    @SuppressWarnings("hiding")
    public void init( StoreRuntimeContext context ) {
        this.context = context;
        this.repo = context.getRepository();
    }


    public void close() {
    }


    public Object stateId( Object state ) {
        return ((IRecordState)state).id();
    }


    public Property createProperty( PropertyDescriptor descriptor ) {
        if (descriptor.getParent() != null) {
            throw new UnsupportedOperationException( "Complex FeatureType is not supported yet." );
        }
        return new PropertyImpl( descriptor );
    }

    
    public UnitOfWork createUnitOfWork() {
        return new RecordStoreUnitOfWork( context, this );
    }


    /*
     * 
     */
    static final class PropertyImpl
            implements Property, PropertyInfo {
        
        private String                  key;

        private IRecordState            state;

        protected PropertyImpl( PropertyDescriptor descriptor ) {
            this.state = (IRecordState)descriptor.getContext().state();
            this.key = descriptor.getNameInStore();
        }

        public Object get() {
            return  state.get( key );
        }

        public void set( Object value ) {
            state.put( key, value );
        }

        public PropertyInfo getInfo() {
            return this;
        }
        
        public String getName() {
            return key;
        }

        public Entity getEntity() {
            throw new RuntimeException( "Not yet implemented." );
        }

    }
    
}
