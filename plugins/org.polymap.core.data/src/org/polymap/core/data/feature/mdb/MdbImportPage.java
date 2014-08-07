/* 
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.data.feature.mdb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.healthmarketscience.jackcess.Database;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.upload.IUploadHandler;
import org.polymap.core.ui.upload.Upload;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class MdbImportPage
        extends WizardPage
        implements IWizardPage, IUploadHandler {

    private static Log log = LogFactory.getLog( MdbImportPage.class );

    public static final String          ID = "CsvImportWizardPage"; //$NON-NLS-1$

    File                                dbFile;
    
    String[]                            tableNames;
    
    private Upload                      upload;
    
    private List                        tablesList;


    protected MdbImportPage() {
        super( ID );
        setTitle( "Datenbank-Datei ausw�hlen." );
        setDescription( "W�hlen Sie eine *.mdb Datei zum importieren aus.");
    }


    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite( parent, SWT.NONE );
        FormLayout layout = new FormLayout();
        layout.spacing = 5;
        fileSelectionArea.setLayout( layout );

        upload = new Upload( fileSelectionArea, SWT.BORDER, /*Upload.SHOW_PROGRESS |*/ Upload.SHOW_UPLOAD_BUTTON );
//        upload.setBrowseButtonText( "Browse" );
//        upload.setUploadButtonText( "Upload" );
        upload.setHandler( this );
        FormData data = new FormData();
        data.left = new FormAttachment( 0 );
        data.right = new FormAttachment( 100 );
        upload.setLayoutData( data );
    
        tablesList = new List( fileSelectionArea, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL );
        data = new FormData();
        data.top = new FormAttachment( upload );
        data.bottom = new FormAttachment( 100 );
        data.left = new FormAttachment( 0 );
        data.right = new FormAttachment( 100 );
        tablesList.setLayoutData( data );
        tablesList.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                tableNames = tablesList.getSelection();
                checkFinish();
            }
        });
        tablesList.setLayoutData( data );
        
        setControl( fileSelectionArea );
        checkFinish();
    }

    
    protected void checkFinish() {
        setPageComplete( dbFile != null && tableNames != null && tableNames.length > 0 );
        getWizard().getContainer().updateButtons();
    }

    
    // UploadListener *************************************
    public void uploadStarted( String name, String contentType, int contentLength, InputStream in ) throws Exception {
        FileOutputStream out = null;
        try {
            log.info( "Uploaded: " + name );

            File uploadDir = Polymap.getWorkspacePath().toFile();
            dbFile = new File( uploadDir, name );
            out = new FileOutputStream( dbFile );
            IOUtils.copy( in, out );
            log.info( "### copied to: " + dbFile );

            Database db = Database.open( dbFile );
            log.info( "Tables: " + db.getTableNames() );
            
            for (String tableName : db.getTableNames()) {
                tablesList.add( tableName );
            }
            db.close();
        } 
        catch (IOException e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, MdbImportPage.this, "Fehler beim Upload der Daten.", e );
        }
        finally {
            IOUtils.closeQuietly( out );
            IOUtils.closeQuietly( in );
        }
        checkFinish();
    }

}
