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

import org.apache.commons.lang3.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.raster.ConstantRasterColorMapType;
import org.polymap.core.style.model.raster.RasterColorMapType;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.UIUtils;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ConstantRasterColorMapTypeEditor
        extends StylePropertyEditor<ConstantRasterColorMapType> {

    private static final IMessages i18n = Messages.forPrefix( "ConstantRasterColorMapTypeEditor" );

    @Override
    public boolean init( StylePropertyFieldSite site ) {
        Class targetType = targetType( site );
        return RasterColorMapType.class.isAssignableFrom( targetType ) ? super.init( site ) : false;
    }

    
    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        combo.setItems( Arrays.stream( RasterColorMapType.values() ) 
                .map( type -> StringUtils.capitalize( type.toString().toLowerCase() ) )
                .collect( Collectors.toList() )
                .toArray( new String[0] ) );
        
        prop.opt().ifPresent( constant -> {
            combo.select( constant.type.get().ordinal() );
        });

        combo.addSelectionListener( UIUtils.selectionListener( ev -> {
            RasterColorMapType selected = Arrays.stream( RasterColorMapType.values() )
                    .filter( v -> v.ordinal() == combo.getSelectionIndex() )
                    .findAny().get();
            prop.get().type.set( selected );
        }));
        return contents;
    }

    
    @Override
    public void updateProperty() {
        prop.createValue( ConstantRasterColorMapType.defaults( RasterColorMapType.RAMP ) );
    }
    
}
