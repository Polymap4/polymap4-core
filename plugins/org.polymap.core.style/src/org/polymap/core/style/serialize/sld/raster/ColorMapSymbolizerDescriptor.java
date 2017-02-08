/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld.raster;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.style.model.raster.ConstantRasterColorMap;
import org.polymap.core.style.model.raster.RasterColorMapStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.SLDSerializer;
import org.polymap.core.style.serialize.sld.StylePropertyValueHandler;
import org.polymap.core.style.serialize.sld.StyleSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

import org.polymap.model2.CollectionProperty;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ColorMapSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Mandatory
    @Immutable
    public Config<Expression>       colorMap;
    
    @Immutable
    public Config<Expression>       opacity;
    
    @Immutable
    public Config<Expression>       type;
    
    
    @Override
    public ColorMapSymbolizerDescriptor clone() {
        return (ColorMapSymbolizerDescriptor)super.clone();
    }

    
    /**
     * Serialize {@link RasterColorMapStyle} into {@link ColorMapSymbolizerDescriptor}.
     */
    public static class Serializer
            extends StyleSerializer<RasterColorMapStyle,ColorMapSymbolizerDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected ColorMapSymbolizerDescriptor createStyleDescriptor() {
            return new ColorMapSymbolizerDescriptor();
        }

        @Override
        public void doSerializeStyle( RasterColorMapStyle style ) {
            setValue( style.opacity.get(), (descriptor,value) -> descriptor.opacity.set( value ) );
            setValue( style.colorMap.get(), (descriptor,value) -> descriptor.colorMap.set( value ) );
            setValue( style.type.get(), (descriptor,value) -> descriptor.type.set( value ) );
        }
    }

    
    /**
     * FIXME This just passes the entire model {@link CollectionProperty} from
     * {@link RasterColorMapStyle} to the {@link SLDSerializer}. The problem is that
     * {@link StylePropertyValueHandler} API just allows {@link Expression}. Fix that!
     */
    public static class DummyExpression
            implements Expression {

        public CollectionProperty<ConstantRasterColorMap.Entry> entries;
        
        public DummyExpression( CollectionProperty<ConstantRasterColorMap.Entry> entries ) {
            this.entries = entries;
        }

        @Override
        public Object evaluate( Object object ) {
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public <T> T evaluate( Object object, Class<T> context ) {
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public Object accept( ExpressionVisitor visitor, Object extraData ) {
            throw new RuntimeException( "not yet implemented." );
        }
        
    }

}
