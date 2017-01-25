/* 
 * polymap.org
 * Copyright (C) 2016-2017, the @authors. All rights reserved.
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

import org.polymap.model2.Composite;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * Describes the value of a style property. Values can be a simple constant value or
 * a complex composite of multiple values and settings.
 * <p/>
 * A StylePropertyValue describes the parameters of a <b>function</b> that maps to
 * target type <b>T</b>. The most simple such function is a constant value.
 *
 * @param <T> The actual target type of this value.
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public abstract class StylePropertyValue<T>
        extends Composite {

    @Nullable
    public Property<String>     lastEditorHint;
    
}
