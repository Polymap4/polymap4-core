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
package org.polymap.core.data.util;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.ComboBoxCellEditor;

/**
 * This combo box cell editor applies the currently selected value and deactivates
 * the cell editor immediately when an entry is clicked - instead of just setting the
 * selection and wait for focusLost to apply.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FastApplyComboBoxCellEditor
        extends ComboBoxCellEditor {

    public FastApplyComboBoxCellEditor() {
        super();
    }

    public FastApplyComboBoxCellEditor( Composite parent, String[] items, int style ) {
        super( parent, items, style );
    }

    public FastApplyComboBoxCellEditor( Composite parent, String[] items ) {
        super( parent, items );
    }

 
    protected Control createControl( Composite parent ) {
        final CCombo control = (CCombo)super.createControl( parent );

        control.addSelectionListener( new SelectionAdapter() {

            public void widgetDefaultSelected( SelectionEvent ev ) {
            }

            public void widgetSelected( SelectionEvent event ) {
                // applies the currently selected value and deactivates the cell editor
                focusLost();
            }
        } );

        return control;
    }

}
