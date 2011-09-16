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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.core.runtime.IProgressMonitor;

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

    public static final FastDateFormat  df = DateFormatUtils.ISO_DATE_FORMAT;
    

    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(),
                context.features().size() );
    
        final File f = File.createTempFile( "polymap-csv-export-", ".csv" );
        f.deleteOnExit();
        Writer writer = new OutputStreamWriter( new BufferedOutputStream( new FileOutputStream( f ) ), "ISO-8859-1" );

        CsvPreference prefs = new CsvPreference('"', ';', "\r\n");  //CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
        CsvListWriter csvWriter = new CsvListWriter( writer, prefs );

        // all features
        FeatureIterator it = context.features().features();
        int count = 0;
        try {
            boolean noHeaderYet = true;
            while (it.hasNext()) {
                if (monitor.isCanceled()) {
                    return Status.Cancel;
                }
                if ((++count % 100) == 0) {
                    monitor.subTask( "Objekte: " + count++ );
                }
                Feature feature = it.next();

                // header
                if (noHeaderYet) {
                    List<String> header = new ArrayList( 32 );
                    for (Property prop : feature.getProperties()) {
                        Class<?> binding = prop.getType().getBinding();
                        if (Number.class.isAssignableFrom( binding )
                                || Boolean.class.isAssignableFrom( binding )
                                || Date.class.isAssignableFrom( binding )
                                || String.class.isAssignableFrom( binding )) {
                            header.add( prop.getName().getLocalPart() );
                        }
                    }
                    csvWriter.writeHeader( header.toArray(new String[header.size()]) );
                    noHeaderYet = false;
                }

                // all properties
                List line = new ArrayList( 32 );
                for (Property prop : feature.getProperties()) {
                    Class binding = prop.getType().getBinding();
                    Object value = prop.getValue();

                    // Number
                    if (Number.class.isAssignableFrom( binding )) {
                        line.add( value != null ? value.toString() : "" );
                    }
                    // Boolean
                    else if (Boolean.class.isAssignableFrom( binding )) {
                        line.add( value == null ? "" :
                            ((Boolean)value).booleanValue() ? "ja" : "nein");
                    }
                    // Date
                    else if (Date.class.isAssignableFrom( binding )) {
                        line.add( value != null ? df.format( (Date)value ) : "" );
                    }
                    // String
                    else if (String.class.isAssignableFrom( binding )) {
                        String s = value != null ? (String)value : "";
                        // Excel happens to interprete decimal value otherwise! :(
                        s = StringUtils.replace( s, "/", "-" );
                        line.add( s );
                    }
                    // other
                    else {
                        log.debug( "skipping: " + prop.getName().getLocalPart() + " type:" + binding );
                    }
                }
                log.debug( "LINE: " + line );
                csvWriter.write( line );
                monitor.worked( 1 );
            }
        }
        finally {
            it.close();
            csvWriter.close();
            writer.close();
        }

        // open download        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                String url = DownloadServiceHandler.registerContent( new ContentProvider() {

                    public String getContentType() {
                        return "text/csv; charset=ISO-8859-1";
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
