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

import java.util.Date;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.IContentDeletable;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentMoveable;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.NotAuthorizedException;
import org.polymap.service.fs.spi.Range;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FsFile
        extends DefaultContentNode
        implements IContentFile, IContentMoveable, IContentDeletable {

    private static Log log = LogFactory.getLog( FsFile.class );

    private String              contentType;

    private MappedByteBuffer    mappedBuffer;
    
    
    public FsFile( IPath parentPath, IContentProvider provider, File source ) {
        super( source.getName(), parentPath, provider, source );
    }

    public FsContentProvider getProvider() {
        return (FsContentProvider)super.getProvider();
    }

    protected File getFile() {
        return (File)getSource();
    }
    
    public Long getContentLength() {
        return getFile().length();
    }

    public String getContentType( String accepts ) {
        if (contentType == null) {
            try {
                String ext = FilenameUtils.getExtension( getFile().getName() );
                if ("css".equals( ext )) {
                    contentType = "text/css";
                }
                else {
                    contentType = getFile().toURI().toURL().openConnection().getContentType();
                }
            }
            catch (Exception e) {
            }
            contentType = contentType != null && !contentType.equals( "content/unknown" )
                    ? contentType : "text/plain";
        }
        return contentType;
    }

    public void sendContent( OutputStream out, Range range, Map<String,String> params, String acceptedContentType )
    throws IOException, BadRequestException {
        FileInputStream in = null;
        try {
            in = new FileInputStream( getFile() );
            IOUtils.copy( in, out );
        }
        finally {
            IOUtils.closeQuietly( in );
        }

//        long length = getContentLength();
//        
//        if (mappedBuffer == null) {
//            mappedBuffer = new RandomAccessFile( getFile(), "r" ).getChannel()
//                    .map( FileChannel.MapMode.READ_ONLY, 0, length );
//        }
//        
//        BufferedOutputStream bout = new BufferedOutputStream( out );
//        for (int c=0; c<length; c++) {
//            out.write( mappedBuffer.get() );
//        }
//        bout.flush();
//        mappedBuffer.rewind();
        
//        if (content != null) {
//            IOUtils.copy( new ByteArrayInputStream( content ), out );
//        }
//        else {
//            FileInputStream in = null;
//            try {
//                in = new FileInputStream( getFile() );
//                ByteArrayOutputStream bout = new ByteArrayOutputStream();
//                IOUtils.copy( in, new TeeOutputStream( out, bout ) );
//                content = bout.toByteArray();
//            }
//            finally {
//                IOUtils.closeQuietly( in );
//            }
//        }
    }

    public void moveTo( IPath dest, String newName )
    throws IOException, BadRequestException {
        getProvider().moveTo( this, dest, newName );
    }

    public void delete()
    throws BadRequestException, NotAuthorizedException {
        getFile().delete();
        getSite().invalidateFolder( getSite().getFolder( getParentPath() ) );
    }

    public Long getMaxAgeSeconds() {
        return 60l;
    }

    public Date getModifiedDate() {
        return new Date( getFile().lastModified() );
    }

}
