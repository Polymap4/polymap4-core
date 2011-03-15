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

package org.polymap.core.data.feature;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;

/**
 * {@link ProcessorRequest} and {@link ProcessorResponse} that handle PNG or JPEG
 * encoded byte chunks.
 *
 * @deprecated Use the new OGC-style request/response instead.
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>19.10.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class EncodedFeatures {
    
    public static final int     ENCODING_TYPE_GEOJSON = 1;
    
    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     *         <li>19.10.2009: created</li>
     */
    public static class GetTypeRequestResponse
            implements ProcessorRequest, ProcessorResponse {

        private int             encodingType;

        /**
         * 
         * @param encodingType * One of the <code>ENCONDING_TYPE_XXX</code>
         *        constants.
         */
        public GetTypeRequestResponse( int encodingType ) {
            super();
            this.encodingType = encodingType;
        }

        /**
         * One of the <code>ENCONDING_TYPE_XXX</code> constants.
         * @return The encoding that the processors provides.
         */
        public int getEncodingType() {
            return encodingType;
        }
        
    }
    
    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     *         <li>19.10.2009: created</li>
     */
    public static class GetDataRequest
            implements ProcessorRequest {
        
        private ReferencedEnvelope  bbox;

        
        public GetDataRequest( ReferencedEnvelope bbox ) {
            super();
            this.bbox = bbox;
        }

        public ReferencedEnvelope getBBox() {
            return bbox;
        }
        
    }

    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     *         <li>19.10.2009: created</li>
     */
    public static class GetDataResponse
            implements ProcessorResponse {
        
        private byte[]          chunk;
        
        private int             chunkSize;
        
        public GetDataResponse( byte[] chunk, int chunkSize ) {
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
        
    }

}
