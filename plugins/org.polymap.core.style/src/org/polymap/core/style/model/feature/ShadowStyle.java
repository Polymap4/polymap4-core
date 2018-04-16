/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.style.model.feature;

import java.util.Optional;

import java.awt.Color;

import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.UIOrder;
import org.polymap.core.style.serialize.sld2.ShadowStyleSerializer;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * A general shadow style for features.
 * <p>
 * Offsets for X, Y and size are taken from general {@link Style#displacement}.
 *
 * @author Falko Bräutigam
 */
public class ShadowStyle
        extends Style {

    /** Initializes a newly created instance with default values. */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<ShadowStyle> defaults = new ValueInitializer<ShadowStyle>() {
        @Override
        public ShadowStyle initialize( ShadowStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "Shadow" );
            proto.opacity.createValue( ConstantNumber.defaults( 0.3 ) );
            proto.color.createValue( ConstantColor.defaults( 0, 0, 0 ) );
            proto.displacement.createValue( Displacement.defaults( 3, 3, 6 ) );
            
            for (Style s : proto.mapStyle().members()) {
                if (ShadowStyleSerializer.SUPPORTED.contains( s.getClass() )) {
                    proto.styleId.createValue( ConstantStyleId.defaults( s ) );
                }
            }
            return proto;
        }
    };

    /**
     * 
     */
    public static class StyleId {

        private String      styleId;
        
        public StyleId( String styleId ) {
            this.styleId = styleId;
        }
        
        public Optional<String> styleId() {
            return Optional.ofNullable( styleId );
        }
        
//        public Style parentStyle() {
//            return
//        }
    }
    
    
    // instance *******************************************
    
    /** The {@link Style#id} of the Style this is the shadow for. */
    @Nullable
    @UIOrder(5)
    @Description("styleId")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<StyleId>> styleId;

    @Nullable
    @UIOrder(10)
    @Description("color")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>>  color;

    @Nullable
    @UIOrder(20)
    @Description("opacity")
    @NumberRange(defaultValue = 1, from = 0, to = 1, increment = 0.1)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> opacity;
    
    
}
