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
package org.polymap.core.style.serialize.sld.feature;

import org.opengis.filter.expression.Expression;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.style.model.feature.PointStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class PointSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    public Config<StrokeDescriptor>     stroke;

    @Immutable
    public Config<FillDescriptor>       fill;

    @Immutable
    // @DefaultDouble(8)
    public Config<Expression>           diameter;
    
    @Override
    public PointSymbolizerDescriptor clone() {
        return (PointSymbolizerDescriptor)super.clone();
    }
    
    
    /**
     * Serialize {@link PointStyle}.
     */
    public static class Serializer
            extends StyleSerializer<PointStyle,PointSymbolizerDescriptor> {

        public Serializer( Context context ) {
            super(context);
        }

        @Override
        protected PointSymbolizerDescriptor createStyleDescriptor() {
            return new PointSymbolizerDescriptor();
        }

        @Override
        public void doSerializeStyle( PointStyle style ) {
            setComposite( new StrokeDescriptor.Serializer( context() ).serialize( style.stroke.get() ),
                    (PointSymbolizerDescriptor sd, StrokeDescriptor value) -> sd.stroke.set( value ) );
            
            setComposite( new FillDescriptor.Serializer( context() ).serialize( style.fill.get() ),
                    (PointSymbolizerDescriptor sd, FillDescriptor value) -> sd.fill.set( value ) );
            
            setValue( style.diameter.get(),
                    (PointSymbolizerDescriptor sd, Expression value) -> sd.diameter.set( value ) );
        }
    }

}
