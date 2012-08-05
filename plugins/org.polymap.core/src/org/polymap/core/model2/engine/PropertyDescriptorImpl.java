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
package org.polymap.core.model2.engine;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import javax.annotation.Nullable;

import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.store.PropertyDescriptor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyDescriptorImpl
        implements PropertyDescriptor {

    private static final String ParameterizedType = null;

    private EntityRuntimeContext    context;
    
    private Field                   field;
    
    private PropertyDescriptor      parent;
    
    
    public PropertyDescriptorImpl( EntityRuntimeContext context, Field field, PropertyDescriptor parent ) {
        this.context = context;
        this.field = field;
        this.parent = parent;
    }

    public EntityRuntimeContext getContext() {
        return context;
    }

    public Field getField() {
        return field;
    }

    public PropertyDescriptor getParent() {
        return parent;
    }

    public Class getPropertyType() {
        ParameterizedType declaredType = (ParameterizedType)field.getGenericType();
        return (Class)declaredType.getActualTypeArguments()[0];

    }
    
    public String getNameInStore() {
        return field.getAnnotation( NameInStore.class ) != null
                ? field.getAnnotation( NameInStore.class ).value()
                : field.getName();
    }

    public boolean isNullable() {
        return field.getAnnotation( Nullable.class ) != null;
    }

    public int getMaxOccurs() {
        throw new RuntimeException( "not yet implemented." );
//        return field.getAnnotation( MaxOccurs.class ) != null
//        ? field.getAnnotation( NameInStore.class ).value()
//        : field.getName();
    }

}
