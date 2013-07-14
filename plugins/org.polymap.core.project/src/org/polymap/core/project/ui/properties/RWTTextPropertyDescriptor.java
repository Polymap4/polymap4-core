/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.project.ui.properties;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.CellEditor;

import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.polymap.core.ui.RWTTextCellEditor;

/**
 * Fix a bug in RAP <= 1.3.2 and Firefox: see {@link RWTTextCellEditor}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RWTTextPropertyDescriptor
        extends TextPropertyDescriptor {

    public RWTTextPropertyDescriptor( Object id, String displayName ) {
        super( id, displayName );
    }

    @Override
    public CellEditor createPropertyEditor( Composite parent ) {
        CellEditor editor = new RWTTextCellEditor( parent );
        if (getValidator() != null) {
            editor.setValidator( getValidator() );
        }
        return editor;
    }
    
}
