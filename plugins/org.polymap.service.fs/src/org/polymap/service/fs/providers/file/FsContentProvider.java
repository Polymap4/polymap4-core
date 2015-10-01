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

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

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

    protected List<FsFolder>            roots;
    
    
    public FsContentProvider() {
//        // read config from workspace/config/FsContentProvider_config.json
//        File configFile = new File( Polymap.getConfigDir(), "org.polymap.service.fs.FsContentProvider.json" );
//        if (!configFile.exists()) {
//            // copy default file
//            InputStream resin = null;
//            OutputStream fileout = null;
//            try {
//                resin = FsPlugin.getDefault().getBundle().getResource( "resources/FsContentProvider.json" ).openStream();
//                fileout = new FileOutputStream( configFile );
//                IOUtils.copy( resin, fileout );
//                IOUtils.closeQuietly( resin );
//                IOUtils.closeQuietly( fileout );
//            }
//            catch (Exception e) {
//                throw new RuntimeException( e );
//            }
//        }
        
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
            @SuppressWarnings("unused")
            String role = params[1].trim();
            String alias = params.length > 2 ? params[2].trim() : dir.getName();
            
            roots.add( new FsFolder( alias, parentPath, this, dir ) );
        }
    }


    public List<? extends IContentNode> getChildren( IPath path ) {
        // check admin
        if (!SecurityUtils.isAdmin()) {
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
        assert newName != null;
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
        try {
            File dir = new File( folder.getDir(), FilenameUtils.normalize( newName ) );
            dir.mkdir();
            return new FsFolder( dir.getName(), folder.getPath(), this, dir );
        }
        finally {
            getSite().invalidateFolder( folder );
        }
    }

    
    public void delete( FsFolder folder ) throws IOException {
        Files.delete( folder.getDir().toPath() );
        getSite().invalidateFolder( getSite().getFolder( folder.getParentPath() ) );
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
