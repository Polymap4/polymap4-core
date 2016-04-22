/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.serialize.sld;

import java.util.Collections;
import java.util.List;

import java.awt.Color;

import org.polymap.core.style.model.ConstantColor;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.ConstantStrokeCapStyle;
import org.polymap.core.style.model.ConstantStrokeDashStyle;
import org.polymap.core.style.model.ConstantStrokeJoinStyle;
import org.polymap.core.style.model.FilterMappedNumbers;
import org.polymap.core.style.model.StrokeCapStyle;
import org.polymap.core.style.model.StrokeDashStyle;
import org.polymap.core.style.model.StrokeJoinStyle;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyValue;

/**
 * Handles one {@link StylePropertyValue} descriptor and provides it with actual
 * values.
 *
 * @author Falko Bräutigam
 */
public abstract class StylePropertyValueHandler<SPV extends StylePropertyValue, V> {

    /**
     * See {@link #doHandle(StylePropertyValue, SymbolizerDescriptor, Setter)}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <SD extends SymbolizerDescriptor,V> List<SD> handle( StylePropertyValue spv, SD sd, Setter<SD,V> setter ) {
        if (spv instanceof ConstantNumber) {
            return new ConstantNumberHandler().doHandle( 
                    (ConstantNumber)spv, sd, (Setter<SD,Number>)setter );
        }
        else if (spv instanceof FilterMappedNumbers) {
            return new FilterMappedNumbersHandler().doHandle( 
                    (FilterMappedNumbers)spv, sd, (Setter<SD,Number>)setter );
        }
        else if (spv instanceof ConstantColor) {
            return new ConstantColorHandler().doHandle( 
                    (ConstantColor)spv, sd, (Setter<SD,Color>)setter );
        }
        else if (spv instanceof ConstantStrokeCapStyle) {
            return new ConstantStrokeCapStyleHandler().doHandle( 
                    (ConstantStrokeCapStyle)spv, sd, (Setter<SD,StrokeCapStyle>)setter );
        }
        else if (spv instanceof ConstantStrokeDashStyle) {
            return new ConstantStrokeDashStyleHandler().doHandle( 
                    (ConstantStrokeDashStyle)spv, sd, (Setter<SD,StrokeDashStyle>)setter );
        }
        else if (spv instanceof ConstantStrokeJoinStyle) {
            return new ConstantStrokeJoinStyleHandler().doHandle( 
                    (ConstantStrokeJoinStyle)spv, sd, (Setter<SD,StrokeJoinStyle>)setter );
        }
        else {
            throw new RuntimeException( "Unhandled StylePropertyValue: " + spv.getClass().getName() );
        }
    }


    // SPI ************************************************

    @FunctionalInterface
    public interface Setter<SD extends SymbolizerDescriptor, V extends Object> {

        void set( SD sd, V value );
    }


    /**
     * Converts the given {@link Style} property value into actual values in the
     * given {@link SymbolizerDescriptor} using the given setter. The given
     * symbolizer might be cloned in order to implement complex styling.
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
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantNumber constantNumber, SD sd,
                Setter<SD,Number> setter ) {
            try {
                setter.set( sd, (Number)constantNumber.value.get() );
                return Collections.singletonList( sd );
            }
            catch (Exception e) {
                throw new RuntimeException( e.getMessage() + " : " + constantNumber.info().getName(), e );
            }
        }
    }


    /**
     * ConstantColor
     */
    static class ConstantColorHandler
            extends StylePropertyValueHandler<ConstantColor,Color> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantColor constantColor, SD sd,
                Setter<SD,Color> setter ) {
            setter.set( sd, constantColor.color() );
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeCapStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeCapStyle,StrokeCapStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantStrokeCapStyle src, SD sd,
                Setter<SD,StrokeCapStyle> setter ) {
            setter.set( sd, src.capStyle.get() );
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeDashStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeDashStyle,StrokeDashStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantStrokeDashStyle src, SD sd,
                Setter<SD,StrokeDashStyle> setter ) {
            setter.set( sd, src.dashStyle.get() );
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeJoinStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeJoinStyle,StrokeJoinStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantStrokeJoinStyle src, SD sd,
                Setter<SD,StrokeJoinStyle> setter ) {
            setter.set( sd, src.joinStyle.get() );
            return Collections.singletonList( sd );
        }
    }
}
