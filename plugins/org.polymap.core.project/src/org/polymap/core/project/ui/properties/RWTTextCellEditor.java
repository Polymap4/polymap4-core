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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.TextCellEditor;

/**
 * This just fixes a bug that occurs with RAP 1.3.2 and Firefox: the contents of the
 * text field cannot be edited if key listener is registered.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RWTTextCellEditor
        extends TextCellEditor {

    private static Log log = LogFactory.getLog( RWTTextCellEditor.class );

    public RWTTextCellEditor() {
        super();
    }

    public RWTTextCellEditor( Composite parent, int style ) {
        super( parent, style );
    }

    public RWTTextCellEditor( Composite parent ) {
        super( parent );
    }

    @Override
    protected Control createControl( Composite parent ) {
        text = new Text(parent, getStyle());
        text.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                handleDefaultSelection(e);
            }
        });
//        text.addKeyListener(new KeyAdapter() {
//            // hook key pressed - see PR 14201  
//            public void keyPressed(KeyEvent e) {
//                keyReleaseOccured(e);
//
//                // as a result of processing the above call, clients may have
//                // disposed this cell editor
//                if ((getControl() == null) || getControl().isDisposed()) {
//                    return;
//                }
//                checkSelection(); // see explanation below
//                checkDeleteable();
//                checkSelectable();
//            }
//        });
        text.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });
//        // We really want a selection listener but it is not supported so we
//        // use a key listener and a mouse listener to know when selection changes
//        // may have occurred
//        text.addMouseListener(new MouseAdapter() {
//            public void mouseUp(MouseEvent e) {
//                checkSelection();
//                checkDeleteable();
//                checkSelectable();
//            }
//        });
        text.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                RWTTextCellEditor.this.focusLost();
            }
        });
        text.setFont(parent.getFont());
        text.setBackground(parent.getBackground());
        text.setText("");//$NON-NLS-1$
//        text.addModifyListener(getModifyListener());
        return text;
    }
    
}
