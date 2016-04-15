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
 * Describes the style of a street. There is no direct SLD counterpart for this.
 *
 * @author Falko Bräutigam
 */
public class StreetStyle
        extends Style {

    public Property<StylePropertyValue>     width;

    public Property<StylePropertyValue>     outerLineWidth;
    
    public Property<StylePropertyValue>     outerLineColor;
    
    public Property<StylePropertyValue>     fillColor;

}
