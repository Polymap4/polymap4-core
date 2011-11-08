/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH. All rights reserved.
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

import java.util.Properties;
import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetLayerTypesRequest;
import org.polymap.core.data.image.GetLayerTypesResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.runtime.Timer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
class ImageCacheProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( ImageCacheProcessor.class );


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
    
    private String               layerId;
    
    
    public void init( Properties props ) {
        ILayer layer = (ILayer)props.get( "layer" );
        layerId = layer.id();
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

    
    protected void getMapRequest( GetMapRequest request, ProcessorContext context )
    throws Exception {
        
        Timer timer = new Timer();
        CachedTile cachedTile = Cache304.instance().get( request, context.getLayers() );
        log.debug( "query time: " + timer.elapsedTime() + "ms" );
        
        if (cachedTile != null) {
            // check If-Modified-Since
            long modifiedSince = request.getIfModifiedSince();
            long lastModified = cachedTile.lastModified.get();
            if (modifiedSince > 0 
                    && lastModified > modifiedSince) {
                log.debug( "### Cache: 304! :)" );
                context.sendResponse( EncodedImageResponse.NOT_MODIFIED );
                context.sendResponse( ProcessorResponse.EOP );
            }
            // in cache but modified
            else {
                byte[] data = cachedTile.data.get();
                log.debug( "### Cache: Hit. (" + data.length + " bytes)" );
                EncodedImageResponse response = new EncodedImageResponse( data, data.length );
                response.setLastModified( cachedTile.lastModified.get() );
                context.sendResponse( response );
                context.sendResponse( ProcessorResponse.EOP );
            }
        }
        
        // not in cache -> send request down the pipeline 
        else {
            log.debug( "### Cache: Miss. (...)" );
            ByteArrayOutputStream cacheBuf = new ByteArrayOutputStream( 128*1024 );
            context.put( "cacheBuf", cacheBuf );
            context.put( "request", request );
            context.sendRequest( request );
        }
    }


    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
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
            GetMapRequest request = (GetMapRequest)context.get( "request" );
            Cache304.instance().put( request, context.getLayers(), cacheBuf.toByteArray() );
            
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
        
}
