/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.ui;

import org.eclipse.jface.viewers.DialogCellEditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A dialog cell editor that opens a CRSChooser dialog.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class CRSDialogCellEditor extends DialogCellEditor {
        
    public CRSDialogCellEditor( Composite parent ) {
        super( parent );
    }


    protected void updateContents( Object value ) {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem)value;
        if (crs != null) {
            String srs = CRS.toSRS( crs );
            super.updateContents( srs /*crs.getName()*/ );
        }
    }


    protected Object openDialogBox( Control cellEditorWindow ) {

        final CRSChooserDialog d = new CRSChooserDialog( cellEditorWindow.getDisplay()
                .getActiveShell(),
        // FIXME _p3: the value seems to crash the chooserDialog if its not null
                /* (CoordinateReferenceSystem) getValue() */null );
        d.setBlockOnOpen( true );
        d.open();
        if (d.getResult() == null || d.getResult().equals( getValue() ))
            return null;
        return d.getResult();
    }
}
