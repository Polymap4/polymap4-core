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

import org.opengis.filter.expression.Expression;

import org.polymap.core.style.model.ConstantColor;
import org.polymap.core.style.model.ConstantFontFamily;
import org.polymap.core.style.model.ConstantFontStyle;
import org.polymap.core.style.model.ConstantFontWeight;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.ConstantString;
import org.polymap.core.style.model.ConstantStrokeCapStyle;
import org.polymap.core.style.model.ConstantStrokeDashStyle;
import org.polymap.core.style.model.ConstantStrokeJoinStyle;
import org.polymap.core.style.model.FeaturePropertyBasedNumber;
import org.polymap.core.style.model.FeaturePropertyBasedValue;
import org.polymap.core.style.model.FilterMappedNumbers;
import org.polymap.core.style.model.FontFamily;
import org.polymap.core.style.model.FontStyle;
import org.polymap.core.style.model.FontWeight;
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
 * @author Steffen Stundzig
 */
public abstract class StylePropertyValueHandler<SPV extends StylePropertyValue, V> {

    /**
     * See {@link #doHandle(StylePropertyValue, SymbolizerDescriptor, Setter)}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <SD extends SymbolizerDescriptor, V> List<SD> handle( StylePropertyValue spv, SD sd,
            Setter<SD> setter ) {
        if (spv == null) {
            return Collections.singletonList( sd );
        }
        else if (spv instanceof ConstantNumber) {
            return new ConstantNumberHandler().doHandle( (ConstantNumber)spv, sd, (Setter<SD>)setter );
        }
        else if (spv instanceof FilterMappedNumbers) {
            return new FilterMappedNumbersHandler().doHandle( (FilterMappedNumbers)spv, sd, (Setter<SD>)setter );
        }
        else if (spv instanceof ConstantColor) {
            return new ConstantColorHandler().doHandle( (ConstantColor)spv, sd, (Setter<SD>)setter );
        }
        // handled in the FontSerializer
//        else if (spv instanceof ConstantFontFamily) {
//            return new ConstantFontFamilyHandler().doHandle( (ConstantFontFamily)spv, sd, (Setter<SD>)setter );
//        }
        else if (spv instanceof ConstantFontStyle) {
            return new ConstantFontStyleHandler().doHandle( (ConstantFontStyle)spv, sd, (Setter<SD>)setter );
        }
        else if (spv instanceof ConstantFontWeight) {
            return new ConstantFontWeightHandler().doHandle( (ConstantFontWeight)spv, sd, (Setter<SD>)setter );
        }
        else if (spv instanceof ConstantString) {
            return new ConstantStringHandler().doHandle( (ConstantString)spv, sd, (Setter<SD>)setter );
        }
        else if (spv instanceof ConstantStrokeCapStyle) {
            return new ConstantStrokeCapStyleHandler().doHandle( (ConstantStrokeCapStyle)spv, sd, (Setter<SD>)setter );
        }
        else if (spv instanceof ConstantStrokeDashStyle) {
            return new ConstantStrokeDashStyleHandler().doHandle( (ConstantStrokeDashStyle)spv, sd,
                    (Setter<SD>)setter );
        }
        else if (spv instanceof ConstantStrokeJoinStyle) {
            return new ConstantStrokeJoinStyleHandler().doHandle( (ConstantStrokeJoinStyle)spv, sd,
                    (Setter<SD>)setter );
        }
        else {
            throw new RuntimeException( "Unhandled StylePropertyValue: " + spv.getClass().getName() );
        }
    }


    // SPI ************************************************

    @FunctionalInterface
    public interface Setter<SD extends SymbolizerDescriptor> {

        void set( SD sd, Expression value );
    }


    /**
     * Converts the given {@link Style} property value into actual values in the
     * given {@link SymbolizerDescriptor} using the given setter. The given
     * symbolizer might be cloned in order to implement complex styling.
     *
     * @return List of resulting symbolizers.
     */
    public abstract <SD extends SymbolizerDescriptor> List<SD> doHandle( SPV spv, SD sd, Setter<SD> setter );


    /**
     * ConstantNumber
     */
    static class ConstantNumberHandler
            extends StylePropertyValueHandler<ConstantNumber,Number> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantNumber constantNumber, SD sd,
                Setter<SD> setter ) {
            try {
                setter.set( sd, SLDSerializer.ff.literal( (Number)constantNumber.value.get() ) );
                return Collections.singletonList( sd );
            }
            catch (Exception e) {
                throw new RuntimeException( e.getMessage() + " : " + constantNumber.info().getName(), e );
            }
        }
    }


    static class FeaturePropertyBasedValueHandler
            extends StylePropertyValueHandler<FeaturePropertyBasedValue,Object> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( FeaturePropertyBasedValue spv, SD sd,
                Setter<SD> setter ) {
            try {
                setter.set( sd, SLDSerializer.ff.property( (String)spv.value.get() ) );
                return Collections.singletonList( sd );
            }
            catch (Exception e) {
                throw new RuntimeException( e.getMessage() + " : " + spv.info().getName(), e );
            }
        }
    }


    static class FeaturePropertyBasedNumberHandler
            extends StylePropertyValueHandler<FeaturePropertyBasedNumber,Number> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( FeaturePropertyBasedNumber spv, SD sd,
                Setter<SD> setter ) {
            try {
                setter.set( sd, SLDSerializer.ff.property( (String)spv.value.get() ) );
                return Collections.singletonList( sd );
            }
            catch (Exception e) {
                throw new RuntimeException( e.getMessage() + " : " + spv.info().getName(), e );
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
                Setter<SD> setter ) {
            if (constantColor.color() != null) {
                setter.set( sd, SLDSerializer.ff.literal( constantColor.color() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeCapStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeCapStyle,StrokeCapStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantStrokeCapStyle src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, SLDSerializer.ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStringHandler
            extends StylePropertyValueHandler<ConstantString,String> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantString src, SD sd, Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, SLDSerializer.ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantFontFamilyHandler
            extends StylePropertyValueHandler<ConstantFontFamily,FontFamily> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantFontFamily src, SD sd, Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, SLDSerializer.ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantFontStyleHandler
            extends StylePropertyValueHandler<ConstantFontStyle,FontStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantFontStyle src, SD sd, Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, SLDSerializer.ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantFontWeightHandler
            extends StylePropertyValueHandler<ConstantFontWeight,FontWeight> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantFontWeight src, SD sd, Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, SLDSerializer.ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeDashStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeDashStyle,StrokeDashStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantStrokeDashStyle src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, SLDSerializer.ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeJoinStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeJoinStyle,StrokeJoinStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( ConstantStrokeJoinStyle src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, SLDSerializer.ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }
}
