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

import static org.polymap.core.style.serialize.sld2.PointStyleSerializer.SvgParam.FILL;
import static org.polymap.core.style.serialize.sld2.PointStyleSerializer.SvgParam.FILL_COLOR;
import static org.polymap.core.style.serialize.sld2.PointStyleSerializer.SvgParam.FILL_OPACITY;
import static org.polymap.core.style.serialize.sld2.PointStyleSerializer.SvgParam.STROKE_COLOR;
import static org.polymap.core.style.serialize.sld2.PointStyleSerializer.SvgParam.STROKE_OPACITY;

import java.util.List;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Style;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.style.GraphicalSymbol;

import org.apache.commons.lang3.StringUtils;
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

    public enum SvgParam {
        FILL, FILL_COLOR, FILL_OPACITY, STROKE_COLOR, STROKE_OPACITY, STROKE_WIDTH;
        @Override
        public String toString() {
            return StringUtils.replace( name().toLowerCase(), "_", "-" );
        }
    }

    
    // instance *******************************************
    
    public PointStyleSerializer( Context context ) {
        super( context );
    }

    
    protected <R> R literalValue( Expression expr ) {
        if (expr instanceof Literal) {
            return (R)((Literal)expr).getValue();
        }
        else {
            throw new RuntimeException( "SLD ExternalGraphics do not support any other than fixed (literal) expression." );
        }
    }
    
    
    @Override
    public void serialize( PointStyle style, Style result ) {
        // default symbolizer
        Graphic gr = sf.createGraphic( null, new Mark[] {}, null, null, null, null );
        PointSymbolizer point = sf.createPointSymbolizer( gr, null );

        // basics / init symbolizer
        FeatureTypeStyle fts = defaultFeatureTypeStyle( result, style, point );
        fts.setName( style.title.opt().orElse( "PointStyle" ) );
        fts.getDescription().setTitle( style.title.opt().orElse( "PointStyle" ) );
        accessor.set( rule -> (PointSymbolizer)rule.symbolizers().get( 0 ) );
        serialize( style, fts );

        // fill
        style.fill.opt().ifPresent( fill -> {
            // color
            set( fts, style.fill.get().color, (value,sym) -> {
                GraphicalSymbol symbol = sym.getGraphic().graphicalSymbols().get( 0 );
                // Mark
                if (symbol instanceof Mark) {
                    ((Mark)symbol).getFill().setColor( value );
                }
                // ExternalGraphic
                else if (symbol instanceof ExternalGraphic) {
                    addParam( (ExternalGraphic)symbol, FILL, SLDSerializer2.toHexString( literalValue( value ) ) );
                    addParam( (ExternalGraphic)symbol, FILL_COLOR, SLDSerializer2.toHexString( literalValue( value ) ) );
                }
                else {
                    throw new RuntimeException( "Unhandled symbol type" + symbol );
                }
            });
            // opacity
            set( fts, style.fill.get().opacity, (value,sym) -> {
                GraphicalSymbol symbol = sym.getGraphic().graphicalSymbols().get( 0 );
                // Mark
                if (symbol instanceof Mark) {
                    ((Mark)symbol).getFill().setOpacity( value );
                }
                // ExternalGraphic
                else if (symbol instanceof ExternalGraphic) {
                    addParam( (ExternalGraphic)symbol, FILL_OPACITY, literalValue( value ).toString() );
                }
                else {
                    throw new RuntimeException( "Unhandled symbol type" + symbol );
                }
            });
        });
        // stroke
        style.stroke.opt().ifPresent( stroke -> {
            // color
            set( fts, style.stroke.get().color, (value,sym) -> {
                GraphicalSymbol symbol = sym.getGraphic().graphicalSymbols().get( 0 );
                if (symbol instanceof Mark) {
                    ((Mark)symbol).getStroke().setColor( value );
                }
                else if (symbol instanceof ExternalGraphic) {
                    addParam( (ExternalGraphic)symbol, STROKE_COLOR, SLDSerializer2.toHexString( literalValue( value ) ) );
                }
                else {
                    throw new RuntimeException( "Unhandled symbol type" + symbol );
                }
            });
            // opacity
            set( fts, style.stroke.get().opacity, (value,sym) -> {
                GraphicalSymbol symbol = sym.getGraphic().graphicalSymbols().get( 0 );
                if (symbol instanceof Mark) {
                    ((Mark)symbol).getStroke().setOpacity( value );
                }
                else if (symbol instanceof ExternalGraphic) {
                    addParam( (ExternalGraphic)symbol, STROKE_OPACITY, literalValue( value ).toString() );
                }
                else {
                    throw new RuntimeException( "Unhandled symbol type" + symbol );
                }
            });
            // width
            set( fts, style.stroke.get().width, (value,sym) -> {
                GraphicalSymbol symbol = sym.getGraphic().graphicalSymbols().get( 0 );
                if (symbol instanceof Mark) {
                    ((Mark)symbol).getStroke().setWidth( value );
                }
                else if (symbol instanceof ExternalGraphic) {
                    addParam( (ExternalGraphic)symbol, SvgParam.STROKE_WIDTH, literalValue( value ).toString() + "px" );
                }
                else {
                    throw new RuntimeException( "Unhandled symbol type" + symbol );
                }
            });
        });
    }

    
    protected ExternalGraphic addParam( ExternalGraphic graphic, SvgParam param, String value ) {
        try {
            StringBuilder uri = new StringBuilder( 256 ).append( graphic.getURI() );
            uri.append( uri.indexOf( "?" ) == -1 ? "?" : "&" );
            uri.append( URLEncoder.encode( param.toString(), "UTF-8" ) );
            uri.append( "=" );
            uri.append( URLEncoder.encode( value, "UTF-8" ) );
            graphic.setURI( uri.toString() );
            return graphic;
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException( "Should never happen.", e );
        }    
    }
    
    
    @Override
    public void serialize( PointStyle style, FeatureTypeStyle fts ) {
        // graphic
        set( fts, style.graphic, (value,sym) -> {
            List<GraphicalSymbol> symbols = sym.getGraphic().graphicalSymbols();
            assert symbols.isEmpty();
            
            org.polymap.core.style.model.feature.Graphic graphic = (org.polymap.core.style.model.feature.Graphic)((Literal)value).getValue();
            
            // mark
            graphic.mark().ifPresent( mark -> {
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
            graphic.url().ifPresent( url -> {
                symbols.add( sf.createExternalGraphic( url, graphic.format().get() ) );
            });
        });
        // default: Circle
        setDefault( fts, style.graphic, (value,sym) -> {
            sym.getGraphic().graphicalSymbols().add( sf.getCircleMark() );
        });
        set( fts, style.diameter, (value,sym) -> sym.getGraphic().setSize( value ) );
        set( fts, style.rotation, (value,sym) -> sym.getGraphic().setRotation( value ) );
        
        // displacement
        style.displacement.opt().ifPresent( displacement -> {
            set( fts, displacement.offsetX, (value,sym) -> {
                sym.getGraphic().setDisplacement( sf.displacement( value, ff.literal( 0 ) ) );
            });
            set( fts, displacement.offsetY, (value,sym) -> {
                sym.getGraphic().getDisplacement().setDisplacementY( value );
            });
            set( fts, displacement.offsetSize, (value,sym) -> {
                // XXX the renderer cannot handle ADD expression, to sad :(
                Number origSize = (Number)((Literal)sym.getGraphic().getSize()).getValue();
                Number offset = (Number)((Literal)value).getValue();
                sym.getGraphic().setSize( ff.literal( origSize.intValue() + offset.intValue() ) );
            });
        });
    }
    
}
