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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.Property;

/**
 * Simple point style. Roughly modelling: 
 * <ul>
 * <li>{@link org.opengis.style.PointSymbolizer}</li>
 * <li>{@link org.opengis.style.Mark}</li>
 * </ul>
 *
 * @author Falko Bräutigam
 */
public class PointStyle
        extends Style {

    private static Log log = LogFactory.getLog( PointStyle.class );
  
    public Property<StylePropertyValue>     strokeWidth;
    
    public Property<StylePropertyValue>     strokeColor;
    
    public Property<StylePropertyValue>     strokeOpacity;
    
    public Property<StylePropertyValue>     fillColor;
    
    public Property<StylePropertyValue>     fillOpacity;
    
}
