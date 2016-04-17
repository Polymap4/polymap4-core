/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.polymap.model2.Property;
import org.polymap.model2.PropertyConcernAdapter;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class NumberConcern<T extends Number>
        extends PropertyConcernAdapter<T> {
    
    @Override
    public void set( T value ) {
        Range a = (Range)info().getAnnotation( Range.class );
        assert a != null : "Missing NumberConcern.Range annotation on property: " + info().getName();
        if (value.intValue() < a.from() || value.intValue() > a.to()) {
            throw new IllegalArgumentException( "Property range: " + value + "outside: " + a.from() + "-" + a.to() + " for property: " + info().getName() );
        }
        super.set( value );
    }

    
    /**
     * Specifies the range an Integer {@link Property} can have.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.FIELD } )
    @Documented
    public static @interface Range {
        public int from();
        public int to();
    }

}
