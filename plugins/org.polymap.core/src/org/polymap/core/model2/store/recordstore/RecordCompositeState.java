/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreCollectionProperty;
import org.polymap.core.model2.store.StoreProperty;
import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class RecordCompositeState
        implements CompositeState {
    
    public static final String      KEY_DELIMITER = "/";

    public static final String      TYPE_KEY = "_type_";
    
    public static String buildKey( String... parts ) {
        // Joiner.on( KEY_DELIMITER ).skipNulls().join( baseKey, info.getNameInStore() );
        StringBuilder result = new StringBuilder( 256 );
        for (String part : parts) {
            if (part != null && part.length() > 0) {
                if (result.length() > 0) {
                    result.append( KEY_DELIMITER );
                }
                result.append( part );
            }
        }
        return result.toString();
    }
    
    // instance *******************************************
    
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
        if (baseKey != null) {
            throw new IllegalStateException( "Composite property does not have an id." );            
        } 
        else {
            return state.id();
        }
    }

    @Override
    public Object getUnderlying() {
        // a non-Entity Composite property does not have an underlying representation 
        assert baseKey == null;
        return state;
    }

    @Override
    public StoreProperty loadProperty( PropertyInfo info ) {
        if (info.isAssociation()) {
            return new PropertyImpl( info );
        }
        else if (info.getMaxOccurs() > 1) {
            return new CollectionPropertyImpl( info ); 
        }
        else if (Composite.class.isAssignableFrom( info.getType() )) {
            return new CompositePropertyImpl( info );
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
            return buildKey( baseKey, info.getNameInStore() );
        }
        
        public Object get() {
            Object value = state.get( key() );
            if (value != null && info.getType().isEnum()) {
                value = Enum.valueOf( info.getType(), (String)value );
            }
            return value;
        }

        public void set( Object value ) {
            if (value == null) {
                state.remove( key() );
            }
            else if (value instanceof Enum) {
                state.put( key(), ((Enum)value).toString() );
            }
            else {
                state.put( key(), value );
            }
        }

        public Object createValue() {
            return getInfo().getDefaultValue();
        }

        public PropertyInfo getInfo() {
            return info;
        }
        
    }
    

    /*
     * 
     */
    protected class CompositePropertyImpl
            extends PropertyImpl {

        protected CompositePropertyImpl( PropertyInfo info ) {
            super( info );
        }
        
        @Override
        public CompositeState get() {
            Object id = state.get( buildKey( key(), "_id_" ) );
            return id != null ? new RecordCompositeState( state, key() ) : null;
        }
        
        @Override
        public CompositeState createValue() {
            state.put( buildKey( key(), "_id_" ), "created" );
            return new RecordCompositeState( state, key() );
        }

        @Override
        public void set( Object value ) {
            throw new UnsupportedOperationException( "Setting composite property is not yet supported." );
        }

    }
    

    /**
     * 
     */
    protected class CollectionPropertyImpl
            extends PropertyImpl
            implements StoreCollectionProperty {

        protected CollectionPropertyImpl( PropertyInfo info ) {
            super( info );
        }

        protected String buildCollKey( int index ) {
            return new StringBuilder( 256 ).append( key() ).append( '[' ).append( index ).append( ']' ).toString();
        }

        @Override
        public Object createValue() {
            RecordCompositeState result = new RecordCompositeState( state, buildCollKey( size() ) );
            state.put( buildKey( key(), "__size__" ), size() + 1 );
            return result;
        }

        @Override
        public int size() {
            Integer result = state.get( buildKey( key(), "__size__" ) );
            return result != null ? result : 0;
        }

        @Override
        public Iterator iterator() {
            return new Iterator() {
                private int size = size();
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index < size;
                }

                @Override
                public Object next() {
                    if (Composite.class.isAssignableFrom( getInfo().getType() )) {
                        return new RecordCompositeState( state, buildCollKey( index++ ) );
                    }
                    else {
                        return state.get( buildCollKey( index++ ) );
                    }
                }

                @Override
                public void remove() {
                    CollectionPropertyImpl.this.remove( index );
                }
            };
        }

        public void remove( int index ) {
            // shift down all fields above index
            for (int i=index; i<size()-1; i++) {
                String targetPrefix = buildCollKey( i );
                String srcPrefix = buildCollKey( i+1 );

                Iterator<Entry<String,Object>> it = state.iterator();
                Map<String,Object> newEntries = new HashMap();
                
                // create new keys/values and remove old values (don't modify while iterate)
                while (it.hasNext()) {
                    Entry<String,Object> entry = it.next();
                    if (entry.getKey().startsWith( srcPrefix )) {
                        String newKey = StringUtils.replace( entry.getKey(), srcPrefix, targetPrefix );
                        newEntries.put( newKey, entry.getValue() );
                        it.remove();
                    }
                }
                // add new entries
                for (Entry<String,Object> entry : newEntries.entrySet()) {
                    state.put( entry.getKey(), entry.getValue() );
                }
            }
            // delete last element's/Composite's fields
            String lastPrefix = buildCollKey( size()-1 );
            Iterator<Entry<String,Object>> it = state.iterator();
            while (it.hasNext()) {
                Entry<String,Object> entry = it.next();
                if (entry.getKey().startsWith( lastPrefix )) {
                    it.remove();
                }
            }
            // adjust size field
            state.put( buildKey( key(), "__size__" ), size() - 1 );            
        }
        
        @Override
        public boolean add( Object o ) {
            state.put( buildCollKey( size() ), o );
            state.put( buildKey( key(), "__size__" ), size() + 1 );
            return true;
        }

    }
    
}
