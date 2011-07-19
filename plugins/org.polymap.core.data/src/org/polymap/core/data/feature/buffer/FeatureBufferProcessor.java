/* 
 * polymap.org
 * Copyright 2010, 2011 Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.feature.buffer;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.data.DefaultQuery;
import org.opengis.feature.Feature;
import org.opengis.filter.identity.FeatureId;

import org.polymap.core.data.feature.AddFeaturesRequest;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
import org.polymap.core.data.feature.ModifyFeaturesRequest;
import org.polymap.core.data.feature.ModifyFeaturesResponse;
import org.polymap.core.data.feature.RemoveFeaturesRequest;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.LayerUseCase;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class FeatureBufferProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( FeatureBufferProcessor.class );


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
    protected IFeatureBuffer            buffer;
    
    
    public FeatureBufferProcessor( IFeatureBuffer buffer ) {
        this.buffer = buffer;
    }


    public void init( Properties props ) {
    }

    
    public void commitChanges() {
        throw new RuntimeException( "not yet implementd" );
        
//        // XXX start Tx
//        Collection<FeatureBufferState> bufferContent = buffer.content();
//        List<Feature> added = new ArrayList( bufferContent.size() );
//        List<Feature> removed = new ArrayList( bufferContent.size() );
//        List<Feature> modified = new ArrayList( bufferContent.size() );
//        
//        for (FeatureBufferState buffered : bufferContent) {
//            if (buffered.isAdded()) {
//                added.add();
//            }
//        }
//        
//        ...
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
            context.put( "request", r );
            GetFeaturesRequest request = (GetFeaturesRequest)r;
            // send added features first
            List<Feature> added = buffer.addedFeatures( request.getQuery().getFilter() );
            if (!added.isEmpty()) {
                context.sendResponse( new GetFeaturesResponse( added ) );
            }
            // tweak features in response
            context.sendRequest( r );
        }
        // GetFeaturesSize
        else if (r instanceof GetFeaturesSizeRequest) {
            context.put( "request", r );
            context.sendRequest( r );
        }
        // AddFeatures
        else if (r instanceof AddFeaturesRequest) {
            AddFeaturesRequest request = (AddFeaturesRequest)r;
            buffer.registerFeatures( request.getFeatures() );
            List<FeatureId> result = buffer.markAdded( request.getFeatures() );
            
            context.sendResponse( new ModifyFeaturesResponse( result ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // RemoveFeatures
        else if (r instanceof RemoveFeaturesRequest) {
            RemoveFeaturesRequest request = (RemoveFeaturesRequest)r;
            context.put( "state", "removing" );
            context.sendRequest( new GetFeaturesRequest( new DefaultQuery( null, request.getFilter() ) ) );
        }
        // ModifyFeatures
        else if (r instanceof ModifyFeaturesRequest) {
            ModifyFeaturesRequest request = (ModifyFeaturesRequest)r;
            
            // request other features ensuring that they are in buffer;
            // see LayerFeatureBufferManager for details
            context.put( "request", r );
            context.put( "state", "modifying" );
            context.sendRequest( new GetFeaturesRequest( new DefaultQuery( null, request.getFilter() ) ) );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }


    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        // GetFeaturesSize
        if (r instanceof GetFeaturesSizeResponse) {
            GetFeaturesSizeResponse response = (GetFeaturesSizeResponse)r;
            GetFeaturesSizeRequest request = (GetFeaturesSizeRequest)context.get( "request" );
            
            int difference = buffer.featureSizeDifference( request.getQuery() );
            int origSize  = response.getSize();
            context.sendResponse( new GetFeaturesSizeResponse( origSize + difference ) );
        }

        // GetFeatures
        else if (r instanceof GetFeaturesResponse) {
            GetFeaturesResponse response = (GetFeaturesResponse)r;
            Object state = context.get( "state" );
            
            // state: removing
            if ("removing".equals( state )) {
                buffer.markRemoved( response.getFeatures() );
            }
            // state: modifying
            else if ("modifying".equals( state )) {
                buffer.registerFeatures( response.getFeatures() );
            }
            // state: none
            else {
                GetFeaturesRequest request = (GetFeaturesRequest)context.get( "request" );
                Collection<Feature> features = buffer.blendFeatures( request.getQuery(), response.getFeatures() );
                context.sendResponse( new GetFeaturesResponse( (List<Feature>)features ) );
            }
        }
        
        // EOP
        else if (r == ProcessorResponse.EOP) {
            Object state = context.get( "state" );
            context.put( "state", null );
            
            if ("modifying".equals( state )) {
                ModifyFeaturesRequest request = (ModifyFeaturesRequest)context.get( "request" );
                List<FeatureId> ids = buffer.markModified( request.getFilter(), request.getType(), request.getValue() );
  
                context.sendResponse( new ModifyFeaturesResponse( ids ) );    
            }
            context.sendResponse( r );
        }
        else {
            context.sendResponse( r );
        }
    }

}
