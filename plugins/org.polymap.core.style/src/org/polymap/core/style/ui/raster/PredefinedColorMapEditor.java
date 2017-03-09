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
import java.util.List;
import java.util.Optional;
import java.util.Random;

import java.io.File;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.opengis.coverage.grid.GridEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.raster.ConstantRasterColorMap;
import org.polymap.core.style.model.raster.RasterColorMap;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.SelectionAdapter;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class PredefinedColorMapEditor
        extends StylePropertyEditor<ConstantRasterColorMap> {

    private static final Log log = LogFactory.getLog( PredefinedColorMapEditor.class );
    
    private static final IMessages i18n = Messages.forPrefix( "PredefinedColorMapEditor" );

    private List<PredefinedColorMap> input;
    

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
        
        ComboViewer combo = new ComboViewer( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        combo.getCombo().setVisibleItemCount( 10 );
        combo.setLabelProvider( new LabelProvider() {
            @Override
            public String getText( Object elm ) {
                return ((PredefinedColorMap)elm).name;
            }
        });
        combo.setComparator( new ViewerComparator() {
            @Override
            public int compare( Viewer viewer, Object elm1, Object elm2 ) {
                PredefinedColorMap cm1 = (PredefinedColorMap)elm1;
                PredefinedColorMap cm2 = (PredefinedColorMap)elm2;
                return cm1.name.compareToIgnoreCase( cm2.name );
            }
        });
        combo.setContentProvider( ArrayContentProvider.getInstance() );
        combo.setInput( input = PredefinedColorMap.all.get() );

        findSelected()
                .ifPresent( selected -> combo.setSelection( new StructuredSelection( selected ) ) );

        combo.addSelectionChangedListener( ev -> {
            ConstantRasterColorMap newColorMap = prop.createValue( ConstantRasterColorMap.defaults() );
            newColorMap.entries.clear();

            PredefinedColorMap colorMap = SelectionAdapter.on( ev.getSelection() ).first( PredefinedColorMap.class ).get();
            UIJob.schedule( "Color map", monitor -> {
                //Thread.sleep( 3000 );
                colorMap.fillModel( newColorMap, site().gridCoverage.get(), monitor );
            });
        });
        return contents;
    }

    
    /**
     * Checks if a predefined colormap exists that has the same color sequence as the
     * current value.
     */
    protected Optional<PredefinedColorMap> findSelected() {
        ConstantRasterColorMap current = prop.get();
        if (current != null) {
            nextPredefined: for (PredefinedColorMap predefined : input) {
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
                    log.info( "found: " + predefined.name );
                    return Optional.of( predefined );
                }
            }
        }
        return Optional.empty();
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
