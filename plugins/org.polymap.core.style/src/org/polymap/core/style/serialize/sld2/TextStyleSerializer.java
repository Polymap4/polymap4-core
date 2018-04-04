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

import java.awt.Color;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;
import org.geotools.styling.TextSymbolizer;
import org.opengis.filter.expression.Literal;

import org.polymap.core.style.model.feature.TextStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class TextStyleSerializer
        extends StyleSerializer<TextStyle,TextSymbolizer> {

    public TextStyleSerializer( Context context ) {
        super( context );
    }

    
    @Override
    public void serialize( TextStyle style, Style result ) {
        // default symbolizer
        TextSymbolizer text = sf.createTextSymbolizer();
        text.setFill( sf.getDefaultFill() );
        style.halo.opt().ifPresent( halo -> text.setHalo( sf.createHalo( sf.getDefaultFill(), ff.literal( 1 ) ) ) );
        style.font.opt().ifPresent( font -> text.setFont( sf.getDefaultFont() ) );

        FeatureTypeStyle fts = defaultFeatureTypeStyle( result, style, text );
        fts.setName( style.title.opt().orElse( "TextStyle" ) );
        fts.getDescription().setTitle( style.title.opt().orElse( "TextStyle" ) );
        accessor.set( rule -> (TextSymbolizer)rule.symbolizers().get( 0 ) );
        serialize( style, fts );

        // font
        style.font.opt().ifPresent( font -> {
            new FontCompositeSerializer( context )
                    .accessor.put( rule -> accessor.get().apply( rule ).getFont() )
                    .serialize( font, fts );
        });
        // halo
        style.halo.opt().ifPresent( halo -> {
            // XXX for what reason ever for halo color an awt.Color does not seem to work
            set( fts, halo.color, (value,sym) -> sym.getHalo().getFill().setColor( ff.literal( SLDSerializer2.toHexString( (Color)((Literal)value).getValue() ) ) ) );
            set( fts, halo.opacity, (value,sym) -> sym.getHalo().getFill().setOpacity( value ) );
            set( fts, halo.width, (value,sym) -> sym.getHalo().setRadius( value ) );
        });
    }

        
    @Override
    public void serialize( TextStyle style, FeatureTypeStyle fts ) {
        // property name
        set( fts, style.property, (value,sym) -> sym.setLabel( value ) );
        
        // fill
        set( fts, style.opacity, (value,sym) -> sym.getFill().setOpacity( value ) );
        set( fts, style.color, (value,sym) -> sym.getFill().setColor( value ) );
    }
    
}
