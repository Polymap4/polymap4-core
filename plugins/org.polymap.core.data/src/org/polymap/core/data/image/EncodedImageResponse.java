/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data.image;

import org.polymap.core.data.pipeline.ProcessorResponse;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class EncodedImageResponse
        implements ProcessorResponse {

    /** 
     * This might be returned by a processor as pesponse the a {@link GetMapRequest} with
     * ifModifiedSince field set.
     */
    public static final EncodedImageResponse NOT_MODIFIED = new EncodedImageResponse( null, -1 );
    
    private byte[]          chunk;
    
    private int             chunkSize;
    
    private long            lastModified = -1;
    
    
    public EncodedImageResponse( byte[] chunk, int chunkSize ) {
        super();
        this.chunk = chunk;
        this.chunkSize = chunkSize;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public int getChunkSize() {
        return chunkSize;
    }
    
    public void setLastModified( long lastModified ) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }
    
}
