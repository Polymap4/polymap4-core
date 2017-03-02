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

import org.jgrasstools.gears.libs.modules.JGTModel;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.FluentIterable;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.Lazy;

import oms3.ComponentAccess;
import oms3.annotations.Execute;
import oms3.annotations.Finalize;
import oms3.annotations.Initialize;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ModuleInfo
        extends BaseInfo {

    public static ModuleInfo of( Class<?> moduleClass ) {
        return new ModuleInfo( moduleClass );
    }

    public static ModuleInfo of( JGTModel module ) {
        return new ModuleInfo( module.getClass() );
    }

    
    // instance *******************************************
    
    private Class<?>                    type;

    /** Shortcut to {@link Class#getSimpleName()}.  */
    public Lazy<String>                 simpleClassname = lazy( () -> type.getSimpleName() );
    
    /** All fields of this module. */
    public Lazy<FluentIterable<FieldInfo>> fields = lazy( () ->
            FluentIterable.of( type.getFields() ).transform( f -> new FieldInfo( f ) ) );
    
    
    protected ModuleInfo( Class<?> type ) {
        this.type = type;
    }
    
    public Class<?> type() {
        return type;
    }
    
    @Override
    public <A extends Annotation> Optional<A> annotation( Class<A> atype ) {
        return Optional.ofNullable( type.getAnnotation( atype ) );
    }
    
    /**
     * Standard title made of {@link #label} and/or {@link #name}.
     */
    public String title() {
        return StringUtils.capitalize( name.get().orElse( simpleClassname.get() ) );
    }
    
    /**
     * All input {@link #fields} with {@link FieldInfo#isInput} is true.
     */
    public FluentIterable<FieldInfo> inputFields() {
        return fields.get().filter( field -> field.isInput.get() );
    }

    /**
     * All input {@link #fields} with {@link FieldInfo#isOutput} is true.
     */
    public FluentIterable<FieldInfo> outputFields() {
        return fields.get().filter( field -> field.isOutput.get() );
    }


    public JGTModel createInstance() {
        try {
            return (JGTModel)type.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    public void execute( JGTModel module, IProgressMonitor monitor ) {
        ComponentAccess.callAnnotated( module, Initialize.class, true );
        ComponentAccess.callAnnotated( module, Execute.class, false );
        ComponentAccess.callAnnotated( module, Finalize.class, true );
    }
    
}
