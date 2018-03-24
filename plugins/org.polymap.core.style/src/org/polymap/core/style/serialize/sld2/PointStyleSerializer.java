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

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Style;
import org.opengis.style.GraphicalSymbol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.feature.PointStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class PointStyleSerializer
        extends StyleSerializer<PointStyle,PointSymbolizer> {

    private static final Log log = LogFactory.getLog( PointStyleSerializer.class );

    public PointStyleSerializer( Context context ) {
        super( context );
    }

    
    @Override
    public void serialize( PointStyle style, Style result ) {
        // default symbolizer
        Graphic gr = sf.createGraphic( null, new Mark[] {}, null, null, null, null );
        PointSymbolizer point = sf.createPointSymbolizer( gr, null );

        // basics
        FeatureTypeStyle fts = defaultFeatureTypeStyle( point );
        accessor.set( rule -> (PointSymbolizer)rule.symbolizers().get( 0 ) );
        serialize( style, fts );

        // fill
        style.fill.opt().ifPresent( fill -> {
            new FillCompositeSerializer( context )
                    .accessor.put( rule -> findMark( accessor.get().apply( rule ) ).getFill() )
                    .serialize( fill, fts );
        });
        // stroke
        style.stroke.opt().ifPresent( stroke -> {
            new StrokeCompositeSerializer( context )
                    .accessor.put( rule -> findMark( accessor.get().apply( rule ) ).getStroke() )
                    .serialize( stroke, fts );
        });
        
        result.featureTypeStyles().add( fts );
    }

    
    protected Mark findMark( PointSymbolizer point ) {
        List<GraphicalSymbol> symbols = point.getGraphic().graphicalSymbols();
        // FIXME in case of ExternalGraphic return fake
        return symbols.get( 0 ) instanceof Mark
                ? (Mark)symbols.get( 0 )
                : sf.getCircleMark();
    }

    
    @Override
    public void serialize( PointStyle style, FeatureTypeStyle fts ) {
        // graphic
        set( fts, style.graphic, (value,sym) -> {
            List<GraphicalSymbol> symbols = sym.getGraphic().graphicalSymbols();
            assert symbols.isEmpty();
            // mark
            value.mark().ifPresent( mark -> {
                switch (mark) {
                    case Circle : symbols.add( sf.getCircleMark() ); break;
                    case Cross : symbols.add( sf.getCrossMark() ); break;
                    case X : symbols.add( sf.getXMark() ); break;
                    case Square : symbols.add( sf.getSquareMark() ); break;
                    case Triangle : symbols.add( sf.getTriangleMark() ); break;
                    case Star : symbols.add( sf.getStarMark() ); break;
                    default: throw new RuntimeException( "Unhandled WellKnownMark: " + mark );
                }
            });
            // url
            value.url().ifPresent( url -> {
                symbols.add( sf.createExternalGraphic( url, value.format().get() ) );
            });
        });
        // default: Circle
        setDefault( fts, style.graphic, (value,sym) -> {
            sym.getGraphic().graphicalSymbols().add( sf.getCircleMark() );
        });
        set( fts, style.diameter, (value,sym) -> sym.getGraphic().setSize( ff.literal( value ) ) );
        set( fts, style.rotation, (value,sym) -> sym.getGraphic().setRotation( ff.literal( value ) ) );
    }
    
}
