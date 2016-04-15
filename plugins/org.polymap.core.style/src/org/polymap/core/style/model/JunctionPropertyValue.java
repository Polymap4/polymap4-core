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

import org.polymap.model2.Property;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class JunctionPropertyValue
        extends StylePropertyValue {

    public enum Junction {
        AND, OR, NOT;
    }

    public Property<Junction>       junction;
    
    public Property<Style>          op1;

    public Property<Style>          op2;

}
