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
package org.polymap.core.data.image.cache304;

import java.util.concurrent.atomic.AtomicLong;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.RecordModel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CachedTile
        extends RecordModel {

    private static Log log = LogFactory.getLog( CachedTile.class );
    
    public static final CachedTile TYPE = type( CachedTile.class );

    private static AtomicLong   filenameCount = new AtomicLong( System.currentTimeMillis() );
    
    protected File              basedir;
    
    /**
     * Ctor for RecordModel.
     */
    public CachedTile( IRecordState record ) {
        super( record );
    }

    /**
     * Ctor to access tile {@link #data}.
     */
    public CachedTile( IRecordState record, File basedir ) {
        super( record );
        this.basedir = basedir;
    }

    public Property<Long>       created = new Property<Long>( "created" );
    
    public Property<Long>       lastModified = new Property<Long>( "lastModified" );
    
    public Property<Long>       lastAccessed = new Property<Long>( "lastAccessed" );
    
    public Property<Long>       expires = new Property<Long>( "expires" );
    
    public Property<Integer>    width = new Property<Integer>( "width" );
    
    public Property<Integer>    height = new Property<Integer>( "height" );
    
    public Property<String>     style = new Property<String>( "style" );
    
    public Property<String>     layerId = new Property<String>( "layerid" );
    
    public Property<Double>     minx = new Property<Double>( "minx" );
    
    public Property<Double>     miny = new Property<Double>( "miny" );
    
    public Property<Double>     maxx = new Property<Double>( "maxx" );
    
    public Property<Double>     maxy = new Property<Double>( "maxy" );
    
    public Property<Integer>    filesize = new Property<Integer>( "filesize" );
    
    public Property<String>     filename = new Property<String>( "filename" );
    
    public Property<byte[]>     data = new Property<byte[]>( "data" ) {

        @Override
        public byte[] get() {
            InputStream in = null;
            try {
                RandomAccessFile raf = new RandomAccessFile( new File( basedir, filename.get() ), "r" );
                // assuming that File#length ist faster than mem copy in ByteArrayOutputStream(?)
                byte[] buf = new byte[(int)raf.length()];
                raf.readFully( buf );
                return buf;
            }
            catch (Exception e) {
                IOUtils.closeQuietly( in );
                throw new RuntimeException( e );
            }
        }
        
        @Override
        public RecordModel put( byte[] value ) {
            // delete file
            if (value == null) {
                if (!new File( basedir, filename.get() ).delete()) {
                    throw new RuntimeException( "Unable to delete file: " + filename.get() );
                }
                filesize.put( 0 );
            }
            else {
                OutputStream out = null;
                try {
                    if (filename.get() == null) {
                        filename.put( String.valueOf( filenameCount.getAndIncrement() ) );
                    }
                    out = new FileOutputStream( new File( basedir, filename.get() ) );
                    IOUtils.copy( new ByteArrayInputStream( value ), out );
                    filesize.put( value.length );
                }
                catch (Exception e) {
                    IOUtils.closeQuietly( out );
                    throw new RuntimeException( e );
                }
            }
            return CachedTile.this;
        }

        @Override
        public RecordModel add( byte[] value ) {
            throw new RuntimeException( "not supported." );
        }
    };
    
}
