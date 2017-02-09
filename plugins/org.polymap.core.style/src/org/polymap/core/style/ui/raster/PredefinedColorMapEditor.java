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

import java.util.Iterator;
import java.util.Random;
import java.util.stream.Collectors;

import java.io.File;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.opengis.coverage.grid.GridEnvelope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.raster.ConstantRasterColorMap;
import org.polymap.core.style.model.raster.RasterColorMap;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.UIUtils;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class PredefinedColorMapEditor
        extends StylePropertyEditor<ConstantRasterColorMap> {

    private static final IMessages i18n = Messages.forPrefix( "PredefinedColorMapEditor" );

    @Override
    public boolean init( StylePropertyFieldSite site ) {
        Class targetType = targetType( site );
        return RasterColorMap.class.isAssignableFrom( targetType ) ? super.init( site ) : false;
    }

    
    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        combo.setVisibleItemCount( 10 );

        combo.setItems( PredefinedColorMap.all.get().stream()
                .map( entry -> entry.name )
                .collect( Collectors.toList() )
                .toArray( new String[0] ) );

        combo.select( findSelected() );

        combo.addSelectionListener( UIUtils.selectionListener( ev -> {
            ConstantRasterColorMap newColorMap = prop.createValue( ConstantRasterColorMap.defaults() );
            newColorMap.entries.clear();

            PredefinedColorMap colorMap = PredefinedColorMap.all.get().get( combo.getSelectionIndex() );
            colorMap.fillModel( newColorMap, site().gridCoverage.get(), new NullProgressMonitor() );
        }));
        return contents;
    }

    
    /**
     * Checks if a predefined colormap exists that has the same color sequence as the
     * current value.
     *
     * @return The index of the found predefined colormap or 0.
     */
    protected int findSelected() {
        ConstantRasterColorMap current = prop.get();
        if (current != null) {
            int count = 0;
            nextPredefined: for (PredefinedColorMap predefined : PredefinedColorMap.all.get()) {
                if (current.entries.size() == predefined.entries.size()) {
                    Iterator<ConstantRasterColorMap.Entry> currentEntries =  current.entries.iterator();
                    Iterator<PredefinedColorMap.Entry> predefinedEntries = predefined.entries.iterator();
                    while (predefinedEntries.hasNext()) {
                        ConstantRasterColorMap.Entry currentEntry = currentEntries.next();
                        PredefinedColorMap.Entry predefinedEntry = predefinedEntries.next();
                        if (predefinedEntry.color.getRed() != currentEntry.r.get()
                                || predefinedEntry.color.getGreen() != currentEntry.g.get()
                                || predefinedEntry.color.getBlue() != currentEntry.b.get()) {
                            continue nextPredefined;
                        }
                    }
                    return count;
                }
                count ++;
            }
        }
        return 0;
    }
    
    
    @Override
    public void updateProperty() {
        throw new RuntimeException( "..." );
        //prop.createValue( ConstantRasterBand.defaults( 0 ) );
    }

    
    protected double[] minMax( GridCoverage2D grid ) {
        GridGeometry2D geometry = grid.getGridGeometry();
        GridEnvelope gridRange = geometry.getGridRange();
        int w = gridRange.getHigh( 0 );
        int h = gridRange.getHigh( 1 );

        Timer timer = new Timer(); 
        Random random = new Random();
        double min = Double.MAX_VALUE; 
        double max = Double.MIN_VALUE;
        double[] values = new double[1];
        for (int i=0; i<10000 || timer.elapsedTime() < 1000; i++) {
            grid.evaluate( new GridCoordinates2D( random.nextInt( w ), random.nextInt( h ) ), values );
            min = values[0] < min ? values[0] : min;
            max = values[0] > max ? values[0] : max;
        }
        return new double[] {min, max};
    }
    
    
    // Test ***********************************************
    
    public static void main( String[] args ) throws Exception {    
        File f = new File( "/home/falko/Data/ncrast/elevation_4326.tif" );

        AbstractGridFormat format = GridFormatFinder.findFormat( f );
        AbstractGridCoverage2DReader reader = format.getReader( f );
        
        String[] names = reader.getGridCoverageNames();
        GridCoverage2D grid = reader.read( names[0], null );
        
        GridGeometry2D geometry = grid.getGridGeometry();
        GridEnvelope gridRange = geometry.getGridRange();

        int w = gridRange.getHigh( 0 );
        int h = gridRange.getHigh( 1 );

        // all
        Timer timer = new Timer(); 
        double min = Double.MAX_VALUE; 
        double max = Double.MIN_VALUE;
        int c = 0;
        double[] buf = new double[1];
        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                double[] value = grid.evaluate( new GridCoordinates2D( x, y ), buf );
                min = value[0] < min ? value[0] : min;
                max = value[0] > max ? value[0] : max;
                c ++;
            }
        }
        System.out.println( "min/max: " + min + ".." + max + " (" + c + " in " + timer.elapsedTime() + "ms)" );

        // random
        timer.start();
        double[] minMax = new PredefinedColorMapEditor().minMax( grid );
        System.out.println( "min/max: " + minMax[0] + ".." + minMax[1] + " (" + c + " in " + timer.elapsedTime() + "ms)" );

        
        
//        final DefaultProcessor proc = new DefaultProcessor(null);
//        for (Operation o : proc.getOperations() ){
//            System.out.println(o.getName());
//            System.out.println(o.getDescription());
//            System.out.println();
//        }
    }
    
}
