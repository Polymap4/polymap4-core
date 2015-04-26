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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
            for (Class cl = instance.getClass(); cl != null; cl = cl.getSuperclass()) {
                for (Field f : cl.getDeclaredFields()) {
                    if (Property.class.isAssignableFrom( f.getType() )) {
                        f.setAccessible( true );
                        f.set( instance, new PropertyImpl<Object,T>( instance, f ) );
                    }
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
    
        private T                       value;
        
        private C                       instance;

        private Field                   f;

        private List<PropertyValidator> validators = new ArrayList( 1 );
        
        private List<PropertyConcern>   concerns = new ArrayList( 1 );
        
        
        protected PropertyImpl( C instance, Field f ) {
            this.instance = instance;
            this.f = f;
            initDefaultValue();

            // init validators
            for (Check a : annotations( Check.class, Checks.class )) {
                try {
                    PropertyValidator validator = a.value().newInstance();
                    validator.args = a.args();
                    validators.add( validator );
                }
                catch (Exception e) {
                    throw new ConfigurationException( "Cannot instantiate validator: " + a.value(), e );
                }
            }
            
            // init concerns
            for (Concern a : annotations( Concern.class, Concerns.class )) {
                try {
                    concerns.add( a.value().newInstance() );
                }
                catch (Exception e) {
                    throw new ConfigurationException( "Cannot instantiate concern: " + a.value(), e );
                }
            }
            if (f.getAnnotation( Immutable.class ) != null) {
                concerns.add( new ImmutableConcern() );
            }
            if (f.getAnnotation( Mandatory.class ) != null) {
                concerns.add( new MandatoryConcern() );
            }
            
            //
            value = checkConcerns( "doInit", null );
        }

        
        @Override
        public C put( T newValue ) {
            set( newValue );
            return instance;
        }
    
    
        @Override
        public T set( T newValue ) {
            newValue = checkConcerns( "doSet", newValue );
            checkValidators( newValue );

            T previous = value;
            value = newValue;
            return previous;
        }
    
    
        @Override
        public T get() {
            return checkConcerns( "doGet", value );
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
        
        
        protected T checkConcerns( String methodName, T v ) {
            try {
                Method m = PropertyConcern.class.getMethod( methodName, new Class[] {Object.class, Property.class, Object.class} );
                for (PropertyConcern concern : concerns) {
                    v = (T)m.invoke( concern, new Object[] {instance, this, v} );
                }
                return v;
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }


        protected void checkValidators( T checkValue ) {
            for (PropertyValidator validator: validators) {
                if (validator.test( checkValue ) == false) {
                    throw new IllegalStateException( "Property check/validator failed: " 
                            + validator.getClass().getName() 
                            + ", value=" + checkValue
                            + ", args=" + Arrays.toString( validator.args ) );
                }
            }
        }


        protected <A1 extends Annotation,A2 extends Annotation> List<A1> annotations( Class<A1> type, Class<A2> multitype ) {
            List<A1> result = new ArrayList();
            A1 a1 = f.getAnnotation( type );
            if (a1 != null) {
                result.add( a1 );
            }
            A2 a2 = f.getAnnotation( multitype );
            if (a2 != null) {
                try {
                    Method m = a2.getClass().getMethod( "value", new Class[0] );
                    A1[] array = (A1[])m.invoke( a2, new Object[0] );
                    result.addAll( Arrays.asList( array ) );
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
            return result;
        }


        @Override
        public PropertyInfo info() {
            return new PropertyInfo() {

                @Override
                public String getName() {
                    return f.getName();
                }

                @Override
                public Class<?> getType() {
                    return f.getType();
                }

                @Override
                public <A extends Annotation> A getAnnotation( Class<A> type ) {
                    return f.getAnnotation( type );
                }
            };
        }
        
    }
    
}
