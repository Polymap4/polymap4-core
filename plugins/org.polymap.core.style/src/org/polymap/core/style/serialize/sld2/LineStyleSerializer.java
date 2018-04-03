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
import java.util.stream.Collectors;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Style;
import org.opengis.filter.expression.Literal;

import org.polymap.core.style.model.feature.LineStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class LineStyleSerializer
        extends StyleSerializer<LineStyle,List<LineSymbolizer>> {

    public LineStyleSerializer( Context context ) {
        super( context );
    }

    
    @Override
    public void serialize( LineStyle style, Style result ) {
        FeatureTypeStyle fts = defaultFeatureTypeStyle( result, style );
        
        // stroke line: 1st LineSymbolizer
        LineSymbolizer strokeLine = sf.createLineSymbolizer();
        strokeLine.setStroke( sf.getDefaultStroke() );
        fts.rules().get( 0 ).symbolizers().add( strokeLine );
//        accessor.set( rule -> (LineSymbolizer)rule.symbolizers().get( 0 ) );
//        style.stroke.opt().ifPresent( stroke -> {
//            new StrokeCompositeSerializer( context )
//                    .accessor.put( rule -> accessor.get().apply( rule ).getStroke() )
//                    .serialize( stroke, fts );
//        });

        // inner line: 2nd LineSymbolizer
        LineSymbolizer innerLine = sf.createLineSymbolizer();
        innerLine.setStroke( sf.getDefaultStroke() );
        fts.rules().get( 0 ).symbolizers().add( innerLine );
//        accessor.set( rule -> (LineSymbolizer)rule.symbolizers().get( 1 ) );
//        style.fill.opt().ifPresent( fillStroke -> {
//            new StrokeCompositeSerializer( context )
//                    .accessor.put( rule -> accessor.get().apply( rule ).getStroke() )
//                    .serialize( fillStroke, fts );
//        });

        serialize( style, fts );
    }


    @Override
    public void serialize( LineStyle style, FeatureTypeStyle fts ) {
        accessor.set( rule -> rule.symbolizers().stream().map( s -> (LineSymbolizer)s ).collect( Collectors.toList() ) );
        
        // inner line: 2nd LineSymbolizer
        style.fill.opt().ifPresent( fillStroke -> {
            new StrokeCompositeSerializer( context )
                    .accessor.put( rule -> accessor.get().apply( rule ).get( 1 ).getStroke() )
                    .serialize( fillStroke, fts );
        });
        
        // stroke line: 1st LineSymbolizer
        style.stroke.opt().ifPresent( outerStroke -> {
            set( fts, outerStroke.width, (value,syms) -> {
                Literal innerWidth = (Literal)syms.get( 1 ).getStroke().getWidth();
                syms.get( 0 ).getStroke().setWidth( ff.add( ff.multiply( value, ff.literal( 2 ) ), innerWidth ) );
            });
            set( fts, outerStroke.color, (value,syms) -> syms.get( 0 ).getStroke().setColor( value ) );
            set( fts, outerStroke.opacity, (value,syms) -> syms.get( 0 ).getStroke().setOpacity( value ) );
            // XXX linestyle
        });
    }
    
}
