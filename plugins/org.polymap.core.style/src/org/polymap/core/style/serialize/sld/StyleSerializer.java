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
 * Serializes a particular {@link Style} into a flat list of
 * {@link SymbolizerDescriptor} instances. The resulting list represents the
 * cross-product of all complex filter and/or scale definitions specified via
 * {@link StylePropertyValue} types. Those different {@link StylePropertyValue} types
 * are handled by corresponding {@link StylePropertyValueHandler} types.
 *
 * @param <S> The input style type.
 * @param <SD> The output symbolizer descriptor.
 * @author Falko Br�utigam
 */
public abstract class StyleSerializer<S extends Style,SD extends SymbolizerDescriptor> {

    private static Log log = LogFactory.getLog( StyleSerializer.class );
    
    protected List<SD>      descriptors = new ArrayList();
    
    
    public List<SD> serialize( S style ) {
        assert descriptors == null;
        try {
            descriptors = new ArrayList();
            doSerialize( style );
            return descriptors;
        }
        finally {
            descriptors = null;
        }
    }
    
    protected abstract SD createDescriptor();
    
    protected abstract void doSerialize( S style );
    
    
    /**
     * Sets the value in all current {@link #descriptors} using the given setter.
     * <p/>
     * Here goes the magic of multiplying style descriptors :)
     *
     * @param spv
     * @param setter
     */
    protected <V extends Object> void setValue( StylePropertyValue spv, Setter<SD,V> setter ) {
        
        if (descriptors.isEmpty()) {
            descriptors.add( createDescriptor() );
        }
        
        List<SD> updated = new ArrayList( descriptors.size() );
        for (SymbolizerDescriptor sd : descriptors) {
            updated.addAll( StylePropertyValueHandler.handle( spv, (SD)sd, setter ) );
        }
        this.descriptors = updated;
    }    
    
}
