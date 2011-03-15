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

import java.util.Properties;
import java.util.Set;

import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.util.SoftValueHashMap;

import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ImageBufferProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( ImageBufferProcessor.class );

    public static final String              PROP_CACHESIZE = "cachesize";
    
    private static SoftValueHashMap<String,byte[]>   cache = new SoftValueHashMap( 50 );

    private static final ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class},
            new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    
    // instance *******************************************
    
    public void init( Properties props ) {
        log.info( "Cache size: " + props.getProperty( PROP_CACHESIZE ) );
    }


    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        // GetMapRequest
        if (r instanceof GetMapRequest) {
            getMapRequest( (GetMapRequest)r, context );
        }
        // GetLegendGraphicRequest
        else if (r instanceof GetLegendGraphicRequest) {
            context.sendRequest( r );
        }
        // GetLayerTypes
        else if (r instanceof GetLayerTypesRequest) {
            context.sendRequest( r );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }

    
    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        String cacheKey = (String)context.get( "cacheKey" );
        ByteArrayOutputStream cacheBuf = (ByteArrayOutputStream)context.get( "cacheBuf" ); 
        
        // EncodedImageResponse
        if (r instanceof EncodedImageResponse) {
            EncodedImageResponse response = (EncodedImageResponse)r;
            context.sendResponse( response );
            cacheBuf.write( response.getChunk(), 0, response.getChunkSize() );
            //log.debug( "    --->data sent: " + response.getChunkSize() );
        }
        // EOP
        else if (r == ProcessorResponse.EOP) {
            synchronized (cache) {
                log.debug( "### Cache: total= " + cache.size() + " -- put " + cacheBuf.size() + " -- cacheKey=" + cacheKey );
                cache.put( cacheKey, cacheBuf.toByteArray() );
            }
            context.sendResponse( ProcessorResponse.EOP );
            log.debug( "...all data sent." );
        }
        // GetLayerTypesResponse
        else if (r instanceof GetLayerTypesResponse) {
            context.sendResponse( r );
        }
        else {
            throw new IllegalStateException( "Unhandled response type: " + r );
        }
    }
    
    
    protected void getMapRequest( GetMapRequest request, ProcessorContext context )
    throws Exception {
        String cacheKey = generateCacheKey( request, context.getLayers() );
        context.put( "cacheKey", cacheKey );
        byte[] cachedData = cache.get( cacheKey );

        log.debug( "### Cache: " + (cachedData != null ? cachedData.length : -1) + " -- cacheKey=" + cacheKey );
        // in cache -> send response
        if (cachedData != null) {
            context.sendResponse( new EncodedImageResponse( cachedData, cachedData.length ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // not in cache -> send request down the pipeline 
        else {
            ByteArrayOutputStream cacheBuf = new ByteArrayOutputStream( 64*1024 );
            context.put( "cacheBuf", cacheBuf );
            context.sendRequest( request );
        }
    }
        
    
    protected String generateCacheKey( GetMapRequest request, Set<ILayer> layers ) {
        StringBuffer result = new StringBuffer( 1024 );
        for (ILayer layer : layers) {
            result.append( layer.id() ).append( "_" );
        }
        result.append( request.getCRS() ).append( "_" );
        result.append( request.getWidth() ).append( "x" ).append( request.getWidth() );
        result.append( request.getBoundingBox().toString() );
        return result.toString();
    }
    
}
