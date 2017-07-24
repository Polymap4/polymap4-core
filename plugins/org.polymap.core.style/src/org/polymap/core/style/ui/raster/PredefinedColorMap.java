/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.style.ui.raster;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;
import java.awt.image.RenderedImage;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.opengis.coverage.grid.GridEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.PlainLazyInit;
import org.polymap.core.runtime.SubMonitor;
import org.polymap.core.runtime.Timer;
import org.polymap.core.style.model.raster.ConstantRasterColorMap;
import org.polymap.core.style.model.raster.RasterColorMapStyle;
import org.polymap.core.style.model.raster.RasterColorMapType;

/**
 * Predefined color maps. 
 *
 * @author Falko Bräutigam
 */
public class PredefinedColorMap {

    private static final Log log = LogFactory.getLog( PredefinedColorMap.class );

    public static final PredefinedColorMap ELEVATION = new PredefinedColorMap( "Elevation" )
            .addNoValue( 0, 191, 191, 0 )
            .add( 0, 255, 0 )
            .add( 255, 255, 0 )
            .add( 255, 127, 0 )
            .add( 191, 127, 63 )
            .add( 20, 21, 20 );

    public static final PredefinedColorMap RAINBOW = new PredefinedColorMap( "Rainbow" )
            //.addNoValue( 255, 255, 255, 0 )
            .add( 255, 255, 0 )
            .add( 0, 255, 0 )
            .add( 0, 255, 255 )
            .add( 0, 0, 255 )
            .add( 255, 0, 255 )
            .add( 255, 0, 0 );

    /** Falko's special, first ever colormap :) */
    public static final PredefinedColorMap FALKOS = new PredefinedColorMap( "Falko's special" )
            .add( 0x000000 ).add( 0xff0000 ).add( 0x00ff00 ).add( 0x0000ff );

    public static final Lazy<List<PredefinedColorMap>> all = new LockedLazyInit( () -> {
        PredefinedColorMap reverse = new PredefinedColorMap( "Reverse" );
        reverse.entries.addAll( Lists.reverse( ELEVATION.entries ) );

        List<PredefinedColorMap> result = new ArrayList();
        result.add( ELEVATION );
        //result.add( reverse );
        result.add( FALKOS );
        result.add( new PredefinedColorMap( "Aspect" )
                .add( 255, 255, 255 )
                .add( 0, 0, 0 )
                .add( 255, 255, 255 ) );
        result.add( RAINBOW );
        result.add( new PredefinedColorMap( "Rainbow extended" )
                .add( 255, 255, 0 )
                .add( 128, 255, 0 )
                .add( 0, 255, 0 )
                .add( 0, 255, 128 )
                .add( 0, 255, 255 )
                .add( 0, 128, 255 )
                .add( 0, 0, 255 )
                .add( 128, 0, 255 )
                .add( 255, 0, 255 )
                .add( 255, 0, 128 )
                .add( 255, 0, 0 ) );
        result.add( new PredefinedColorMap( "Radiation" )
                .add( 198, 198, 224 )
                .add( 0, 0, 115 )
                .add( 0, 100, 210 )
                .add( 90, 183, 219 )
                .add( 0, 255, 255 )
                .add( 40, 254, 100 )
                .add( 80, 131, 35 )
                .add( 160, 190, 0 )
                .add( 255, 255, 100 )
                .add( 255, 180, 0 )
                .add( 255, 0, 0 ) );
        result.add( new PredefinedColorMap( "Grayscale" )
                .addNoValue( 0, 0, 0, 0 )
                .add( 0x000000 )
                .add( 0xffffff ) );
        result.add( new PredefinedColorMap( "Grayscale invers" )
                .addNoValue( 0, 0, 0, 0 )
                .add( 0xffffff )
                .add( 0x000000 ) );
        result.add( new PredefinedColorMap( "Red" )
                .addNoValue( 0, 0, 0, 0 )
                .add( 0x000000 )
                .add( 0xff0000 ) );
        result.add( new PredefinedColorMap( "Green" )
                .addNoValue( 0, 0, 0, 0 )
                .add( 0x000000 )
                .add( 0x00ff00 ) );
        result.add( new PredefinedColorMap( "Blue" )
                .addNoValue( 0, 0, 0, 0 )
                .add( 0x000000 )
                .add( 0x0000ff ) );

        result.add( new PredefinedColorMap( "Bathymetric" )
                .addValue( -30000, 0, 0, 0 )
                .addValue( -20000, 0, 0, 0 )
                .addValue( -20000, 0, 0, 0 )
                .addValue( -10000, 0, 0, 59 )
                .addValue( -10000, 0, 0, 59 )
                .addValue( -9000, 0, 0, 130 )
                .addValue( -9000, 0, 0, 130 )
                .addValue( -8000, 0, 0, 202 )
                .addValue( -8000, 0, 0, 202 )
                .addValue( -7000, 0, 18, 255 )
                .addValue( -7000, 0, 18, 255 )
                .addValue( -6000, 0, 90, 255 )
                .addValue( -6000, 0, 90, 255 )
                .addValue( -5000, 0, 157, 255 )
                .addValue( -5000, 0, 157, 255 )
                .addValue( -4000, 0, 227, 255 )
                .addValue( -4000, 0, 227, 255 )
                .addValue( -3000, 43, 255, 255 )
                .addValue( -3000, 43, 255, 255 )
                .addValue( -2000, 115, 255, 255 )
                .addValue( -2000, 115, 255, 255 )
                .addValue( -1000, 184, 255, 255 )
                .addValue( -1000, 184, 255, 255 )
                .addValue( 0, 250, 255, 255 )
                .addValue( 0, 0, 128, 0 )
                .addValue( 500, 133, 5, 0 )
                .addValue( 500, 133, 5, 0 )
                .addValue( 1000, 255, 128, 0 )
                .addValue( 1000, 255, 128, 0 )
                .addValue( 2000, 255, 255, 0 )
                .addValue( 2000, 255, 255, 0 )
                .addValue( 3000, 255, 255, 127 )
                .addValue( 3000, 255, 255, 127 )
                .addValue( 4000, 255, 255, 244 )
                .addValue( 4000, 255, 255, 255 )
                .addValue( 10000, 255, 255, 255 ) );        
        result.add( new PredefinedColorMap( "Flow" )
                .addValue( 1, 255, 255, 0 )
                .addValue( 2, 0, 255, 0 )
                .addValue( 3, 0, 255, 255 )
                .addValue( 4, 255, 0, 255 )
                .addValue( 5, 0, 0, 255 )
                .addValue( 6, 160, 32, 240 )
                .addValue( 7, 255, 165, 0 )
                .addValue( 8, 30, 144, 255 )
                .addValue( 10, 255, 0, 0 ) );
        result.add( new PredefinedColorMap( "TCA" )
                .addValue( 1, 255, 255, 255 )
                .addValue( 10, 0, 255, 0 )
                .addValue( 100, 0, 255, 255 )
                .addValue( 1000, 0, 0, 255 )
                .addValue( 10000, 255, 0, 255 )
                .addValue( 100000, 255, 0, 0 )
                .addValue( 1000000, 110, 0, 0 )
                .addValue( 10000000, 0, 0, 0 ) );
        result.add( new PredefinedColorMap( "Sea" )
                .addValue( -30000, 255, 255, 255 )
                .addValue( -8000, 255, 255, 255 )
                .addValue( -8000, 0, 0, 255 )
                .addValue( -2500, 30, 144, 255 )
                .addValue( -2500, 30, 144, 255 )
                .addValue( -2000, 162, 208, 252 )
                .addValue( -2000, 162, 208, 252 )
                .addValue( -1500, 250, 117, 117 )
                .addValue( -1500, 250, 117, 117 )
                .addValue( 0, 255, 0, 0 ) );
        result.add( new PredefinedColorMap( "Shalstab" )
                .addValue( 1, 255, 0, 0 )
                .addValue( 1, 255, 0, 0 )
                .addValue( 2, 0, 255, 0 )
                .addValue( 2, 0, 255, 0 )
                .addValue( 3, 255, 255, 0 )
                .addValue( 3, 255, 255, 0 )
                .addValue( 4, 0, 0, 255 )
                .addValue( 4, 0, 0, 255 )
                .addValue( 8888, 77, 77, 77 )
                .addValue( 8888, 77, 77, 77 ) );
        result.add( new PredefinedColorMap( "Slope" )
                .addValue( -5.0, 255, 0, 0 )
                .addValue( -2.0, 255, 0, 128 )
                .addValue( -2.0, 255, 0, 128 )
                .addValue( -1.0, 255, 0, 255 )
                .addValue( -1.0, 255, 0, 255 )
                .addValue( -0.7, 128, 0, 255 )
                .addValue( -0.7, 128, 0, 255 )
                .addValue( -0.5, 0, 0, 255 )
                .addValue( -0.5, 0, 0, 255 )
                .addValue( -0.3, 0, 128, 255 )
                .addValue( -0.3, 0, 128, 255 )
                .addValue( -0.1, 0, 255, 255 )
                .addValue( -0.1, 0, 255, 255 )
                .addValue( -0.07, 0, 255, 128 )
                .addValue( -0.07, 0, 255, 128 )
                .addValue( -0.03, 0, 255, 0 )
                .addValue( -0.03, 0, 255, 0 )
                .addValue( -0.01, 128, 255, 0 )
                .addValue( -0.01, 128, 255, 0 )
                .addValue( 0, 255, 255, 0 )
                // invert
                .addValue( 0, 255, 255, 0 )
                .addValue( 0.01, 128, 255, 0 )
                .addValue( 0.01, 128, 255, 0 )
                .addValue( 0.03, 0, 255, 0 )
                .addValue( 0.03, 0, 255, 0 )
                .addValue( 0.07, 0, 255, 128 )
                .addValue( 0.07, 0, 255, 128 )
                .addValue( 0.1, 0, 255, 255 )
                .addValue( 0.1, 0, 255, 255 )
                .addValue( 0.3, 0, 128, 255 )
                .addValue( 0.3, 0, 128, 255 )
                .addValue( 0.5, 0, 0, 255 )
                .addValue( 0.5, 0, 0, 255 )
                .addValue( 0.7, 128, 0, 255 )
                .addValue( 0.7, 128, 0, 255 )
                .addValue( 1.0, 255, 0, 255 )
                .addValue( 1.0, 255, 0, 255 )
                .addValue( 2.0, 255, 0, 128 )
                .addValue( 2.0, 255, 0, 128 )
                .addValue( 5.0, 255, 0, 0 ) );
        result.add( new PredefinedColorMap( "Geomorph" )
                .addValue( 1000.0, 127, 127, 127 )
                .addValue( 1001.0, 108, 0, 0 )
                .addValue( 1002.0, 255, 0, 0 )
                .addValue( 1003.0, 255, 165, 0 )
                .addValue( 1004.0, 255, 219, 61 )
                .addValue( 1005.0, 255, 255, 0 )
                .addValue( 1006.0, 143, 203, 44 )
                .addValue( 1007.0, 50, 189, 160 )
                .addValue( 1008.0, 0, 0, 255 ) );
        return result;
    });
    
    private static final double UNDEFINED = Double.MIN_VALUE;
    
    // instance *******************************************
    
    public String               name;
    
    public Color                novalue;
    
    public List<Entry>          entries = new ArrayList();
    
    public RasterColorMapType   type;  // = RasterColorMapType.RAMP;
    
    class Entry {
        /** Color with alpha/opacity. */
        public Color            color;
        /** Optional: preset value. */
        public double           value = UNDEFINED;
        
        public Entry( double value, Color color ) {
            this.value = value;
            this.color = color;
        }
    }
    
    public PredefinedColorMap( String name ) {
        this.name = name;
    }

    public PredefinedColorMap type( RasterColorMapType newType ) {
        this.type = newType;
        return this;
    }
    
    protected PredefinedColorMap add( int color ) {
        entries.add( new Entry( UNDEFINED, new Color( color ) ) );
        return this;
    }

    protected PredefinedColorMap add( int r, int g, int b ) {
        entries.add( new Entry( UNDEFINED, new Color( r, g, b ) ) );
        return this;
    }

    protected PredefinedColorMap add( int r, int g, int b, int a ) {
        entries.add( new Entry( UNDEFINED, new Color( r, g, b, a ) ) );
        return this;
    }

    protected PredefinedColorMap addValue( double value, int r, int g, int b ) {
        entries.add( new Entry( value, new Color( r, g, b ) ) );
        return this;
    }

    protected PredefinedColorMap addNoValue( int r, int g, int b, int a ) {
        assert novalue == null;
        novalue = new Color( r, g, b, a );
        return this;
    }

    public void fillModel( RasterColorMapStyle style, GridCoverage2D grid, IProgressMonitor monitor ) {
       ConstantRasterColorMap newColorMap = style.colorMap.createValue( ConstantRasterColorMap.defaults() );
       fillModel( newColorMap, grid, monitor );    
    }
    
    public void fillModel( ConstantRasterColorMap newColorMap, GridCoverage2D grid, IProgressMonitor monitor ) {
        assert !entries.isEmpty();
        
        monitor.beginTask( "Color map", 10 );
        
        Lazy<double[]> minmax = new PlainLazyInit( () -> {
            double[] result = minMax3( grid, SubMonitor.on( monitor, 9 ) );
            //assert result[0] < result[1] : "Min/max: " + result[0] + " / " + result[1];
            return result;
        });
        
        monitor.subTask( "creating entries" );
        if (novalue != null) {
            newColorMap.entries.createElement( proto -> {
                proto.r.set( novalue.getRed() );
                proto.g.set( novalue.getGreen() );
                proto.b.set( novalue.getBlue() );
                proto.opacity.set( ((double)novalue.getAlpha())/255 );
                proto.value.set( minmax.get()[2] );
                return proto;
            });
        }
        
        AtomicDouble breakpoint = new AtomicDouble( -1 );
        for (Entry entry : entries) {
            newColorMap.entries.createElement( proto -> {
                //log.info( "Breakpoint: " + finalBreakpoint + " -> " + entry.color );
                proto.r.set( entry.color.getRed() );
                proto.g.set( entry.color.getGreen() );
                proto.b.set( entry.color.getBlue() );
                proto.opacity.set( ((double)entry.color.getAlpha())/255 );
                
                if (entry.value != UNDEFINED) {
                    proto.value.set( entry.value );
                }
                else {
                    if (breakpoint.get() == -1) {
                        breakpoint.set( minmax.get()[0] );
                    }
                    proto.value.set( breakpoint.get() );

                    double range = minmax.get()[1] - minmax.get()[0];
                    double step = range / entries.size();
                    breakpoint.addAndGet( step );
                }
                return proto;
            });
        }
        monitor.done();
    }
    
    /**
     * 
     * @return double[] {min, max, novalue}
     */
    protected double[] minMax3( GridCoverage2D grid, IProgressMonitor monitor ) {
        @SuppressWarnings( "hiding" )
        double novalue = -9999.0; // see OmsRasterReader 
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        
        RenderedImage renderedImage = grid.getRenderedImage();
        monitor.beginTask( "calculating min/max", renderedImage.getHeight() );
        RectIter iter = RectIterFactory.create( renderedImage, null );
        Timer timer = new Timer();
        int sampleCount = 0;
        do {
            do {
                double value = iter.getSampleDouble();

                if (value == novalue) {
                    continue;
                }
                if (value < novalue) {
                    throw new IllegalStateException( "XXX value < novalue : " + value );
                }
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
                sampleCount ++;
            } 
            while (!iter.nextPixelDone());
            iter.startPixels();
            monitor.worked( 1 );
            //monitor.subTask( String.valueOf( sampleCount ) );
        }
        // XXX still a time limit 
        // http://github.com/Polymap4/polymap4-core/issues/108 will be better/faster
        while (!iter.nextLineDone() && timer.elapsedTime()<5000);
        monitor.done();
        log.info( "minMax(): " + sampleCount + " samples in " + timer.elapsedTime() + "ms" );
      
        // XXX check novalue
        return new double[] {min, max, novalue};
    }

    
    /**
     * Row/col iteration.
     * 
     * @deprecated Slower than {@link #minMax3(GridCoverage2D, IProgressMonitor)} and
     *             white-locks sometimes for unknown reason.
     * @return double[] {min, max, novalue}
     */
    protected double[] minMax( GridCoverage2D grid, IProgressMonitor monitor ) {
        // really, someday I have to learn about all this stuff :/
        GridGeometry2D geometry = grid.getGridGeometry();
        GridEnvelope gridRange = geometry.getGridRange();
        int w = gridRange.getHigh( 0 );
        monitor.beginTask( "calculating min/max", w );

        @SuppressWarnings( "hiding" )
        double novalue = -9999.0; // see OmsRasterReader 
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
              
        // FIXME http://github.com/Polymap4/polymap4-p4/issues/129 does not come to an end
        // without the limit
        double[] values = new double[1];
        int startX = gridRange.getLow( 0 );
        int endX = Math.min( 2000, gridRange.getHigh( 0 ) );
        int startY = gridRange.getLow( 1 );
        int endY = Math.min( 2000, gridRange.getHigh( 1 ) );
        
        // XXX using an iterator could be faster
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                grid.evaluate( new GridCoordinates2D( x, y ), values );
                if (values[0] == novalue) {
                    continue;
                }
                if (values[0] < novalue) {
                    throw new IllegalStateException( "XXX value < novalue : " + values[0] );
                }
                if (values[0] < min) {
                    min = values[0];
                }
                if (values[0] > max) {
                    max = values[0];
                }
            }
            monitor.worked( 1 );
        }
        monitor.done();
        
        // XXX check novalue
        return new double[] {min, max, novalue};
    }

//    /**
//     * This version uses {@link OmsRasterSummary} which is faster and more robust
//     * than {@link #minMax(GridCoverage2D, IProgressMonitor)}. But, it assumes
//     * <code>novalue</code> to be NaN which is the default in JGrasstools.
//     * JGrasstools transforms -9999 from background data source into NaN while reading.
//     * 
//     * @return double[] {min, max, novalue}
//     * @throws Exception
//     */
//    protected double[] minMax2( GridCoverage2D grid, IProgressMonitor monitor ) {
//        try {
//            monitor.beginTask( "calculating min/max", IProgressMonitor.UNKNOWN );
//            double[] minMax = OmsRasterSummary.getMinMax( grid );
//            monitor.done();
//            return new double[] {minMax[0], minMax[1], -9999.0};        
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }

}
