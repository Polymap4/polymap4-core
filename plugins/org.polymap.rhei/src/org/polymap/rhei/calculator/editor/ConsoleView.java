/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
package org.polymap.rhei.calculator.editor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.RheiPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 1.0
 */
public class ConsoleView
        extends ViewPart
        implements IViewPart {

    private static Log log = LogFactory.getLog( ConsoleView.class );

    public static final String          ID = "org.polymap.rhei.calculator.editor.ConsoleView";

    public static final String          ENCODING = "ISO-8859-1";


    /**
     * Makes sure that the view is open. If the view is already open, then it
     * gets activated.
     * 
     * @return The view.
     */
    public static ConsoleView open() {
        final ConsoleView[] result = new ConsoleView[1];
        Polymap.getSessionDisplay().syncExec( new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    result[0] = (ConsoleView)page.showView( ID );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, null, e.getMessage(), e );
                }
            }
        });
        return result[0];
    }
    
    
    // instance *******************************************
    
    private Text                    text;
    
    private ConsoleOutputStream     cout, cerr;
    
    
    public ConsoleView() {
        super();
        this.cout = new ConsoleOutputStream();
        this.cerr = new ConsoleOutputStream();
    }

    public PrintStream getOut() {
        try {
            return new PrintStream( cout, true, ENCODING );
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException( e );
        }
    }
    
    public PrintStream getErr() {
        try {
            return new PrintStream( cerr, true, ENCODING );
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException( e );
        }
    }
    
    public void clear() {
        cout.reset();
        cerr.reset();
        if (text != null) {
            text.setText( "" );
        }
    }
    
    
    public void createPartControl( Composite parent ) {
        Composite content = new Composite( parent, SWT.NONE );
        FormLayout layout = new FormLayout();
        content.setLayout( layout );
        
        // text
        text = new Text( content, SWT.MULTI | SWT.WRAP );
        text.setForeground( Graphics.getColor( 0x30, 0x30, 0xa0 ) );
        FormData ld = new FormData();
        ld.top = new FormAttachment( 0 );
        ld.left = new FormAttachment( 0 );
        ld.bottom = new FormAttachment( 100 );
        ld.right = new FormAttachment( 100 );
        text.setLayoutData( ld );
    }


    public void setFocus() {
    }
    
    
    /**
     * 
     */
    protected class ConsoleOutputStream
            extends ByteArrayOutputStream {

        public synchronized void flush()
                throws IOException {
            super.flush();
            String line = toString( ENCODING );
            text.append( line );
            super.reset();
        }

    }
    
}
