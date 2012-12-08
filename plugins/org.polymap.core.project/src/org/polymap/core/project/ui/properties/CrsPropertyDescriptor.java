/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.ui.CRSDialogCellEditor;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CrsPropertyDescriptor
        extends PropertyDescriptor {
    
    public CrsPropertyDescriptor(Object id, String displayName) {
        super( id, displayName );

        setLabelProvider( new LabelProvider() {
            public String getText( Object elm ) {
                return CRS.toSRS( (CoordinateReferenceSystem)elm );
            }
        } );
    }

    public CellEditor createPropertyEditor( Composite parent ) {
        CellEditor editor = new CRSDialogCellEditor( parent );
        return editor;
    }

}
