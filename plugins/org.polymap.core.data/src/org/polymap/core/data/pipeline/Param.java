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

import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;

/**
 * Instances are used to define an init parameter of a {@link PipelineProcessor}
 * instance. Also provides methods to access values provided by an
 * {@link ParamsHolder}.
 * <p/>
 * The {@link #name} of the param is created by joining the name of the class that
 * called the constructor (usually the class the param is defined by) and the suffix
 * given to {@link #Param(String, Class)}
 * 
 * @author Falko Bräutigam
 */
public class Param<T> {

    /**
     * Common interface for classes that can be accesses via {@link Param}. 
     */
    public interface ParamsHolder {
        public Params params();
    }

    
    // instance *******************************************

    public String       name;

    public T            defaultValue;

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

    public void set( ParamsHolder params, T value ) throws IllegalStateException{
        if (params.params().put( name, value ) != null) {
            throw new IllegalStateException( "Params already exists: " + name );
        }
    }
    
    public T put( ParamsHolder params, T value ) throws IllegalStateException{
        return (T)params.params().put( name, value );
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
        return (T)params.params().getOrDefault( name, defaultValue );
    }

}