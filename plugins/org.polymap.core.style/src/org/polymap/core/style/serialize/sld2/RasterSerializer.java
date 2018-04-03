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

import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntryImpl;
import org.geotools.styling.ColorMapImpl;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.opengis.filter.expression.Literal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.raster.ConstantRasterColorMap;
import org.polymap.core.style.model.raster.RasterBand;
import org.polymap.core.style.model.raster.RasterColorMap;
import org.polymap.core.style.model.raster.RasterColorMapStyle;
import org.polymap.core.style.model.raster.RasterColorMapType;
import org.polymap.core.style.model.raster.RasterGrayStyle;
import org.polymap.core.style.model.raster.RasterRGBStyle;
import org.polymap.core.style.model.raster.RasterStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class RasterSerializer<T extends RasterStyle>
        extends StyleSerializer<T,RasterSymbolizer> {

    private static final Log log = LogFactory.getLog( RasterSerializer.class );

    public RasterSerializer( Context context ) {
        super( context );
    }

    @Override
    public void serialize( T style, FeatureTypeStyle fts ) {
        set( fts, style.opacity, (value,sym) -> sym.setOpacity( value ) );
    }

    protected static ContrastEnhancement noGammaCorrection() {
        // XXX SLD is properly generated without ContrastEnhancement, renderer does enhancement however :(
//        return sf.createContrastEnhancement( null );
//        return sf.createContrastEnhancement( ff.literal( 1.0 ) );
        return null;
    }

    
    /**
     * 
     */
    public static class GraySerializer
            extends RasterSerializer<RasterGrayStyle> {
        
        public GraySerializer( Context context ) {
            super( context );
        }

        @Override
        public void serialize( RasterGrayStyle style, Style result ) {
            // default symbolizer
            RasterSymbolizer raster = sf.createRasterSymbolizer();
            raster.setContrastEnhancement( noGammaCorrection() );

            FeatureTypeStyle fts = defaultFeatureTypeStyle( result, style, raster );
            fts.setName( style.title.opt().orElse( "RasterGrayStyle" ) );
            fts.getDescription().setTitle( style.title.opt().orElse( "RasterGrayStyle" ) );

            accessor.set( rule -> (RasterSymbolizer)rule.symbolizers().get( 0 ) );
            serialize( style, fts );
        }

        @Override
        public void serialize( RasterGrayStyle style, FeatureTypeStyle fts ) {
            super.serialize( style, fts );
            set( fts, style.grayBand, (value,sym) -> {
                RasterBand band = (RasterBand)((Literal)value).getValue();
                sym.setChannelSelection( sf.channelSelection( sf.createSelectedChannelType( band.band(), noGammaCorrection() ) ) );
            });
        }
    }
    

    /**
     * 
     */
    public static class RGBSerializer
            extends RasterSerializer<RasterRGBStyle> {
        
        public RGBSerializer( Context context ) {
            super( context );
        }

        @Override
        public void serialize( RasterRGBStyle style, Style result ) {
            // default symbolizer
            RasterSymbolizer raster = sf.createRasterSymbolizer();
            raster.setContrastEnhancement( noGammaCorrection() );

            FeatureTypeStyle fts = defaultFeatureTypeStyle( result, style, raster );
            fts.setName( style.title.opt().orElse( "RasterRGBStyle" ) );
            fts.getDescription().setTitle( style.title.opt().orElse( "RasterRGBStyle" ) );

            accessor.set( rule -> (RasterSymbolizer)rule.symbolizers().get( 0 ) );
            serialize( style, fts );
        }

        @Override
        public void serialize( RasterRGBStyle style, FeatureTypeStyle fts ) {
            super.serialize( style, fts );
            
            set( fts, style.redBand, (value,sym) -> {
                RasterBand band = (RasterBand)((Literal)value).getValue();
                SelectedChannelType[] channels = sym.getChannelSelection().getRGBChannels();
                channels[0] = sf.createSelectedChannelType( band.band(), noGammaCorrection() );
                sym.getChannelSelection().setRGBChannels( channels );
            });
            set( fts, style.greenBand, (value,sym) -> {
                RasterBand band = (RasterBand)((Literal)value).getValue();
                SelectedChannelType[] channels = sym.getChannelSelection().getRGBChannels();
                channels[1] = sf.createSelectedChannelType( band.band(), noGammaCorrection() );
                sym.getChannelSelection().setRGBChannels( channels );
            });
            set( fts, style.blueBand, (value,sym) -> {
                RasterBand band = (RasterBand)((Literal)value).getValue();
                SelectedChannelType[] channels = sym.getChannelSelection().getRGBChannels();
                channels[1] = sf.createSelectedChannelType( band.band(), noGammaCorrection() );
                sym.getChannelSelection().setRGBChannels( channels );
            });
        }
    }

    
    /**
     * 
     */
    public static class ColorMapSerializer
            extends RasterSerializer<RasterColorMapStyle> {
        
        public ColorMapSerializer( Context context ) {
            super( context );
        }

        @Override
        public void serialize( RasterColorMapStyle style, Style result ) {
            // default symbolizer
            RasterSymbolizer raster = sf.createRasterSymbolizer();
            raster.setContrastEnhancement( noGammaCorrection() );
            raster.setColorMap( new ColorMapImpl() );

            FeatureTypeStyle fts = defaultFeatureTypeStyle( result, style, raster );
            fts.setName( style.title.opt().orElse( "RasterColorMapStyle" ) );
            fts.getDescription().setTitle( style.title.opt().orElse( "RasterColorMapStyle" ) );

            accessor.set( rule -> (RasterSymbolizer)rule.symbolizers().get( 0 ) );
            serialize( style, fts );
        }

        @Override
        public void serialize( RasterColorMapStyle style, FeatureTypeStyle fts ) {
            super.serialize( style, fts );
            // type
            set( fts, style.type, (value,sym) -> {
                RasterColorMapType type = (RasterColorMapType)((Literal)value).getValue();
                switch (type) {
                    case RAMP: sym.getColorMap().setType( ColorMap.TYPE_RAMP ); break;
                    case VALUES: sym.getColorMap().setType( ColorMap.TYPE_VALUES ); break;
                    case INTERVALLS: sym.getColorMap().setType( ColorMap.TYPE_INTERVALS ); break;
                }                
            });
            // colormap
            set( fts, style.colorMap, (value,sym) -> {
                RasterColorMap colormap = (RasterColorMap)((Literal)value).getValue();
                for (ConstantRasterColorMap.Entry entry : colormap) {
                    ColorMapEntryImpl newEntry = new ColorMapEntryImpl();
                    Color color = new Color( entry.r.get(), entry.g.get(), entry.b.get() );
                    newEntry.setColor( ff.literal( color ) );
                    entry.opacity.opt().ifPresent( opacity -> newEntry.setOpacity( ff.literal( opacity ) ) );
                    newEntry.setQuantity( ff.literal( entry.value.get() ) );
                    sym.getColorMap().addColorMapEntry( newEntry );
                }
            });
        }
    }
    
}
