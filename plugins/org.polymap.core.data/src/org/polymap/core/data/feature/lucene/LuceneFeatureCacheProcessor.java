/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag.
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
package org.polymap.core.data.feature.lucene;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.AddFeaturesRequest;
import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
import org.polymap.core.data.feature.ModifyFeaturesRequest;
import org.polymap.core.data.feature.RemoveFeaturesRequest;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.LayerUseCase;

/**
 * Feature cache backed by a {@link LuceneCache}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class LuceneFeatureCacheProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( LuceneFeatureCacheProcessor.class );

    public static final int                 DEFAULT_CHUNK_SIZE = DataSourceProcessor.DEFAULT_CHUNK_SIZE;

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
    
    private LuceneCache         cache;
    
    private FeatureType         schema;
    
    
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
            
            if (cache == null) {
                this.cache = LuceneCache.aquire( context.getLayers().iterator().next(), schema );
            }
            // empty cache
            if (cache.isEmpty()) {
                context.put( "query", request.getQuery() );
                
                GetFeaturesRequest all = new GetFeaturesRequest( new DefaultQuery( schema.getName().getLocalPart() ) );
                context.sendRequest( all );
            }
            // not supported filter
            else if (!cache.supports( request.getQuery().getFilter() )) {
                log.warn( "Filter not supported: " + request.getQuery().getFilter() );
                context.sendRequest( request );
            }
            // cache
            else {
                List<Feature> chunk = new ArrayList( DEFAULT_CHUNK_SIZE );
                for (Feature feature : cache.getFeatures( request.getQuery() )) {
                    chunk.add( feature );
                    if (chunk.size() >= DEFAULT_CHUNK_SIZE) {
                        context.sendResponse( new GetFeaturesResponse( chunk ) );
                        chunk = new ArrayList( DEFAULT_CHUNK_SIZE );
                    }
                }
                if (!chunk.isEmpty()) {
                    context.sendResponse( new GetFeaturesResponse( chunk ) );
                }
                context.sendResponse( ProcessorResponse.EOP );
            }
        }
        
        // GetFeaturesSize
        else if (r instanceof GetFeaturesSizeRequest) {
            log.warn( "XXX GetFeatureSize: not implemented yet!" );
            context.sendResponse( new GetFeaturesSizeResponse( 0 ) );
            context.sendResponse( ProcessorResponse.EOP );
//            throw new RuntimeException( "Request type not supported: " + r );
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


    public void processResponse( ProcessorResponse r, ProcessorContext context )
            throws Exception {
        // features
        if (r instanceof GetFeaturesResponse) {
            GetFeaturesResponse response = (GetFeaturesResponse)r;
            log.debug( "received: " + response.count() + " features" );
            
            Query origQuery = (Query)context.get( "query" );
            List<Feature> putBuffer = (List<Feature>)context.get( "putBuffer" );
            if (origQuery != null) {
                if (putBuffer == null) {
                    putBuffer = new ArrayList<Feature>( 2000 );
                    context.put( "putBuffer", putBuffer );
                }
                putBuffer.addAll( response.getFeatures() );
                
                // FIXME
                if (putBuffer.size() >= 2000 || response.count() < DataSourceProcessor.DEFAULT_CHUNK_SIZE) {
                    // store in cache
                    cache.putFeatures( putBuffer );
                    putBuffer.clear();
                }

                // filter and response
                List<Feature> chunk = new ArrayList( response.count() );
                for (Feature feature : response) {
                    if (origQuery.getFilter().evaluate( feature )) {
                        chunk.add( feature );
                    }
                }
                log.debug( "    filtered against orig query: " + chunk.size() );
                context.sendResponse( new GetFeaturesResponse( chunk ) );
            }
            else {
                context.sendResponse( response );                
            }
        }
        
        // FeatureType
        else if (r instanceof GetFeatureTypeResponse) {
            GetFeatureTypeResponse response = (GetFeatureTypeResponse)r;
            this.schema = response.getFeatureType();
            
            context.sendResponse( response );
        }
        
        // other
        else {
            context.sendResponse( r );
        }
    }

}
