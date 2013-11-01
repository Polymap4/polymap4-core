/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.ui;

import org.eclipse.ui.forms.widgets.ColumnLayout;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ColumnLayoutFactory {

    /**
     * Creates a new factory with margins and spacing set to <code>0</code>.
     * 
     * @see new {@link ColumnLayout#ColumnLayout()}
     * @return Newly created factory.
     */
    public static ColumnLayoutFactory defaults() {
        ColumnLayoutFactory factory = new ColumnLayoutFactory();
        factory.margins( 0, 0 );
        factory.spacing( 0 );
        return factory;
    }
    
    /**
     * Creates a new factory with SWT default values set.
     * 
     * @see new {@link ColumnLayout#ColumnLayout()}
     * @return Newly created factory.
     */
    public static ColumnLayoutFactory swtDefaults() {
        return new ColumnLayoutFactory();
    }
    
    
    // instance *******************************************
    
    private ColumnLayout        layout;
    
    
    protected ColumnLayoutFactory() {
        layout = new ColumnLayout();
        layout.maxNumColumns = 1;
        margins( 0, 0 );
        spacing( 0 );
    }
    
    public ColumnLayoutFactory spacing( int spacing ) {
        layout.verticalSpacing = spacing;
        layout.horizontalSpacing = spacing;
        return this;
    }

    public ColumnLayoutFactory margins( int width, int height ) {
        layout.leftMargin = width;
        layout.rightMargin = width;
        layout.topMargin = height;
        layout.bottomMargin = height;
        return this;
    }

    public ColumnLayoutFactory margins( int margin ) {
        layout.leftMargin = margin;
        layout.rightMargin = margin;
        layout.topMargin = margin;
        layout.bottomMargin = margin;
        return this;
    }

    public ColumnLayoutFactory columns( int min, int max ) {
        layout.minNumColumns = min;
        layout.maxNumColumns = max;
        return this;
    }
    
    public ColumnLayout create() {
        return layout;
    }
    
}
