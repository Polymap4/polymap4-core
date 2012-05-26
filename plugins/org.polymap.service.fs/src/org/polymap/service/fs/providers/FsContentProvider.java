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
package org.polymap.service.fs.providers;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.NotAuthorizedException;

/**
 * Provides content of the filesystem.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FsContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( FsContentProvider.class );

    private List<FsFolder>          roots;
    
    
    public FsContentProvider() {
        String configs = System.getProperty( "FsContentProvider", "" );
        Path parentPath = new Path( "/" );
        roots = new ArrayList();
        for (String config : StringUtils.split( configs, ";" )) {
            String[] params = StringUtils.split( config, "," );
            
            if (params.length < 2 || params.length > 3) {
                throw new IllegalArgumentException( "Wrong config params: " + config );
            }
            
            File dir = new File( params[0].trim() );
            if (!dir.exists()) {
                throw new IllegalArgumentException( "Directory does not exists: " + dir );
            }
            String role = params[1].trim();
            String alias = params.length > 2 ? params[2].trim() : dir.getName();
            
            roots.add( new FsFolder( alias, parentPath, this, dir ) );
        }
    }


    public List<? extends IContentNode> getChildren( IPath path ) {
        // check admin
        if (!SecurityUtils.isAdmin( Polymap.instance().getPrincipals())) {
            return null;
        }
        
        // roots
        if (path.segmentCount() == 0) {
            return roots;
        }

        // folder
        IContentFolder parent = getSite().getFolder( path );
        if (parent instanceof FsFolder) {
            File[] files = ((FsFolder)parent).getDir().listFiles();
            List<IContentNode> result = new ArrayList( files.length );
            
            for (File f : files) {
                if (f.isFile()) { 
                    result.add( new FsFile( parent.getPath(), this, f ) );
                }
                else if (f.isDirectory()) {
                    result.add( new FsFolder( f.getName(), parent.getPath(), this, f ) );                    
                }
            
            }
            return result;
        }
        return null;
    }


    /**
     * Creates a new file for the given input.
     * @param fsFolder 
     * 
     * @param newName
     * @param in
     * @return Newly created {@link FsFile} reflecting the created file.
     */
    public IContentFile createNew( FsFolder folder, String newName, InputStream in )
    throws IOException, NotAuthorizedException, BadRequestException {
        OutputStream fileOut = null;
        try {
            File f = new File( folder.getDir(), newName );
            boolean isNewFile = !f.exists();
            fileOut = new FileOutputStream( f );
            IOUtils.copy( in, fileOut );

            // reload folder if file was created
            if (isNewFile) {
                getSite().invalidateFolder( folder );
            }

            return new FsFile( folder.getPath(), this, f );
        }
        finally {
            IOUtils.closeQuietly( fileOut );
        }
    }


    public IContentFolder createFolder( FsFolder folder, String newName ) {
        File dir = new File( folder.getDir(), FilenameUtils.normalize( newName ) );
        dir.mkdir();
        return new FsFolder( dir.getName(), folder.getPath(), this, dir );
    }

    
    public void moveTo( IContentNode src, IPath dest, String newName )
    throws BadRequestException, IOException {
        FsFolder destFolder = (FsFolder)getSite().getFolder( dest );
        File destFile = new File( destFolder.getDir(), newName );

        // file
        if (src instanceof FsFile) {
            FsFile srcFile = (FsFile)src;
            FileUtils.moveFile( srcFile.getFile(), destFile );
            getSite().invalidateFolder( getSite().getFolder( srcFile.getParentPath() ) );
        }
        // directory
        else if (src instanceof FsFolder) {
            FsFolder srcFolder = (FsFolder)src;
            FileUtils.moveDirectory( srcFolder.getDir(), destFile );
            getSite().invalidateFolder( getSite().getFolder( srcFolder.getParentPath() ) );
        }
        else {
            throw new BadRequestException( "Destination is not a valid folder to move to." );
        }
        
        getSite().invalidateFolder( destFolder );
    }
}
