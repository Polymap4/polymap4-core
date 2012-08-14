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

import java.awt.Composite;

import com.google.common.base.Joiner;

import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreProperty;
import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class RecordCompositeState
        implements CompositeState {

    protected IRecordState          state;
    
    protected String                baseKey;
    
    
    protected RecordCompositeState( IRecordState state ) {
        assert state != null;
        this.state = state;
    }

    protected RecordCompositeState( IRecordState state, String baseKey ) {
        assert state != null;
        assert baseKey != null;
        this.state = state;
        this.baseKey = baseKey;
    }


    @Override
    public Object id() {
        // a non-Entity Composite property does not have an id 
        assert baseKey == null;
        return state.id();
    }

    @Override
    public Object getUnderlying() {
        // a non-Entity Composite property does not have an underlying representation 
        assert baseKey == null;
        return state;
    }

    @Override
    public StoreProperty loadProperty( PropertyInfo info ) {
        if (Composite.class.isAssignableFrom( info.getType() )) {
            return new CompositePropertyImpl( info );
        }
        else if (info.getMaxOccurs() > 1) {
            throw new RuntimeException( "Collection properties are not yet supported." );
        }
        else {
            return new PropertyImpl( info );
        }
    }


    /*
     * 
     */
    protected class PropertyImpl
            implements StoreProperty {
        
        private PropertyInfo            info;
        
        protected PropertyImpl( PropertyInfo info ) {
            this.info = info;
        }

        protected String key() {
            return Joiner.on( '/' ).skipNulls().join( baseKey, info.getNameInStore() );
        }
        
        public Object get() {
            return state.get( key() );
        }

        public void set( Object value ) {
            state.put( key(), value );
        }

        public Object newValue() {
            return getInfo().getDefaultValue();
        }

        public PropertyInfo getInfo() {
            return info;
        }
        
    }
    

    /*
     * 
     */
    class CompositePropertyImpl
            extends PropertyImpl {

        protected CompositePropertyImpl( PropertyInfo info ) {
            super( info );
        }
        
        @Override
        public CompositeState get() {
            return new RecordCompositeState( state, key() );
        }
        
        @Override
        public CompositeState newValue() {
            return new RecordCompositeState( state, key() );
        }
        
    }
    
}
