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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentMoveable;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.Range;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FsFile
        extends DefaultContentNode
        implements IContentFile, IContentMoveable {

    private static Log log = LogFactory.getLog( FsFile.class );

    
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
        return "text/plain";
    }

    public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
    throws IOException, BadRequestException {
        FileInputStream in = null;
        try {
            in = new FileInputStream( getFile() );
            if (range != null) {
                in.skip( range.getStart() );
            }
            IOUtils.copy( in, out );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }

    public void moveTo( IPath dest, String newName )
    throws IOException, BadRequestException {
        getProvider().moveTo( this, dest, newName );
    }

    public Long getMaxAgeSeconds() {
        return 60l;
    }

    public Date getModifiedDate() {
        return new Date( getFile().lastModified() );
    }

}
