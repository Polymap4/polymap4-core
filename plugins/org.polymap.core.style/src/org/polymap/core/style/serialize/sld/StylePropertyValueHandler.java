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

import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.awt.Color;

import org.opengis.filter.expression.Expression;

import com.google.common.collect.Lists;

import org.polymap.core.style.model.*;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

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
    public static <SD extends SymbolizerDescriptor, V> List<SD> handle( Context context, StylePropertyValue spv, SD sd,
            Setter<SD> setter ) {
        try {
            if (spv == null) {
                return Collections.singletonList( sd );
            }
            else if (spv instanceof ConstantNumber) {
                return new ConstantNumberHandler().doHandle( context, (ConstantNumber)spv, sd, (Setter<SD>)setter );
            }
            else if (spv instanceof FilterMappedNumbers) {
                return new FilterMappedNumbersHandler().doHandle( context, (FilterMappedNumbers)spv, sd,
                        (Setter<SD>)setter );
            }
            else if (spv instanceof ConstantColor) {
                return new ConstantColorHandler().doHandle( context, (ConstantColor)spv, sd, (Setter<SD>)setter );
            }
            // handled in the FontSerializer
            // else if (spv instanceof ConstantFontFamily) {
            // return new ConstantFontFamilyHandler().doHandle(context,
            // (ConstantFontFamily)spv, sd, (Setter<SD>)setter );
            // }
            else if (spv instanceof ConstantFontStyle) {
                return new ConstantFontStyleHandler().doHandle( context, (ConstantFontStyle)spv, sd,
                        (Setter<SD>)setter );
            }
            else if (spv instanceof ConstantFontWeight) {
                return new ConstantFontWeightHandler().doHandle( context, (ConstantFontWeight)spv, sd,
                        (Setter<SD>)setter );
            }
            else if (spv instanceof ConstantString) {
                return new ConstantStringHandler().doHandle( context, (ConstantString)spv, sd, (Setter<SD>)setter );
            }
            else if (spv instanceof ConstantStrokeCapStyle) {
                return new ConstantStrokeCapStyleHandler().doHandle( context, (ConstantStrokeCapStyle)spv, sd,
                        (Setter<SD>)setter );
            }
            else if (spv instanceof ConstantStrokeDashStyle) {
                return new ConstantStrokeDashStyleHandler().doHandle( context, (ConstantStrokeDashStyle)spv, sd,
                        (Setter<SD>)setter );
            }
            else if (spv instanceof ConstantStrokeJoinStyle) {
                return new ConstantStrokeJoinStyleHandler().doHandle( context, (ConstantStrokeJoinStyle)spv, sd,
                        (Setter<SD>)setter );
            }
            else if (spv instanceof NoValue) {
                return new NoValueHandler().doHandle( context, (NoValue)spv, sd, setter );
            }
            else if (spv instanceof PropertyString) {
                return new PropertyStringHandler().doHandle( context, (PropertyString)spv, sd, setter );
            }
            else if (spv instanceof PropertyNumber) {
                return new PropertyNumberHandler().doHandle( context, (PropertyNumber)spv, sd, setter );
            }
            else if (spv instanceof ExpressionMappedValues) {
                return new ExpressionMappedValuesHandler().doHandle( context, (ExpressionMappedValues)spv, sd, setter );
            }
            else if (spv instanceof ScaleMappedNumbers) {
                return new ScaleMappedNumbersHandler().doHandle( context, (ScaleMappedNumbers)spv, sd, setter );
            }
            // else if (spv instanceof FeaturePropertyBasedNumber) {
            // return new FeaturePropertyBasedNumberHandler().doHandle(
            // (FeaturePropertyBasedNumber)spv, sd,
            // setter );
            // }
            else {
                throw new RuntimeException( "Unhandled StylePropertyValue: " + spv.getClass().getName() );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e.getMessage() + " : " + spv.info().getName(), e );
        }
    }


    // SPI ************************************************

    @FunctionalInterface
    public interface Setter<SD extends SymbolizerDescriptor> {

        void set( SD sd, Expression value );
    }
    //
    // @FunctionalInterface
    // public interface FilterSetter<SD extends SymbolizerDescriptor> {
    //
    // void set( SD sd, Filter value );
    // }


    /**
     * Converts the given {@link Style} property value into actual values in the
     * given {@link SymbolizerDescriptor} using the given setter. The given
     * symbolizer might be cloned in order to implement complex styling.
     *
     * @return List of resulting symbolizers.
     */
    public abstract <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, SPV spv, SD sd,
            Setter<SD> setter );


    static class ConstantNumberHandler
            extends StylePropertyValueHandler<ConstantNumber,Number> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantNumber constantNumber,
                SD sd, Setter<SD> setter ) {
            setter.set( sd, ff.literal( (Number)constantNumber.constantNumber.get() ) );
            return Collections.singletonList( sd );
        }
    }


    static class ConstantColorHandler
            extends StylePropertyValueHandler<ConstantColor,Color> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantColor constantColor, SD sd,
                Setter<SD> setter ) {
            if (constantColor.color() != null) {
                setter.set( sd, ff.literal( constantColor.color() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeCapStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeCapStyle,StrokeCapStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantStrokeCapStyle src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStringHandler
            extends StylePropertyValueHandler<ConstantString,String> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantString src, SD sd,
                Setter<SD> setter ) {
            if (src.constantString.get() != null) {
                setter.set( sd, ff.literal( src.constantString.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantFontFamilyHandler
            extends StylePropertyValueHandler<ConstantFontFamily,FontFamily> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantFontFamily src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantFontStyleHandler
            extends StylePropertyValueHandler<ConstantFontStyle,FontStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantFontStyle src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantFontWeightHandler
            extends StylePropertyValueHandler<ConstantFontWeight,FontWeight> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantFontWeight src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeDashStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeDashStyle,StrokeDashStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantStrokeDashStyle src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class ConstantStrokeJoinStyleHandler
            extends StylePropertyValueHandler<ConstantStrokeJoinStyle,StrokeJoinStyle> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantStrokeJoinStyle src, SD sd,
                Setter<SD> setter ) {
            if (src.value.get() != null) {
                setter.set( sd, ff.literal( src.value.get() ) );
            }
            return Collections.singletonList( sd );
        }
    }


    static class NoValueHandler
            extends StylePropertyValueHandler<NoValue,Object> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, NoValue empty, SD sd,
                Setter<SD> setter ) {
            // simply do nothing
            return Collections.singletonList( sd );
        }
    }


    static class PropertyStringHandler
            extends StylePropertyValueHandler<PropertyString,Object> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, PropertyString spv, SD sd,
                Setter<SD> setter ) {
            setter.set( sd, ff.property( (String)spv.propertyValue.get() ) );
            return Collections.singletonList( sd );
        }
    }


    static class ScaleMappedNumbersHandler
            extends StylePropertyValueHandler<ScaleMappedNumbers,Object> {

        @Override
        public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ScaleMappedNumbers spv, SD sd,
                Setter<SD> setter ) {
            Number defaultValue = (Number)spv.defaultNumberValue.get();
            Expression ife = ff.literal( defaultValue );
            Iterator<Number> scales = spv.scales.iterator();
            Iterator<Number> values = spv.numberValues.iterator();
            List<Expression> allExpressions = Lists.newArrayList(
                    ff.function( "env", ff.literal( "wms_scale_denominator" ) ), ff.literal( defaultValue ) );

            while (scales.hasNext()) {
                assert values.hasNext();
                allExpressions.add( ff.literal( scales.next() ) );
                allExpressions.add( ff.literal( values.next() ) );
            }
            ife = ff.function( "categorize", allExpressions.toArray( new Expression[allExpressions.size()] ) );
            setter.set( sd, ife );
            return Collections.singletonList( sd );
        }
    }
}
