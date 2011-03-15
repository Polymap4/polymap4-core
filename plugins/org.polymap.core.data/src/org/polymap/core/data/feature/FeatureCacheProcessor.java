/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
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
 * $Id: $
 */
package org.polymap.core.data.feature;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.data.Query;

import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.LayerUseCase;

/**
 *
 * <p>
 * Using this processor for rendering pipelines is useful ONLY for tiled
 * rendering. Otherwise subsequent queries do not match and the cache
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FeatureCacheProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( FeatureCacheProcessor.class );


    private static final ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
            new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
            new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class},
            new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    
    // instance *******************************************

    /** 
     * The query/feature cache. Persistent for the lifetime of the pipeline
     * and shared by all threads using this pipeline.
     */
    protected FeatureCache          cache = new FeatureCache();
    
    
    public void init( Properties props ) {
    }


    public void processRequest( ProcessorRequest r, ProcessorContext context )
            throws Exception {
        log.debug( "    Request: " + r.getClass().getSimpleName() );

        // GetFeatureType
        if (r instanceof GetFeatureTypeRequest) {
            context.sendRequest( r );
        }
        // GetFeatures
        else if (r instanceof GetFeaturesRequest) {
            GetFeaturesRequest request = (GetFeaturesRequest)r;
            doGetFeaturesRequest( request, context );
        }
        // GetFeaturesSize
        else if (r instanceof GetFeaturesSizeRequest) {
            GetFeaturesSizeRequest request = (GetFeaturesSizeRequest)r;

            FeatureCache.Result cacheResult = cache.getFeatures( request.getQuery() );
            // cache has all fids and features
            if (cacheResult.isComplete) {
                context.sendResponse( new GetFeaturesSizeResponse( cacheResult.features.size() ) );
                context.sendResponse( ProcessorResponse.EOP );
            }
            else {
                // XXX fetch features instead of just the size
                context.sendRequest( request );
            }
        }
        // AddFeatures
        else if (r instanceof AddFeaturesRequest) {
            throw new RuntimeException( "Request type not supported: " + r );
        }
        // RemoveFeatures
        else if (r instanceof RemoveFeaturesRequest) {
            throw new RuntimeException( "Request type not supported: " + r );
        }
        // ModifyFeatures
        else if (r instanceof ModifyFeaturesRequest) {
            throw new RuntimeException( "Request type not supported: " + r );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }


    private void doGetFeaturesRequest( GetFeaturesRequest request, ProcessorContext context )
    throws Exception {
        FeatureCache.Result cacheResult = cache.getFeatures( request.getQuery() );
        
        // cache has all fids and features
        if (cacheResult.isComplete) {
            log.info( "    Cache HIT: " + cacheResult.features.size() + " features");
            context.sendResponse( new GetFeaturesResponse( cacheResult.features ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // XXX featch all fids/features; later this can be revised to fetch
        // just fids first, check features and then fetch just missing features
        else {
            log.info( "    Cache MISS: " );
            context.put( "query", request.getQuery() );
            context.sendRequest( request );
        }
    }


    public void processResponse( ProcessorResponse r, ProcessorContext context )
            throws Exception {
        if (r instanceof GetFeaturesResponse) {
            GetFeaturesResponse response = (GetFeaturesResponse)r;
            Query query = (Query)context.get( "query" );
            cache.putFeatures( query, response.getFeatures() );
            
            context.sendResponse( response );
        }
        else {
            context.sendResponse( r );
        }
    }

}
