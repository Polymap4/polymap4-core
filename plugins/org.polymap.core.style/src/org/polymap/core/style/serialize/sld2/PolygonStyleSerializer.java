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

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;

import org.polymap.core.style.model.feature.PolygonStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class PolygonStyleSerializer
        extends StyleSerializer<PolygonStyle,PolygonSymbolizer> {

    public PolygonStyleSerializer( Context context ) {
        super( context );
    }

    
    @Override
    public void serialize( PolygonStyle style, Style result ) {
        // default symbolizer
        PolygonSymbolizer polygon = sf.createPolygonSymbolizer();
        polygon.setFill( sf.getDefaultFill() );
        polygon.setStroke( sf.getDefaultStroke() );

        FeatureTypeStyle fts = defaultFeatureTypeStyle( result, style, polygon );
        fts.setName( style.title.opt().orElse( "PolygonStyle" ) );
        fts.getDescription().setTitle( style.title.opt().orElse( "PolygonStyle" ) );
        accessor.set( rule -> (PolygonSymbolizer)rule.symbolizers().get( 0 ) );

        // fill
        style.fill.opt().ifPresent( fill -> {
            new FillCompositeSerializer( context )
                    .accessor.put( rule -> accessor.get().apply( rule ).getFill() )
                    .serialize( fill, fts );
        });
        // stroke
        style.stroke.opt().ifPresent( stroke -> {
            new StrokeCompositeSerializer( context )
                    .accessor.put( rule -> accessor.get().apply( rule ).getStroke() )
                    .serialize( stroke, fts );
        });
    }

        
    @Override
    public void serialize( PolygonStyle style, FeatureTypeStyle fts ) {
        throw new RuntimeException( "No properties to serialize." );
    }
    
}
