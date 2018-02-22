/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.data.pipeline;

import java.util.Optional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;
import org.polymap.core.ui.UIUtils;

/**
 * Used to define init parameters of a {@link PipelineProcessor} instance. Also
 * provides methods to access values provided by a {@link ParamsHolder}.
 * <p/>
 * The {@link #name} of the param is created by joining the name of the class that
 * called the constructor (usually the class the param is defined by) and the suffix
 * given to {@link #Param(String, Class)}
 * <p/>
 * The values set by the methods of this class are <b>converted and stored as
 * Strings</b> for simple serialization. The following value types are currently
 * supported: {@link String}, {@link Number}, {@link Boolean}, {@link Duration}.
 * 
 * @author Falko Bräutigam
 */
public class Param<T> {

    /**
     * Denotes UI settings of a {@link Param}. Params without annotation are not
     * rendered in the UI.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.FIELD } )
    @Documented
    public @interface UI {

        /** Specifies the description to be shown in the UI. */
        public String description();

        /** Specifies a custom UI. */
        public Class<? extends UISupplier> custom() default DEFAULT_UI.class;

        public String min() default "";

        public String max() default "";

        public String[] values() default {};
    }

    /**
     *
     */
    public interface UISupplier<T> {
        /** The color to be used for field foreground in case of validation error. */
        public static Color errorColor() { return UIUtils.getColor( 240, 20, 20 ); }
        
        /**
         * 
         * @param parent The {@link Composite} to create the UI controls.
         * @param param The Param we are working for.
         * @param site The site of the processor, delivering {@link Params} to get a value from.
         * @return The control that was created or a container.
         */
        public Control createContents( Composite parent, Param<T> param, PipelineProcessorSite site );
    }
    
    /**
     * Specifies that the default UI element should be used.
     */
    public static class DEFAULT_UI 
            implements UISupplier<Object> {
        @Override
        public Control createContents( Composite parent, Param<Object> param, PipelineProcessorSite site ) {
            throw new RuntimeException( "Must never be called" );
        }
    };
    
    /**
     * Common interface for classes that can be accesses via {@link Param}. 
     */
    public interface ParamsHolder {
        public Params params();
    }

    // instance *******************************************

    private String      name;

    private T           defaultValue;

    private Class       type;

    /**
     * Construct with no default value set.
     * 
     * @param localpart The suffix part of the name of this parameter.
     * @param type The type of this parameter.
     */
    public Param( String localpart, Class<T> type ) {
        this( localpart, type, null );
    }
    
    /**
     * 
     * @param localpart The suffix part of the name of this parameter.
     * @param type The type of this parameter.
     * @param defaultValue The default value.
     */
    public Param( String localpart, Class<T> type, T defaultValue ) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement elm = stacktrace[2];
        //System.out.println( "" + elm.getClassName() );
        
        this.name = elm.getClassName() + "." + localpart;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public Optional<T> defaultValue() {
        return Optional.ofNullable( defaultValue );
    }
    
    public Class type() {
        return type;
    }

    public String name() {
        return name;
    }
    
    
    /**
     * Set a value for this param, throw {@link IllegalStateException} if the param
     * is set already.
     * <p/>
     * The given value is converted and stored as String.
     *
     * @param params
     * @param value
     * @throws IllegalStateException If param is already set.
     */
    public void set( ParamsHolder params, T value ) throws IllegalStateException {
        String s = asString( value );
        if (params.params().putIfAbsent( name, s ) != null) {
            throw new IllegalStateException( "Params already exists: " + name );
        }
    }

    /**
     * <p/>
     * The given value is converted and stored as String. 
     */
    public Optional<T> put( ParamsHolder params, T value ) throws IllegalStateException {
        String s = asString( value );
        String result = (String)params.params().put( name, s );
        return Optional.ofNullable( result != null ? (T)asValue( result, type ) : null );
    }
    
    /**
     * Does not convert to String. For internal use.
     */
    public Optional<T> rawput( ParamsHolder params, T value ) throws IllegalStateException {
        return Optional.ofNullable( (T)params.params().put( name, value ) );
    }

    public Optional<T> rawopt( ParamsHolder params ) {
        return Optional.ofNullable( (T)params.params().get( name ) );
    }
    
    public Optional<T> opt( ParamsHolder params ) {
        return Optional.ofNullable( doGet( params ) );
    }

    /**
     * 
     * @throws IllegalStateException If this param is not found.
     */
    public T get( ParamsHolder params ) throws IllegalStateException {
        T result = doGet( params );
        if (result == null) {
            throw new IllegalStateException( "Param not given: " + name );
        }
        return result;
    }
    
    protected T doGet( ParamsHolder params ) throws RuntimeException {
        String result = (String)params.params().get( name );
        return result != null ? (T)asValue( result, type ) : defaultValue;
    }

    
    public static String asString( Object value ) {
        if (value instanceof String) {
            return (String)value;
        }
        else if (value instanceof Boolean) {
            return ((Boolean)value).toString();
        }
        else if (value instanceof Number) {
            return ((Number)value).toString();
        }
        else if (value instanceof Duration) {
            return ((Duration)value).toString();
        }
        else {
            throw new UnsupportedOperationException( "Unsupported param type: " + value );
        }
    }
    
    
    public static <R> R asValue( String s, Class<R> type ) {
        if (String.class.equals( type )) {
            return (R)s;
        }
        else if (Duration.class.equals( type )) {
            return (R)Duration.parse( s );
        }
        else if (Boolean.class.equals( type )) {
            return (R)Boolean.valueOf( s );
        }
        else if (Integer.class.equals( type )) {
            return (R)Integer.valueOf( s );
        }
        else if (Long.class.equals( type )) {
            return (R)Long.valueOf( s );
        }
        else if (Float.class.equals( type )) {
            return (R)Float.valueOf( s );
        }
        else if (Double.class.equals( type )) {
            return (R)Double.valueOf( s );
        }
        else {
            throw new UnsupportedOperationException( "Unsupported param type: " + type );
        }
    }
    
}