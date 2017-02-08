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
import org.polymap.core.style.model.feature.LineStyle;
import org.polymap.core.style.model.raster.RasterGrayStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class GrayscaleSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Mandatory
    @Immutable
    public Config<Expression>       grayBand;
    
    @Immutable
    public Config<Expression>       opacity;
    
    @Override
    public GrayscaleSymbolizerDescriptor clone() {
        return (GrayscaleSymbolizerDescriptor)super.clone();
    }

    
    /**
     * Serialize {@link LineStyle}.
     */
    public static class Serializer
            extends StyleSerializer<RasterGrayStyle,GrayscaleSymbolizerDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected GrayscaleSymbolizerDescriptor createStyleDescriptor() {
            return new GrayscaleSymbolizerDescriptor();
        }

        @Override
        public void doSerializeStyle( RasterGrayStyle style ) {
            setValue( style.opacity.get(), (descriptor,value) -> descriptor.opacity.set( value ) );
            setValue( style.grayBand.get(), (descriptor,value) -> descriptor.grayBand.set( value ) );
        }
    }


}
