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
package org.polymap.core.style.serialize.sld;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.serialize.sld.StylePropertyValueHandler.Setter;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class StyleSerializer<S extends Style> {

    private static Log log = LogFactory.getLog( StyleSerializer.class );
    
    protected List<SymbolizerDescriptor>    descriptors = new ArrayList();
    
    
    public abstract void serialize( S style );
    
    
    /**
     * Here goes the magic of multiplying style descriptors :)
     *
     * @param spv
     * @param setter
     */
    protected void setValue( StylePropertyValue spv, Setter setter ) {
        List<SymbolizerDescriptor> updated = new ArrayList( descriptors.size() * 2 );
        for (SymbolizerDescriptor sd : descriptors) {
            updated.addAll( StylePropertyValueHandler.handle( spv, sd, setter ) );
        }
    }    
    
}
