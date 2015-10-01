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
package org.polymap.service.fs.providers.file;

import io.milton.http.FileItem;

import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentDeletable;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentMoveable;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.IMakeFolder;
import org.polymap.service.fs.spi.NotAuthorizedException;

/**
 * 
 * <p/>
 * Impl.: Implements IContentWriteable to allow upload via browser.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FsFolder
        extends DefaultContentFolder
        implements IContentPutable, IContentWriteable, IMakeFolder, IContentMoveable, IContentDeletable {

    public FsFolder( String name, IPath parentPath, IContentProvider provider, File dir ) {
        super( name, parentPath, provider, dir );
    }

    public FsContentProvider getProvider() {
        return (FsContentProvider)super.getProvider();
    }

    public File getDir() {
        return (File)super.getSource();
    }
    

//    public String getDescription( String contentType ) {
//        // FIXME hard coded servlet path
//        String basePath = FilenameUtils.normalizeNoEndSeparator( getPath().toString() );
//        String path = "/webdav" + basePath; // + "/" + r.getName();
//
//        return "<b>Dieses Verzeichnis bietet eine Schittstelle zum Filesystem des POLYMAP3-Servers.</b>" +
//            "<p/>" +
//            "<form action=\"" + path + "\"" +
//            "  enctype=\"multipart/form-data\" method=\"post\">" +
//            "  <p>" +
//            "    Waehlen Sie eine Datei fuer den Import (EDBS, *.zip):<br/>" +
//            "    <input type=\"file\" name=\"datafile\" size=\"40\">" +
//            "  </p>" +
//            "  <div>" +
//            "    <input type=\"submit\" value=\"Senden\">" +
//            "  </div>" +
//            "</form>";
//    }

    
    public IContentFile createNew( String newName, InputStream in, Long length, String contentType )
            throws IOException, NotAuthorizedException, BadRequestException {
        return getProvider().createNew( this, newName, in );
    }


    public IContentFolder createFolder( String newName ) {
        return getProvider().createFolder( this, newName );
    }

    
    public void replaceContent( InputStream in, Long length )
    throws IOException, BadRequestException, NotAuthorizedException {
        throw new RuntimeException( "not yet implemented." );
    }


    public void moveTo( IPath dest, String newName )
    throws IOException, BadRequestException {
        getProvider().moveTo( this, dest, newName );
    }

    
    @Override
    public void delete() throws BadRequestException, NotAuthorizedException {
        try {
            getProvider().delete( this );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }

    
    /**
     * Upload a new file via the browser interface created in
     * {@link #getDescription(String)}.
     */
    public String processForm( Map<String,String> params, Map<String,FileItem> files )
            throws IOException, NotAuthorizedException, BadRequestException {
        assert !files.isEmpty() : "No files, should we parse params?";
        for (FileItem item : files.values()) {
            createNew( item.getName(), item.getInputStream(), item.getSize(), "text/plain" );
        }
        return null;
    }
    
}
