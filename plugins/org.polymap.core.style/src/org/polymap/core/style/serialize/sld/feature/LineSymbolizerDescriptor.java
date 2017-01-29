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

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.style.model.feature.LineStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 * @author Steffen Stundzig
 */
public class LineSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    public Config<StrokeDescriptor>     fill;

    @Immutable
    public Config<StrokeDescriptor>     stroke;
//
//    @Immutable
//    public Config<Expression> offset;


    @Override
    public LineSymbolizerDescriptor clone() {
        return (LineSymbolizerDescriptor)super.clone();
    }
    
    
    /**
     * Serializes {@link LineStyle}.
     */
    public static class Serializer
            extends StyleSerializer<LineStyle,LineSymbolizerDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected LineSymbolizerDescriptor createStyleDescriptor() {
            return new LineSymbolizerDescriptor();
        }

        @Override
        public void doSerializeStyle( final LineStyle style ) {
            setComposite( new StrokeDescriptor.Serializer( context() ).serialize( style.fill.get() ),
                    ( LineSymbolizerDescriptor sd, StrokeDescriptor value ) -> sd.fill.set( value ) );
            
            setComposite( new StrokeDescriptor.Serializer( context() ).serialize( style.stroke.get() ),
                    ( LineSymbolizerDescriptor sd, StrokeDescriptor value ) -> sd.stroke.set( value ) );
            
            // setValue( style.offset.get(), ( LineSymbolizerDescriptor sd, Expression
            // value ) -> sd.offset.set( value ) );
        }
    }

}
