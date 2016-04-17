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

import java.awt.Color;

import org.polymap.core.style.model.ConstantColor;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.ConstantNumbersFromFilter;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyValue;

/**
 * Handles one {@link StylePropertyValue} descriptor and provides it with actual values.
 *
 * @author Falko Bräutigam
 */
public abstract class StylePropertyValueHandler<SPV extends StylePropertyValue,V> {

    /**
     * See {@link #doHandle(StylePropertyValue, SymbolizerDescriptor, Setter)}.
     */
    public static <SD extends SymbolizerDescriptor,V extends Object> 
    List<SD> handle( StylePropertyValue spv, SD sd, Setter<SD,V> setter ) {
        if (spv instanceof ConstantNumber) {
            return new ConstantNumberHandler().doHandle( (ConstantNumber)spv, sd, (Setter<SD,Number>)setter );
        }
        else if (spv instanceof ConstantNumbersFromFilter) {
            return new ConstantNumbersFromFilterHandler().doHandle( (ConstantNumbersFromFilter)spv, sd, (Setter<SD,Number>)setter );
        }
        else if (spv instanceof ConstantColor) {
            return new ConstantColorHandler().doHandle( (ConstantColor)spv, sd, (Setter<SD,Color>)setter );
        }
        else {
            throw new RuntimeException( "Unhandled StylePropertyValue: " + spv.getClass().getName() );
        }
    }

    
    // SPI ************************************************
    
    @FunctionalInterface
    public interface Setter<SD extends SymbolizerDescriptor,V extends Object> {
        void set( SD sd, V value );
    }
    
    
    /**
     * Converts the given {@link Style} property value into actual values in the
     * given {@link SymbolizerDescriptor} using the given setter. The given symbolizer
     * might be cloned in order to implement complex styling.
     *
     * @return List of resulting symbolizers.
     */
    public abstract <SD extends SymbolizerDescriptor> List<SD> doHandle( SPV spv, SD sd, Setter<SD,V> setter );
    

    /**
     * ConstantNumber
     */
    static class ConstantNumberHandler
            extends StylePropertyValueHandler<ConstantNumber,Number> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantNumber constantNumber, SD sd, Setter<SD,Number> setter ) {
            setter.set( sd, constantNumber.value.get() );
            return Collections.singletonList( sd );
        }
    }

    
    /**
     * ConstantColor
     */
    static class ConstantColorHandler
            extends StylePropertyValueHandler<ConstantColor,Color> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantColor constantColor, SD sd, Setter<SD,Color> setter ) {
            setter.set( sd, constantColor.color() );
            return Collections.singletonList( sd );
        }
    }
    
}
