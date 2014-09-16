/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2;

import java.util.Date;

import javax.annotation.Nullable;

import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * Property for simple ({@link Number}, {@link Boolean}, {@link String}, {@link Date}
 * , {@link Enum}) or {@link Composite} values.
 * <p/>
 * The property value of a newly created Entity is null unless {@link Defaults} or
 * {@link DefaultValue} was specified for this property or a {@link ValueInitializer}
 * was used when creating the Entity. A {@link ModelRuntimeException} is thrown if
 * the value is <code>null</code> and no {@link Nullable} annotation is given.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Property<T>
        extends PropertyBase<T> {

    /**
     * Returnes the value of this property.
     */
    public T get();

    /**
     * Creates a new value for this property if the current value is null.
     * <p/>
     * For <em>primitive</em> types a default value is created. This value is equal
     * to the value created by the {@link Defaults} annotation. For {@link Composite}
     * types a new instance is created with properties set to their default values.
     * <p/>
     * The initializer allows to provide a default value for primitive types. For
     * {@link Composite} types it gets called to initialize the properties of the
     * instance, including non-{@link Nullable} properties.
     * 
     * @param initializer Change/set primitive values and set properties of a
     *        {@link Composite} value. Can be null.
     * @return The newly created (and initialized) value if the current value is
     *         <code>null</code>, or the current value.
     */
    public T createValue( ValueInitializer<T> initializer );

    /**
     * Modifies the value of this property.
     *
     * @param value
     */
    public void set( T value );

}
