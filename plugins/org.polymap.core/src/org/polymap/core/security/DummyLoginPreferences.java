/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.security;

import java.io.File;

import org.apache.commons.io.FileUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.polymap.core.CorePlugin;
import org.polymap.core.Messages;
import org.polymap.core.ui.StatusDispatcher;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class DummyLoginPreferences
        extends PreferencePage
        implements IWorkbenchPreferencePage {

    private DummyLoginModule    loginModule;
    
    private File                loginConfigFile;
    
    private Text                editor;
    
    
    public DummyLoginPreferences( DummyLoginModule loginModule ) {
        this.loginModule = loginModule;
    }


    public void init( IWorkbench workbench ) {
        noDefaultAndApplyButton();
    }


    protected Control createContents( Composite parent ) {
        Composite contents = new Composite( parent, SWT.NONE );
        contents.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        Label msg = new Label( contents, SWT.WRAP ); 
        msg.setText( Messages.get( "DummyLoginPreferences_msg" ) );

        editor = new Text( contents, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER );
        loginConfigFile = loginModule.getConfigFile();
        if (loginConfigFile != null) {
            try {
                editor.setText( FileUtils.readFileToString( loginConfigFile, "ISO-8859-1" ) );
            }
            catch (Exception e) {
                StatusDispatcher.handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }

        // Layout
        FormLayout layout = new FormLayout();
        contents.setLayout( layout );

        FormData msgData = new FormData();
        msgData.top = new FormAttachment( 0, 0 );
        msgData.left = new FormAttachment( 0, 0 );
        msgData.right = new FormAttachment( 100, 0);
        msg.setLayoutData( msgData );

        FormData editorData = new FormData();
        editorData.top = new FormAttachment( msg, 5 );
        editorData.left = new FormAttachment( 0, 0 );
        editorData.right = new FormAttachment( 100, 0);
        editorData.bottom = new FormAttachment( 100, 0);
        editor.setLayoutData( editorData );

        return contents;
    }


    public boolean isValid() {
        return true;
    }


    public boolean okToLeave() {
        return true;
    }


    public boolean performCancel() {
        return true;
    }


    public boolean performOk() {
        try {
            FileUtils.writeStringToFile( loginConfigFile, editor.getText(), "ISO-8859-1" );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
        return true;
    }
    

    protected void performDefaults() {
        try {
            editor.setText( FileUtils.readFileToString( loginConfigFile, "ISO-8859-1" ) );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
        super.performDefaults();
    }


    public void performHelp() {
        throw new RuntimeException( "not yet implemented." );
    }

}
