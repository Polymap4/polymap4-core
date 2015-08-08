/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.recordstore;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import java.lang.reflect.Constructor;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public abstract class RecordModel {

    /**
     * Allows to access the properties of the model (name, type(?)) in a static way.
     *
     * @param <M> The state model type.
     * @param cl The state model class.
     * @return A new type instance with null state.
     */
    public static <M extends RecordModel> M type( Class<M> cl ) {
        IRecordState nullState = new IRecordState() {
            public IRecordState add( String key, Object value ) {
                throw new RuntimeException( "Not allowed for TYPE state." );
            }
            public <T> T get( String key ) {
                throw new RuntimeException( "Not allowed for TYPE state." );
            }
            public <T> List<T> getList( String key ) {
                throw new RuntimeException( "Not allowed for TYPE state." );
            }
            public Object id() {
                throw new RuntimeException( "Not allowed for TYPE state." );
            }
            public Iterator<Entry<String, Object>> iterator() {
                throw new RuntimeException( "Not allowed for TYPE state." );
            }
            public <T> IRecordState put( String key, T value ) {
                throw new RuntimeException( "Not allowed for TYPE state." );
            }
            public IRecordState remove( String key ) {
                throw new RuntimeException( "Not allowed for TYPE state." );
            }            
        };

        try {
            Constructor<M> ctor = cl.getDeclaredConstructor( new Class[] { IRecordState.class } );
            return ctor.newInstance( new Object[] { nullState } );
        }
        catch (Exception e) {
            // try no-arg ctor
            try {
                Constructor<M> ctor = cl.getDeclaredConstructor( ArrayUtils.EMPTY_CLASS_ARRAY );
                return ctor.newInstance( ArrayUtils.EMPTY_OBJECT_ARRAY );
            }
            catch (Exception e2) {
                throw new RuntimeException( e );
            }
        }
    }
    
 
    // instance *******************************************
    
    private IRecordState                state;
    
    
    protected RecordModel( IRecordState record ) {
        assert record != null : "record argument is null";
        this.state = record;
    }

    public IRecordState state() {
        return state;
    }


    /**
     * Models a property of an {@link RecordModel} with a given name and type.
     * {@link Property} holds the name of the property to be used as name in the
     * {@link IRecordState} and exposes methods for typesafe access to the value of
     * the property.
     */
    public class Property<T> {
        
        private String              name;
        
        public Property( String key ) {
            this.name = key;    
        }
        
        public String name() {
            return name;
        }
        
        public T get() {
            return state.get( name );
        }

        public List<T> getList() {
            return state.getList( name );
        }

        /**
         * @see IRecordState#put(String, Object)
         */
        public RecordModel put( T value ) {
            state.put( name, value );
            return RecordModel.this;
        }

        /**
         * @see IRecordState#put(String, Object)
         */
        public RecordModel add( T value ) {
            state.add( name, value );
            return RecordModel.this;
        }
    }
    
}
