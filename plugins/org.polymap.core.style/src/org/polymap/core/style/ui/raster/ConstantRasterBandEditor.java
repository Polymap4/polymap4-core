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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.geotools.coverage.GridSampleDimension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.style.model.raster.ConstantRasterBand;
import org.polymap.core.style.model.raster.RasterBand;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ConstantRasterBandEditor
        extends StylePropertyEditor<ConstantRasterBand> {

    @Override
    public boolean init( StylePropertyFieldSite site ) {
        Class targetType = targetType( site );
        return RasterBand.class.isAssignableFrom( targetType ) ? super.init( site ) : false;
    }

    
    @Override
    public String label() {
        return "band";
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        GridSampleDimension[] bands = site().gridCoverage.get().getSampleDimensions();
        combo.setItems( Arrays.stream( bands )
                .map( band -> band.getDescription().toString() )
                .collect( Collectors.toList() )
                .toArray( new String[0] ) );
        combo.select( prop.get().band.get() );

        combo.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().band.set( combo.getSelectionIndex() );
            }
        });
        return contents;
    }

    
    @Override
    public void updateProperty() {
        prop.createValue( ConstantRasterBand.defaults( 0 ) );
    }
    
}
