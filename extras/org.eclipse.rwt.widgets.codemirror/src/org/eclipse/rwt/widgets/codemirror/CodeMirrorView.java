/*
 * polymap.org 
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated by the
 * @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.eclipse.rwt.widgets.codemirror;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.part.ViewPart;

/**
 * Simple view with a {@link CodeMirror}, just for testing. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CodeMirrorView
        extends ViewPart {

    private static Log log = LogFactory.getLog( CodeMirrorView.class );

    public static final String          ID = "org.eclipse.rwt.widgets.codemirror.CodeMirrorView";

    private CodeMirror            editor;

    
    public CodeMirrorView() {
    }


    public void createPartControl( Composite parent ) {
        this.editor = new CodeMirror( parent, SWT.NONE );
        this.editor.setLayoutData( new GridData( 100, 100 ) );
        this.editor.setText( "// comment\nint i = 0;" );
    }


    public void setFocus() {
        editor.setFocus();
    }
    
}
