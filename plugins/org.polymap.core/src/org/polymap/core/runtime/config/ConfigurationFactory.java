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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConfigurationFactory {

    private static final Lazy<Method>       doGet = new PlainLazyInit( () -> doMethod( "doGet" ) ); 
    private static final Lazy<Method>       doSet = new PlainLazyInit( () -> doMethod( "doSet" ) ); 
    private static final Lazy<Method>       doInit = new PlainLazyInit( () -> doMethod( "doInit" ) ); 
    
    
    private static Method doMethod( String name ) {
        try {
            return PropertyConcern.class.getDeclaredMethod( name, new Class[] {Object.class, Config.class, Object.class} );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
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
     * Injects {@link Config} instances into the given object.
     */
    public static <T> T inject( T instance ) throws ConfigurationException {
        try {
            // init properties
            for (Class cl = instance.getClass(); cl != null; cl = cl.getSuperclass()) {
                for (Field f : cl.getDeclaredFields()) {
                    if (Config.class.isAssignableFrom( f.getType() )) {
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
    private static class PropertyImpl<H,V>
            implements Config2<H,V> {
    
        private V                       value;
        
        private H                       instance;

        private Field                   f;

        private List<PropertyValidator> validators = new ArrayList( 1 );
        
        private List<PropertyConcern>   concerns = new ArrayList( 1 );
        
        
        protected PropertyImpl( H instance, Field f ) {
            this.instance = instance;
            this.f = f;
            
            //
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
            value = checkConcerns( doInit.get(), value );
        }

        
        @Override
        public String toString() {
            return "Config[" + value + "]";
        }


        @Override
        public <AH extends H> AH put( V newValue ) {
            set( newValue );
            return (AH)instance;
        }
    
    
        @Override
        public V set( V newValue ) {
            newValue = checkConcerns( doSet.get(), newValue );
            checkValidators( newValue );

            V previous = value;
            value = newValue;
            return previous;
        }
    
    
        @Override
        public V get() {
            return checkConcerns( doGet.get(), value );
        }
        
        
        @Override
        public boolean isPresent() {
            return value != null;
        }


        @Override
        public void ifPresent( Consumer<V> consumer ) {
            if (isPresent()) {
                consumer.accept( value );
            }
        }


        @Override
        public V orElse( V other ) {
            return isPresent() ? get() : other;
        }


        @Override
        public V orElse( Supplier<V> supplier ) {
            return isPresent() ? get() : supplier.get();
        }


        @Override
        public <U> Optional<U> map( Function<? super V,? extends U> mapper ) {
            assert mapper != null;
            return isPresent() 
                    ? Optional.ofNullable( mapper.apply( get() ) )
                    : Optional.empty();
        }


        protected void initDefaultValue() {
            assert value == null;
            // String
            DefaultString defaultString = f.getAnnotation( DefaultString.class );
            if (defaultString != null) {
                set( (V)defaultString.value() );
            }
            // Double
            DefaultDouble defaultDouble = f.getAnnotation( DefaultDouble.class );
            if (defaultDouble != null) {
                set( (V)new Double( defaultDouble.value() ) );
            }
            // Float
            DefaultFloat defaultFloat = f.getAnnotation( DefaultFloat.class );
            if (defaultFloat != null) {
                set( (V)new Float( defaultFloat.value() ) );
            }
            // Int
            DefaultInt defaultInt = f.getAnnotation( DefaultInt.class );
            if (defaultInt != null) {
                set( (V)new Integer( defaultInt.value() ) );
            }
            // Boolean
            DefaultBoolean defaultBoolean = f.getAnnotation( DefaultBoolean.class );
            if (defaultBoolean != null) {
                set( (V)new Boolean( defaultBoolean.value() ) );
            }
            // Defaults
            Defaults defaults = f.getAnnotation( Defaults.class );
            if (defaults != null) {
                Class<?> propType = info().getType();
                if (String.class.equals( propType )) {
                    set( (V)"" );
                }
                else if (Integer.class.equals( propType )) {
                    set( (V)new Integer( 0 ) );
                }
                else if (Float.class.equals( propType )) {
                    set( (V)new Float( 0 ) );
                }
                else if (Double.class.equals( propType )) {
                    set( (V)new Double( 0 ) );
                }
                else if (Boolean.class.equals( propType )) {
                    set( (V)Boolean.FALSE );
                }
                else if (List.class.equals( propType )) {
                    set( (V)new ArrayList() );
                }
                else if (Map.class.equals( propType )) {
                    set( (V)new HashMap() );
                }
                else if (Set.class.equals( propType )) {
                    set( (V)new HashSet() );
                }
                else if (Date.class.equals( propType )) {
                    set( (V)new Date() );
                }
                else {
                    throw new RuntimeException( "Unhandled @Defaults type: " + propType );
                }
            }
            
            
        }
        
        
        protected V checkConcerns( Method m, V newValue ) {
            try {
                for (PropertyConcern concern : concerns) {
                    newValue = (V)m.invoke( concern, new Object[] {instance, this, newValue} );
                }
                return newValue;
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }


        protected void checkValidators( V checkValue ) {
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
            return new PropertyInfo/*<H,V>*/() {

                @Override
                public String getName() {
                    return f.getName();
                }

                @Override
                public Class<V> getType() {
                    ParameterizedType declaredType = (ParameterizedType)f.getGenericType();
                    Type[] typeArgs = declaredType.getActualTypeArguments();
                    // Property2 has value type as second type param
                    Type typeArg = typeArgs[typeArgs.length-1];
                    return typeArg instanceof ParameterizedType
                            ? (Class<V>)((ParameterizedType)typeArg).getRawType()
                            : (Class<V>)typeArg;
                }

                @Override
                public <T extends Annotation> T getAnnotation( Class<T> type ) {
                    return f.getAnnotation( type );
                }
                
                @Override 
                public <T extends Object> T getHostObject() {
                    return (T)instance;
                }
                
                @Override
                public <R extends Object> R getRawValue() {
                    return (R)value;
                }

                @Override
                public <R extends Object> R setRawValue( R newValue ) {
                    R previous = (R)value;
                    value = (V)newValue;
                    return previous;
                }
            };
        }
        
    }
    
}
