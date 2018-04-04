/* 
 * polymap.org
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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
package org.polymap.core.style.model.feature;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.polymap.core.style.model.StylePropertyValue;

/**
 * Denotes the value range used in the UI to edit the value of a
 * {@link StylePropertyValue}.
 * 
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface NumberRange {

    /** Value range start. */
    public double from();
    
    /** Value range end. */
    public double to();
    
    public double defaultValue();
    
    /** The number of decimal digits used in the UI. */
    public int digits() default 1;
    
    public double increment();
    
}
