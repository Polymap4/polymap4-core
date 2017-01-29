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

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.style.model.raster.RasterRGBStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class RGBSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Mandatory
    @Immutable
    public Config<Expression>       redBand;
    
    @Mandatory
    @Immutable
    public Config<Expression>       greenBand;
    
    @Mandatory
    @Immutable
    public Config<Expression>       blueBand;
    
    
    @Override
    public RGBSymbolizerDescriptor clone() {
        return (RGBSymbolizerDescriptor)super.clone();
    }

    
    /**
     * Serialize {@link RasterRGBStyle} into {@link RGBSymbolizerDescriptor}.
     */
    public static class Serializer
            extends StyleSerializer<RasterRGBStyle,RGBSymbolizerDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected RGBSymbolizerDescriptor createStyleDescriptor() {
            return new RGBSymbolizerDescriptor();
        }

        @Override
        public void doSerializeStyle( RasterRGBStyle style ) {
            setValue( style.redBand.get(), (descriptor,value) -> descriptor.redBand.set( value ) );
            setValue( style.greenBand.get(), (descriptor,value) -> descriptor.greenBand.set( value ) );
            setValue( style.blueBand.get(), (descriptor,value) -> descriptor.blueBand.set( value ) );
        }
    }


}
