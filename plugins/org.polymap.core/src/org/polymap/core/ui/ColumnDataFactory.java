/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.ui;

import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ColumnDataFactory {

    public enum Alignment {
        CENTER( ColumnLayoutData.CENTER ),
        LEFT( ColumnLayoutData.LEFT ),
        RIGHT( ColumnLayoutData.RIGHT ),
        FILL( ColumnLayoutData.FILL );
        
        protected int alignment = -1;

        private Alignment( int alignment ) {
            this.alignment = alignment;
        }
    }

    
    // static factories ***********************************
    
    /**
     * Constructs a new instance with default settings set. The internal
     * {@link ColumnLayoutData} instance is copied from the the instance already set
     * on the Control. All settings made on the factory are immediately applied to
     * the Control.
     */
    public static ColumnDataFactory on( Control applyTo ) {
        assert applyTo != null;
        return new ColumnDataFactory( applyTo );
    }
    
    /**
     * Constructs a new instance with default settings.
     */
    public static ColumnDataFactory defaults() {
        return new ColumnDataFactory( (Control)null );
    }

    
    // instance *******************************************
    
    private ColumnLayoutData    data;

    /** Optional control to set the layout data on. */
    private Control             applyTo;


    /**
     * Constructs a new instance with default settings.
     */
    protected ColumnDataFactory( Control applyTo ) {
        this.data = new ColumnLayoutData();
        this.applyTo = applyTo;
        
        if (applyTo != null) {
            assert applyTo.getParent().getLayout() instanceof ColumnLayout : "Parent has wrong layout set: " + applyTo.getParent().getLayout().getClass().getName();
            
            if (applyTo.getLayoutData() != null) {
                doCopy( (ColumnLayoutData)applyTo.getLayoutData() );
            }
            applyTo.setLayoutData( data );
        }
    }

    /**
     * Constructs a new factory with values initialized from the given FormData instance. The given
     * FormData instance is copied, its values are not changed. 
     *
     * @param other Formdata
     */
    protected ColumnDataFactory doCopy( ColumnLayoutData other ) {
        data.heightHint = other.heightHint;
        data.widthHint = other.widthHint;
        data.horizontalAlignment = other.horizontalAlignment;
        return this;
    }
    
    public ColumnDataFactory widthHint( int widhtHint ) {
        data.widthHint = widhtHint;
        return this;
    }
    
    public ColumnDataFactory heightHint( int heightHint ) {
        data.heightHint = heightHint;
        return this;
    }
    
    public ColumnDataFactory horizAlign( Alignment alignment ) {
        data.horizontalAlignment = alignment.alignment;
        return this;
    }

    public ColumnLayoutData create() {
        return data;
    }
    
    public <T extends Control> T  applyTo( T control ) {
        assert applyTo != null : "Control must not be null.";
        assert applyTo.getParent().getLayout() instanceof ColumnLayout : "Parent has wrong layout set: " + applyTo.getParent().getLayout().getClass().getName();

        control.setLayoutData( create() );
        return control;
    }
}
