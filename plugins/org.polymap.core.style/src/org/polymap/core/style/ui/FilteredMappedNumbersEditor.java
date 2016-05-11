/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.ui;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.style.model.FilterMappedNumbers;

/**
 * Editor that creates {@link FilterMappedNumbers}. 
 *
 * @author Falko Bräutigam
 */
class FilteredMappedNumbersEditor
        extends StylePropertyEditor<FilterMappedNumbers> {

    private static Log log = LogFactory.getLog( FilteredMappedNumbersEditor.class );
    
    @Override
    public String label() {
        return "Filtered mapped numbers";
    }

    
    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Number.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }

    
    @Override
    public void updateProperty() {
        try {
            // FIXME just create anything for testing
            prop.createValue( FilterMappedNumbers.defaults() )
                    .add( 0.1, ff.equals( ff.literal( 1 ), ff.literal( 1 ) ) )
                    .add( 0.2, ff.equals( ff.literal( 2 ), ff.literal( 2 ) ) );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        new Label( parent, SWT.NONE ).setText( "List of numbers..." );
        return contents;
    }
    
}
