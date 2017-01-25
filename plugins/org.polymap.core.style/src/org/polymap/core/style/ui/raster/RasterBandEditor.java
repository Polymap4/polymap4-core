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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.raster.RasterBand;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class RasterBandEditor
        extends StylePropertyEditor<StylePropertyValue<RasterBand>> {

    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return RasterBand.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }

    
    @Override
    public String label() {
        return "Raster band";
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Label msg = new Label( contents, SWT.NONE );
        msg.setText( "Raster band..." );
        return contents;
    }

    
    @Override
    public void updateProperty() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
