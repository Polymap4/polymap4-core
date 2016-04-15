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

import org.polymap.model2.Concerns;
import org.polymap.model2.Property;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ConstantColor
        extends StylePropertyValue {

    @Concerns( NumberConcern.class )
    @NumberConcern.Range( from=0, to=255 )
    public Property<Integer>            r;
    
    @Concerns( NumberConcern.class )
    @NumberConcern.Range( from=0, to=255 )
    public Property<Integer>            g;
    
    @Concerns( NumberConcern.class )
    @NumberConcern.Range( from=0, to=255 )
    public Property<Integer>            b;
    
}
