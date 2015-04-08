/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.lang.reflect.Field;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConfigurationFactory {

    /**
     * Creates a new configuration of the given type.
     */
    public static <T> T create( Class<T> cl ) throws ConfigurationException {
        try {
            T instance = cl.newInstance();
            return inject( instance );
        }
        catch (ReflectiveOperationException e) {
            throw new ConfigurationException( e );
        }
    }
    
    
    /**
     * Injects {@link Property} instances into the given object.
     */
    public static <T> T inject( T instance ) throws ConfigurationException {
        try {
            // init properties
            for (Field f : instance.getClass().getDeclaredFields()) {
                if (f.getType().isAssignableFrom( Property.class )) {
                    f.setAccessible( true );
                    f.set( instance, new PropertyImpl<Object,T>( instance, f ) );
                }
            }
            return instance;
        }
        catch (Exception e) {
            throw new ConfigurationException( e );
        }
    }


    /**
     * 
     */
    private static class PropertyImpl<C,T>
            implements Property2<C,T> {
    
        private T           value;
        
        private C           instance;

        private Field       f;
    
    
        protected PropertyImpl( C instance, Field f ) {
            this.instance = instance;
            this.f = f;
            initDefaultValue();
        }


        @Override
        public C fset( T newValue ) {
            this.value = newValue;
            return instance;
        }
    
    
        @Override
        public T set( T newValue ) {
            checkValidators( newValue );
            T previous = value;
            value = newValue;
            return previous;
        }
    
    
        @Override
        public T get() {
            if (value == null && f.getAnnotation( Mandatory.class ) != null) {
                throw new ConfigurationException( "Configuration property is @Mandatory: " + f.getName() );
            }
            return value;
        }
        
        
        protected void initDefaultValue() {
            assert value == null;
            DefaultValue defaultValue = f.getAnnotation( DefaultValue.class );
            if (defaultValue != null) {
                set( (T)defaultValue.value() );
            }
            DefaultDouble defaultDouble = f.getAnnotation( DefaultDouble.class );
            if (defaultDouble != null) {
                set( (T)new Double( defaultDouble.value() ) );
            }
            DefaultBoolean defaultBoolean = f.getAnnotation( DefaultBoolean.class );
            if (defaultBoolean != null) {
                set( (T)new Boolean( defaultBoolean.value() ) );
            }
        }
        
        
        protected void checkValidators( T checkValue ) {
            // get annotation(s)
            List<Check> l = new ArrayList();
            Check check = f.getAnnotation( Check.class );
            if (check != null) {
                l.add( check );
            }
            Checks checks = f.getAnnotation( Checks.class );
            if (checks != null) {
                l.addAll( Arrays.asList( checks.value() ) );
            }

            // test validators
            for (Check c : l) {
                try {
                    PropertyValidator validator = c.validator().newInstance();
                    validator.args = c.args();
                    if (validator.test( checkValue ) == false) {
                        throw new IllegalStateException( "Property check/validator failed: " 
                                + validator.getClass().getName() 
                                + ", value=" + checkValue
                                + ", args=" + Arrays.toString( validator.args ) );
                    }
                }
                catch (ReflectiveOperationException e) {
                    throw new RuntimeException( e );
                }
            }
        }
    }
    
}
