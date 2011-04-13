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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.calculator.CalculatorSupport;
import org.polymap.rhei.calculator.ICalculator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 1.0
 */
public class ScriptEditor
        extends EditorPart 
        implements IEditorPart {

    private static Log log = LogFactory.getLog( ScriptEditor.class );

    public static final String          ID = "org.polymap.rhei.calculator.editor.ScriptEditor";
    
    /**
     *
     * @return The editor of the given script URL, or null.
     */
    public static ScriptEditor open( URL scriptUrl, String lang ) {
        try {
            log.debug( "open(): URL= " + scriptUrl );
            ScriptEditorInput input = new ScriptEditorInput( scriptUrl, lang );

            // check current editors
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorReference[] editors = page.getEditorReferences();
            for (IEditorReference reference : editors) {
                IEditorInput cursor = reference.getEditorInput();
                if (cursor instanceof ScriptEditorInput) {
                    log.debug( "        editor: " + cursor );
                }
                if (cursor.equals( input )) {
                    Object previous = page.getActiveEditor();
                    page.activate( reference.getPart( true ) );
                    return (ScriptEditor)reference.getEditor( false );
                }
            }

            // not found -> open new editor
            IEditorPart part = page.openEditor( input, input.getEditorId(), true,
                    IWorkbenchPage.MATCH_NONE );
            log.debug( "editor= " + part );
            // might be ErrorEditorPart
            return part instanceof ScriptEditor ? (ScriptEditor)part : null;
        }
        catch (PartInitException e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, null, e.getMessage(), e );
            return null;
        }
    }

    
    // instance *******************************************
    
    private List<Action>                actions = new ArrayList();

    private boolean                     isDirty;
    
    private boolean                     isValid;
    
    private boolean                     actionsEnabled;

    private ScriptEditorInput           input;

    private Text                        text;
    
    private Map<String,Object>          calculatorParams = new HashMap();      
    
    
    public ScriptEditor() {
    }

    
    public void setCalculatorParams( Map<String,Object> params ) {
        this.calculatorParams.clear();    
        this.calculatorParams.putAll( params );    
    }

    
    public void init( IEditorSite _site, IEditorInput _input )
            throws PartInitException {
        super.setSite( _site );
        super.setInput( _input );
        this.input = (ScriptEditorInput)_input;

        String name = StringUtils.substringAfterLast( input.getScriptUrl().getFile(), "/" );
        setPartName( name );
        setContentDescription( "Script: " + name );
        setTitleToolTip( input.getScriptUrl().toString() );
        
        // submit action
        Action submitAction = new Action( Messages.get( "ScriptEditor_submit" ) ) {
            public void run() {
                try {
                    log.debug( "submitAction.run(): ..." );
                    doSave( new NullProgressMonitor() );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                }
            }
        };
        submitAction.setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiPlugin.getDefault().getBundle().getResource( "icons/etool16/validate.gif" ) ) );
        submitAction.setToolTipText( Messages.get( "ScriptEditor_submitTip" ) );
        actions.add( submitAction );

        // revert action
        Action revertAction = new Action( Messages.get( "ScriptEditor_revert" ) ) {
            public void run() {
                log.debug( "revertAction.run(): ..." );
                doLoad( new NullProgressMonitor() );
            }
        };
        revertAction.setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiPlugin.getDefault().getBundle().getResource( "icons/etool16/revert.gif" ) ) );
        revertAction.setToolTipText( Messages.get( "ScriptEditor_revertTip" ) );
        actions.add( revertAction );

        // run action
        actions.add( new RunAction() );
    }

    
    /**
     * 
     */
    class RunAction
            extends Action {

        public RunAction() {
            super( Messages.get( "ScriptEditor_run" ) );
            setImageDescriptor( ImageDescriptor.createFromURL( 
                    RheiPlugin.getDefault().getBundle().getResource( "icons/etool16/run.gif" ) ) );
            setToolTipText( Messages.get( "ScriptEditor_runTip" ) );
        }

        public void run() {
            log.debug( "runAction.run(): ..." );
//            doSave( new NullProgressMonitor() );

            ICalculator calculator = CalculatorSupport.instance().newCalculator( text.getText(), input.getLang() );
            
            ConsoleView console = ConsoleView.open();
            console.clear();
            console.getOut().println( "*** Script startet at " + new Date() + " ..." );
            calculator.setOut( console.getOut() );
            calculator.setErr( console.getErr() );
            
            for (Map.Entry<String,Object> entry : calculatorParams.entrySet()) {
                calculator.setParam( entry.getKey(), entry.getValue() );
            }
            
            try {
                calculator.eval();
            }
            catch (Exception e) {
                log.debug( "Script eval error: ", e );
                e.printStackTrace( console.getErr() );
            }
        }
        
    }

    
    public void createPartControl( Composite parent ) {
        Composite content = new Composite( parent, SWT.NONE );
        FormLayout layout = new FormLayout();
        content.setLayout( layout );
        
        // buttonbar
        Composite buttonbar = new Composite( content, SWT.NONE );
        buttonbar.setLayout( new RowLayout() );
        FormData ld = new FormData();
        ld.top = new FormAttachment( 0 );
        ld.left = new FormAttachment( 0 );
//        ld.bottom = new FormAttachment( 80 );
        ld.right = new FormAttachment( 100 );
        buttonbar.setLayoutData( ld );

        // buttons
        for (final Action action : actions) {
            Button btn = new Button( buttonbar, SWT.PUSH );
            // XX dispose images
            btn.setImage( action.getImageDescriptor().createImage( true ) );
            btn.setToolTipText( action.getToolTipText() );
            btn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    action.run();
                }
            });
        }
        
        // seaprator
        Label sep = new Label( content, SWT.SEPARATOR | SWT.HORIZONTAL );
        ld = new FormData();
        ld.top = new FormAttachment( buttonbar );
        ld.left = new FormAttachment( 0 );
        ld.right = new FormAttachment( 100 );
        sep.setLayoutData( ld );

        
        // text
        text = new Text( content, SWT.MULTI | SWT.WRAP );
        ld = new FormData();
        ld.top = new FormAttachment( sep, 0 );
        ld.left = new FormAttachment( 0 );
        ld.bottom = new FormAttachment( 100 );
        ld.right = new FormAttachment( 100 );
        text.setLayoutData( ld );
        
        doLoad( new NullProgressMonitor() );
    }


    
//        log.debug( "fieldChange(): dirty=" + isDirty + ", isValid=" + isValid );
//        boolean old = actionsEnabled;
//        actionsEnabled = isValid && isDirty;
//        if (actionsEnabled != old) {
//            for (Action action : standardPageActions) {
//                action.setEnabled( actionsEnabled );
//            }
//            editorDirtyStateChanged();
//        }
//    }


    public void dispose() {
        super.dispose();
    }


    public boolean isDirty() {
        return isDirty;
    }


    public void doSave( IProgressMonitor monitor ) {
        log.debug( "doSave(): " + input.getScriptUrl().getPath() );
        try {
            OutputStream out = null;
            if (input.getScriptUrl().getProtocol().startsWith( "file" )) {
                out = new FileOutputStream( input.getScriptUrl().getPath() );    
            }
            else {
                URLConnection urlConn = input.getScriptUrl().openConnection(); 
                urlConn.setDoOutput( true ); 
                urlConn.setUseCaches( false );
                out = urlConn.getOutputStream();
            }
            
            InputStream in = new ByteArrayInputStream( text.getText().getBytes( "ISO-8859-1" ) );
            try {
                StreamUtils.copy( in, out );
            }
            finally {
                out.close();
            }
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void doLoad( IProgressMonitor monitor ) {
        log.debug( "doLoad(): ..." );
        try {
            URLConnection urlConn = input.getScriptUrl().openConnection(); 
            urlConn.setDoInput( true ); 
            urlConn.setUseCaches( false );

            InputStream in = urlConn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                StreamUtils.copy( in, out );
                text.setText( out.toString( "ISO-8859-1" ) );
            }
            finally {
                in.close();
            }
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void doSaveAs() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
    }

}
