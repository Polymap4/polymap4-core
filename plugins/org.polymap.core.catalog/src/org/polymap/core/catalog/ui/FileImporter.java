/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.catalog.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.catalog.Messages;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Copy files into workspace. Handle *.zip, *.tar, *.gz. Flatten file hierarchy.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FileImporter {

    private static Log log = LogFactory.getLog( FileImporter.class );
    
    private static final IMessages      i18n = Messages.forPrefix( "FileImporter" );

    
    public static List<File> run( String name, String contentType, InputStream in ) {
        FileImporter importer = new FileImporter();
        importer.doRun( name, contentType, in );
        return importer.results;
    }


    // instance *******************************************
    
    private File                targetDir = new File( Polymap.getDataDir(), "filedata" );
    
    private List<File>          results = new ArrayList();
    
    private boolean             overwrite;
    
    /** Default charset for ZIP */
    private Charset             charset = Charset.forName( "UTF8" );
    
    
    protected FileImporter() {
        targetDir.mkdirs();
    }
    
    
    public FileImporter setOverwrite( boolean overwrite ) {
        this.overwrite = overwrite;
        return this;
    }

    
    public FileImporter setCharset( Charset charset ) {
        this.charset = charset;
        return this;
    }


    public List<File> doRun( String name, String contentType, InputStream in ) {
        try {
            handle( name, contentType, in );
            return results;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }


    protected void handle( String name, String contentType, InputStream in ) throws Exception {
        contentType = contentType == null ? "" : contentType;
        if (name.toLowerCase().endsWith( ".zip" ) || contentType.equalsIgnoreCase( "application/zip" )) {
            handleZip( name, in );
        }
        else if (name.toLowerCase().endsWith( ".tar" ) || contentType.equalsIgnoreCase( "application/tar" )) {
            handleTar( name, in );
        }
        else if (name.toLowerCase().endsWith( "gz" ) || name.toLowerCase().endsWith( "gzip" ) 
                || contentType.equalsIgnoreCase( "application/gzip" )) {
            handleGzip( name, in );
        }
        else {
            handleFile( name, in );
        }
    }
    
    
    protected void handleGzip( String name, InputStream in ) throws Exception {
        log.info( "    GZIP: " + name );
        GZIPInputStream gzip = new GZIPInputStream( in );
        String nextName = null;
        if (name.toLowerCase().endsWith( ".gz" )) {
            nextName = name.substring( 0, name.length() - 3 );
        }
        else if (name.toLowerCase().endsWith( ".tgz" )) {
            nextName = name.substring( 0, name.length() - 3 ) + "tar";
        }
        else {
            nextName = name.substring( 0, name.length() - 2 );            
        }
        handle( nextName, null, gzip );
    }


    protected void handleFile( String name, InputStream in ) throws Exception {
        log.info( "    FILE: " + name );
        File target = new File( targetDir, FilenameUtils.getName( name ) );
        
        boolean ok = true;
        if (!overwrite && target.exists()) {
            ok = MessageDialog.openQuestion(
                    PolymapWorkbench.getShellToParentOn(),
                    i18n.get( "fileExists_title", target.getName() ), i18n.get( "fileExists_msg", target.getName() ) );
        }
        if (ok) {
            OutputStream out = new FileOutputStream( target );
            try {
                IOUtils.copy( in, out );
            }
            finally {
                IOUtils.closeQuietly( out );
            }
            results.add( target );
        }
    }
    
    
    protected void handleZip( String name, InputStream in ) throws Exception {
        log.info( "    ZIP: " + name );
        try {
            ZipInputStream zip = new ZipInputStream( in, charset );
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    handle( entry.getName(), null, zip );
                }
            }
        }
        catch (Exception e) {
            if (e instanceof IllegalArgumentException || e.getMessage().equals( "MALFORMED" )) {
                throw new IOException( i18n.get( "wrongCharset", charset ) );
            }
            else {
                throw e;
            }
        }
    }


    protected void handleTar( String name, InputStream in ) throws Exception {
        log.info( "    TAR: " + name );
        TarInputStream tar = new TarInputStream( in );
        TarEntry entry = null;
        while ((entry = tar.getNextEntry()) != null) {
            if (entry.isDirectory()) {
            }
            else {
                handle( entry.getName(), null, in );
            }
        }
    }

}
