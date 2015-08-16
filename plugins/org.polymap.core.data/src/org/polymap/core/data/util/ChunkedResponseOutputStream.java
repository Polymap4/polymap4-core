/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ChunkedResponseOutputStream
        extends OutputStream {

    private static final Log log = LogFactory.getLog( ChunkedResponseOutputStream.class );

    public static final int     DEFAULT_START_SIZE = 16*1024;
    
    private int                 bufStartSize = DEFAULT_START_SIZE;

    private byte[]              buf;

    private int                 buflen;
    
    private ProcessorContext    context;
    
    private int                 total;

    
    public ChunkedResponseOutputStream( ProcessorContext context ) {
        super();
        this.context = context;
        this.buf = new byte[bufStartSize];
    }

    public void write( int b )
            throws IOException {
        write( new byte[] {(byte)b}, 0, 1 );
    }

    public void write( byte[] b, int off, int len )
            throws IOException {
        //log.info( "    --->receive: " + len + ", buf: " + buf.length + " ,available: " + (buf.length-buflen) );
        if ((buflen + len) > bufStartSize) {
            flush();
            // buf to small for the received byte array?
            if (len > buf.length) {
                buf = new byte[len];
            }
        }
        System.arraycopy( b, off, buf, buflen, len );
        buflen += len;
    }

    public void flush()
            throws IOException {
        try {
            byte[] sendBuf = new byte[buflen];
            System.arraycopy( buf, 0, sendBuf, 0, buflen );
            context.sendResponse( createResponse( sendBuf, buflen ) );
            total += buflen;
            log.debug( "    --->data sent: " + buflen + " (total: " + total + ")" );
            buflen = 0;
        }
        catch (Exception e) {
            throw new IOException( e );
        }
    }

    /**
     * Returns to total byte count sent so far.
     */
    public int getTotalSent() {
        return total;    
    }


    /**
     * 
     * @param _buf The bytes to send. This array is already copied and can be
     *        used without any side effects.
     * @param _buflen The number of valid bytes in the <code>_buf</code>.
     * @return
     */
    protected abstract ProcessorResponse createResponse( byte[] _buf, int _buflen );
    
}
