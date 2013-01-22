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
package org.polymap.core.mapeditor.tooling;

import org.eclipse.ui.forms.widgets.ColumnLayout;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ColumnLayoutFactory {

    public static ColumnLayoutFactory defaults() {
        return new ColumnLayoutFactory();
    }
    
    public static ColumnLayoutFactory fillDefaults() {
        return new ColumnLayoutFactory();
    }
    
    
    // instance *******************************************
    
    private ColumnLayout        layout;
    
    protected ColumnLayoutFactory() {
        layout = new ColumnLayout();
        layout.maxNumColumns = 1;
        margin( 0, 0 );
        spacing( 0 );
    }
    
    public ColumnLayoutFactory spacing( int spacing ) {
        layout.verticalSpacing = spacing;
        layout.horizontalSpacing = spacing;
        return this;
    }

    public ColumnLayoutFactory margin( int x, int y ) {
        layout.leftMargin = x;
        layout.rightMargin = x;
        layout.topMargin = y;
        layout.bottomMargin = y;
        return this;
    }

    public ColumnLayout create() {
        return layout;
    }
    
}
