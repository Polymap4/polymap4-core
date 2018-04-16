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
package org.polymap.core.style.serialize.sld2;

import java.util.List;
import java.util.UUID;

import java.awt.Color;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Symbolizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.feature.ConstantColor;
import org.polymap.core.style.model.feature.ConstantNumber;
import org.polymap.core.style.model.feature.ConstantStyleId;
import org.polymap.core.style.model.feature.Displacement;
import org.polymap.core.style.model.feature.PointStyle;
import org.polymap.core.style.model.feature.ShadowStyle;
import org.polymap.core.style.model.feature.TextStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

import org.polymap.model2.runtime.UnitOfWork;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ShadowStyleSerializer
        extends StyleSerializer<ShadowStyle,Symbolizer> {

    private static final Log log = LogFactory.getLog( ShadowStyleSerializer.class );

    public static final List<Class<? extends Style>> SUPPORTED = Lists.newArrayList( PointStyle.class, TextStyle.class );
            
    
    public ShadowStyleSerializer( Context context ) {
        super( context );
    }

    @Override
    public void serialize( ShadowStyle style, org.geotools.styling.Style result ) {
        if (style.styleId.get() == null || !(style.styleId.get() instanceof ConstantStyleId)) {
            return;
        }
        ConstantStyleId styleId = (ConstantStyleId)style.styleId.get();
        if (styleId.styleId.get() == null) {
            return;
        }
        Style shadowedStyle = style.mapStyle().members().stream()
                .filter( s -> styleId.styleId.get().equals( s.id.get() ) ).findAny().get();
        
        if (shadowedStyle instanceof PointStyle) {
            serializePointShadow( style, (PointStyle)shadowedStyle, result );
        }
        else if (shadowedStyle instanceof TextStyle) {
            serializeTextShadow( style, (TextStyle)shadowedStyle, result );
        }
        else {
            throw new RuntimeException( "Unhandled shadowed style: " + shadowedStyle.getClass() );
        }
    }

    
    protected void serializePointShadow( ShadowStyle shadow, PointStyle point, org.geotools.styling.Style result ) {
        // old styles do not have the id field 
        if (!point.id.opt().isPresent()) {
            point.id.set( UUID.randomUUID().toString() );
        }
        
        try (UnitOfWork nested = point.belongsTo().newUnitOfWork()) {
            // the orig Style is modified to be the shadow style within a nested UnitOfWork,
            // so we can simple throw modified style away when done 
            FeatureStyle nestedStyle = nested.entity( point.mapStyle() );
            PointStyle shadowPoint = (PointStyle)nestedStyle.members().stream()
                    .filter( s -> point.id.get().equals( s.id.get() ) ).findAny().get(); 

            // XXX support complex values
            Color shadowColor = ((ConstantColor)shadow.color.get()).value();
            Double shadowOpacity = (Double)((ConstantNumber)shadow.opacity.get()).value();
            Integer shadowOffsetX = (Integer)((ConstantNumber)shadow.displacement.get().offsetX.get()).value();
            Integer shadowOffsetY = (Integer)((ConstantNumber)shadow.displacement.get().offsetY.get()).value();
            Integer shadowOffsetSize = (Integer)((ConstantNumber)shadow.displacement.get().offsetSize.get()).value();
            
            // XXX support displacement already set in Style
            shadowPoint.fill.get().color.createValue( ConstantColor.defaults( shadowColor ) );
            shadowPoint.stroke.get().color.createValue( ConstantColor.defaults( shadowColor ) );
            shadowPoint.stroke.get().width.createValue( ConstantNumber.defaults( 0d ) );
            shadowPoint.stroke.get().opacity.createValue( ConstantNumber.defaults( 0d ) );

            // offsetRadius is something like blurRadius
            float offsetRadius = shadowOffsetSize / 2;
            int levels = Math.min( 3, Math.round( offsetRadius ) );
            for (int i=0; i<levels; i++) {
                shadowPoint.title.set( shadow.title.get() + " (Level " + i + ")" );
                shadowPoint.fill.get().opacity.createValue( ConstantNumber.defaults( shadowOpacity / levels ) );
                shadowPoint.displacement.createValue( Displacement.defaults( shadowOffsetX, shadowOffsetY, 
                        Math.round( -(offsetRadius / 3) + (offsetRadius / levels * 2 * i) ) ) );
                new PointStyleSerializer( context ).serialize( shadowPoint, result );
            }
        }
    }
    
    
    protected void serializeTextShadow( ShadowStyle shadow, TextStyle text, org.geotools.styling.Style result ) {
        // old styles do not have the id field 
        if (!text.id.opt().isPresent()) {
            text.id.set( UUID.randomUUID().toString() );
        }
        
        try (UnitOfWork nested = text.belongsTo().newUnitOfWork()) {
            // the orig Style is modified to be the shadow style within a nested UnitOfWork,
            // so we can simply throw modified style away when done 
            FeatureStyle nestedStyle = nested.entity( text.mapStyle() );
            TextStyle shadowText = (TextStyle)nestedStyle.members().stream()
                    .filter( s -> text.id.get().equals( s.id.get() ) ).findAny().get(); 

            // XXX support complex values
            Color shadowColor = ((ConstantColor)shadow.color.get()).value();
            Double shadowOpacity = (Double)((ConstantNumber)shadow.opacity.get()).value();
            Integer shadowOffsetX = (Integer)((ConstantNumber)shadow.displacement.get().offsetX.get()).value();
            Integer shadowOffsetY = (Integer)((ConstantNumber)shadow.displacement.get().offsetY.get()).value();
            Integer shadowOffsetSize = (Integer)((ConstantNumber)shadow.displacement.get().offsetSize.get()).value();

            // blur via different levels of size does not work; just use
            // halo with fixed size
            shadowText.color.createValue( ConstantColor.defaults( shadowColor ) );
            shadowText.halo.get().color.createValue( ConstantColor.defaults( shadowColor ) );
            shadowText.halo.get().width.createValue( ConstantNumber.defaults( 1d ) );

            int levels = 1; //Math.min( 3, shadowOffsetSize );
            for (int i=0; i<levels; i++) {
                shadowText.title.set( shadow.title.get() + " (Level " + i + ")" );
                shadowText.opacity.createValue( ConstantNumber.defaults( shadowOpacity / levels ) );
                shadowText.halo.get().opacity.createValue( ConstantNumber.defaults( shadowOpacity / levels ) );
                // XXX support displacement already set in Style
                shadowText.displacement.createValue( Displacement.defaults( shadowOffsetX, shadowOffsetY, 
                        Math.round( (float)shadowOffsetSize / levels * i ) ) );
                new TextStyleSerializer( context ).serialize( shadowText, result );
            }
        }
    }
    
    
    @Override
    public void serialize( ShadowStyle style, FeatureTypeStyle fts ) {
        throw new RuntimeException( "nothing to do here." );
    }
    
}
