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

import java.util.Collections;
import java.util.List;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyValue;

/**
 * Create actual values out of an {@link StylePropertyValue} descriptor.
 *
 * @author Falko Bräutigam
 */
public abstract class StylePropertyValueHandler<SPV extends StylePropertyValue,V> {

    /**
     * See {@link #doHandle(StylePropertyValue, SymbolizerDescriptor, Setter)}.
     */
    public static List<SymbolizerDescriptor> handle( StylePropertyValue spv, SymbolizerDescriptor sd, Setter setter ) {
        if (spv instanceof ConstantNumber) {
            return new ConstantNumberHandler().doHandle( (ConstantNumber)spv, sd, setter );
        }
        else {
            throw new RuntimeException( "Unhandled StylePropertyValue: " + spv.getClass().getName() );
        }
    }

    
    // SPI ************************************************
    
    @FunctionalInterface
    public interface Setter<SD extends SymbolizerDescriptor,V> {
        void set( SD sd, V value );
    }
    
    
    /**
     * Converts the given {@link Style} property value into actual values in the
     * given {@link SymbolizerDescriptor} using the given setter. The given symbolizer
     * might be cloned in order to implement complex styling.
     *
     * @return List of resulting symbolizers.
     */
    public abstract List<SymbolizerDescriptor> doHandle( SPV spv, SymbolizerDescriptor sd, Setter<SymbolizerDescriptor,V> setter );
    

    /**
     * ConstantNumber
     */
    static class ConstantNumberHandler
            extends StylePropertyValueHandler<ConstantNumber,Number> {

        @Override
        public List<SymbolizerDescriptor> doHandle( ConstantNumber constantNumber, 
                SymbolizerDescriptor sd, Setter<SymbolizerDescriptor,Number> setter ) {
            
            setter.set( sd, constantNumber.value.get() );
            return Collections.singletonList( sd );
        }
    }
    
}
