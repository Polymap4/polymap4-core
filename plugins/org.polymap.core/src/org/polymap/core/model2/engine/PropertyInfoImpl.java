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

import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Immutable;
import org.polymap.core.model2.MaxOccurs;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class PropertyInfoImpl<T>
        implements PropertyInfo<T> {

    private Field                   field;

    
    public PropertyInfoImpl( Field field ) {
        assert Property.class.isAssignableFrom( field.getType() );
        this.field = field;
    }

    @Override
    public Class getType() {
        ParameterizedType declaredType = (ParameterizedType)field.getGenericType();
        return (Class)declaredType.getActualTypeArguments()[0];
    }
    
    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public String getNameInStore() {
        return field.getAnnotation( NameInStore.class ) != null
                ? field.getAnnotation( NameInStore.class ).value()
                : field.getName();
    }

    @Override
    public boolean isNullable() {
        return field.getAnnotation( Nullable.class ) != null;
    }

    @Override
    public boolean isImmutable() {
        return field.getAnnotation( Immutable.class ) != null;
    }

    @Override
    public int getMaxOccurs() {
        if (CollectionProperty.class.isAssignableFrom( field.getType() )) {
            return field.getAnnotation( MaxOccurs.class ) != null
                    ? field.getAnnotation( MaxOccurs.class ).value()
                    : Integer.MAX_VALUE;
        }
        else {
            return 1;
        }
    }

    @Override
    public T getDefaultValue() {
        return (T)DefaultValues.valueOf( field );
    }

    @Override
    public Entity getEntity() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
