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
import java.util.Random;

import java.awt.Color;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.opengis.coverage.grid.GridEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.Timer;
import org.polymap.core.style.model.raster.ConstantRasterColorMap;
import org.polymap.core.style.model.raster.RasterColorMapStyle;

/**
 * Predefined color maps. 
 *
 * @author Falko Bräutigam
 */
public class PredefinedColorMap {

    private static final Log log = LogFactory.getLog( PredefinedColorMap.class );

    public static final PredefinedColorMap ELEVATION = new PredefinedColorMap( "Elevation" )
            .add( 0, 191, 191 ).add( 0, 255, 0 ).add( 255, 255, 0 ).add( 255, 127, 0 ).add( 191, 127, 63 ).add( 20, 21, 20 );

    /** Falko's special, first ever colormap :) */
    public static final PredefinedColorMap FALKOS = new PredefinedColorMap( "Falko's" )
            .add( 0x000000 ).add( 0xff0000 ).add( 0x00ff00 ).add( 0x0000ff );

    public static final Lazy<List<PredefinedColorMap>> all = new LockedLazyInit( () -> {
        PredefinedColorMap reverse = new PredefinedColorMap( "Reverse" );
        reverse.entries.addAll( Lists.reverse( ELEVATION.entries ) );

        List<PredefinedColorMap> result = new ArrayList();
        result.add( ELEVATION );
        result.add( reverse );
        result.add( FALKOS );
        result.add( new PredefinedColorMap( "Red" ).add( 0x000000 ).add( 0xff0000 ) );
        result.add( new PredefinedColorMap( "Green" ).add( 0x000000 ).add( 0x00ff00 ) );
        result.add( new PredefinedColorMap( "Blue" ).add( 0x000000 ).add( 0x0000ff ) );
        return result;
    });
    
    // instance *******************************************
    
    public String               name;
    
    public List<Color>          entries = new ArrayList();
    
    
    public PredefinedColorMap( String name ) {
        this.name = name;
    }

    protected PredefinedColorMap add( int color ) {
        entries.add( new Color( color ) );
        return this;
    }

    protected PredefinedColorMap add( int r, int g, int b ) {
        entries.add( new Color( r, g, b ) );
        return this;
    }

    public void fillModel( RasterColorMapStyle style, GridCoverage2D grid, IProgressMonitor monitor ) {
       ConstantRasterColorMap newColorMap = style.colorMap.createValue( ConstantRasterColorMap.defaults() );
       fillModel( newColorMap, grid, monitor );    
    }
    
    public void fillModel( ConstantRasterColorMap newColorMap, GridCoverage2D grid, IProgressMonitor monitor ) {
        assert !entries.isEmpty();
        
        double[] minMax = minMax( grid, monitor );
        assert minMax[0] < minMax[1];
        double range = minMax[1] - minMax[0];
        double step = range / entries.size();
        
        double breakpoint = minMax[0];
        for (Color color : entries) {
            double finalBreakpoint = breakpoint;
            newColorMap.entries.createElement( proto -> {
                log.info( "Breakpoint: " + finalBreakpoint + " -> " + color );
                proto.r.set( color.getRed() );
                proto.g.set( color.getGreen() );
                proto.b.set( color.getBlue() );
                proto.value.set( finalBreakpoint );
                return proto;
            });
            breakpoint += step;
        }
    }
    
    /**
     * 
     * @return double[] {min, max, novalue}
     */
    protected double[] minMax( GridCoverage2D grid, IProgressMonitor monitor ) {
        GridGeometry2D geometry = grid.getGridGeometry();
        GridEnvelope gridRange = geometry.getGridRange();
        int w = gridRange.getHigh( 0 );
        int h = gridRange.getHigh( 1 );

        Timer timer = new Timer(); 
        Random random = new Random();
        double novalue = Double.MAX_VALUE; 
        double min = Double.MAX_VALUE; 
        double max = Double.MIN_VALUE;
        double[] values = new double[1];
        for (int i=0; i<10000 || timer.elapsedTime() < 1000; i++) {
            grid.evaluate( new GridCoordinates2D( random.nextInt( w ), random.nextInt( h ) ), values );
            if (values[0] < novalue) {
                min = novalue;
                novalue = values[0];
            }
            if (values[0] > max) {
                max = values[0];
            }
        }
        // XXX check novalue
        return new double[] {min, max, novalue};
    }

}
