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
import org.polymap.core.style.model.feature.PolygonStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 * @author Steffen Stundzig
 */
public class PolygonSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    public Config<StrokeDescriptor> stroke;

    @Immutable
    public Config<FillDescriptor>   fill;
    
    @Override
    public PolygonSymbolizerDescriptor clone() {
        return (PolygonSymbolizerDescriptor)super.clone();
    }
    
    
    /**
     * Serializes {@link PolygonStyle}.
     */
    public static class Serializer
            extends StyleSerializer<PolygonStyle,PolygonSymbolizerDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected PolygonSymbolizerDescriptor createStyleDescriptor() {
            return new PolygonSymbolizerDescriptor();
        }

        @Override
        public void doSerializeStyle( PolygonStyle style ) {
            setComposite( new StrokeDescriptor.Serializer( context() ).serialize( style.stroke.get() ),
                    (PolygonSymbolizerDescriptor sd, StrokeDescriptor value) -> sd.stroke.set( value ) );
            
            setComposite( new FillDescriptor.Serializer( context() ).serialize( style.fill.get() ),
                    (PolygonSymbolizerDescriptor sd, FillDescriptor value) -> sd.fill.set( value ) );
        }
    }

}
