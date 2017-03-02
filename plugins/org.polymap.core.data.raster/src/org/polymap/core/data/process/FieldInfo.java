/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.data.process;

import java.util.Optional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.jgrasstools.gears.libs.modules.JGTModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.polymap.core.runtime.Lazy;

import oms3.annotations.In;
import oms3.annotations.Out;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public class FieldInfo
        extends BaseInfo {

    private static final Log log = LogFactory.getLog( FieldInfo.class );
    
    private Field           field;

    /** The value of the {@link In} annotation. */
    public Lazy<Boolean>    isInput = lazy( () -> annotation( In.class ).isPresent() );

    /** The value of the {@link Out} annotation. */
    public Lazy<Boolean>    isOutput = lazy( () -> annotation( Out.class ).isPresent() );

    /** The type of the {@link Field}. */
    public Lazy<Class<?>>   type = lazy( () -> field.getType() );

    
    protected FieldInfo( Field field ) {
        this.field = field;
        field.setAccessible( true );
    }

    
    public <R> R getValue( JGTModel module ) {
        try {
        return (R)field.get( module );
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException( e );
        }
    }

    
    public FieldInfo setValue( JGTModel module, Object value ) {
        try {
            field.set( module, value );
            return this;
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public boolean isAssignableFrom( Class<?> targetType ) {
        return field.getType().isAssignableFrom( targetType );
    }
    
    
    @Override
    public <A extends Annotation> Optional<A> annotation( Class<A> atype ) {
        return Optional.ofNullable( field.getAnnotation( atype ) );
    }

    
    @Override
    public String toString() {
        return Joiner.on( "" ).join( "FieldInfo[", 
                "name=", name.get().orElse( field.getName() ), ", " ,
                "description=", description.get().orElse( "<empty>" ),
                "]" );
    }
}
