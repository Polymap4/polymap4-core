/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operations.feature;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CsvExportFeatureOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( CsvExportFeatureOperation.class );

    
    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( 
                context.adapt( FeatureOperationExtension.class ).getLabel(),
                context.features().size() );
    
        final File f = File.createTempFile( "polymap-csv-export-", ".csv" );
        f.deleteOnExit();

        OutputStream out = new BufferedOutputStream( new FileOutputStream( f ) );
        final CsvExporter exporter = new CsvExporter();

        try {
            exporter.setLocale( Polymap.getSessionLocale() );
            exporter.write( context.features(), out, monitor );
        }
        catch (OperationCanceledException e) {
            return Status.Cancel;
        }
        finally {
            IOUtils.closeQuietly( out );
        }

        // open download        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                String url = DownloadServiceHandler.registerContent( new ContentProvider() {

                    public String getContentType() {
                        return "text/csv; charset=" + exporter.getCharset();
                    }

                    public String getFilename() {
                        return "polymap-export.csv";
                    }

                    public InputStream getInputStream() throws Exception {
                        return new BufferedInputStream( new FileInputStream( f ) );
                    }

                    public void done( boolean success ) {
                        f.delete();
                    }
                    
                });
                
                log.info( "CSV: download URL: " + url );

//                String filename = view.getLayer() != null
//                        ? view.getLayer().getLabel() + "_export.csv" : "polymap3_export.csv";
//                String linkTarget = "../csv/" + id + "/" + filename;
//                String htmlTarget = "../csv/download.html?id=" + id + "&filename=" + filename;

                ExternalBrowser.open( "download_window", url,
                        ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
            }
        });
        monitor.done();
        return Status.OK;
    }


    public Status undo( IProgressMonitor monitor )
    throws Exception {
        return Status.OK;
    }


    public Status redo( IProgressMonitor monitor )
    throws Exception {
        return Status.OK;
    }


    public boolean canExecute() {
        return true;
    }

    public boolean canRedo() {
        return true;
    }

    public boolean canUndo() {
        return true;
    }
    
}
