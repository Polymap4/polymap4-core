/* 
 * polymap.org
 * Copyright 2009-2018, Polymap GmbH. All rights reserved.
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
package org.polymap.service.geoserver.spring;

import java.util.ArrayList;
import java.util.List;

import java.net.URI;

import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.AddFeaturesRequest;
import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
import org.polymap.core.data.feature.ModifyFeaturesRequest;
import org.polymap.core.data.feature.ModifyFeaturesResponse;
import org.polymap.core.data.feature.RemoveFeaturesRequest;
import org.polymap.core.data.pipeline.Consumes;
import org.polymap.core.data.pipeline.EndOfProcessing;
import org.polymap.core.data.pipeline.Param;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.Produces;

/**
 * This processor sets name and namespace of the {@link FeatureType}. It is
 * used by {@link GeoServerLoader}.
 * 
 * @author Falko Bräutigam
 */
public class FeatureRenameProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( FeatureRenameProcessor.class );

    public static final Param<Name>     NAME = new Param( "name", Name.class );
    
    private Name                        name;
    
    /** The target {@link FeatureType} build from src with the name set. */
    private volatile FeatureType        schema;

    /** The source schema from the upstream processor. */
    private FeatureType                 srcSchema;
    
    
    @Override
    public void init( PipelineProcessorSite site ) throws Exception {
        name = NAME.rawopt( site ).get();
    }


    @Produces( GetFeatureTypeRequest.class )
    public void featureTypeRequest( GetFeatureTypeRequest request, ProcessorContext context ) throws Exception {
        // first time ask the upstream processor for the src schema
        // and init target schema on response
        if (schema == null) {
            context.sendRequest( request );
        }
        else {
            context.sendResponse( new GetFeatureTypeResponse( schema ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
    }

    
    @Produces( GetFeatureTypeResponse.class )
    public void featureTypeResponse( GetFeatureTypeResponse response, ProcessorContext context ) throws Exception {
        assert this.srcSchema == null;
        this.srcSchema = response.getFeatureType();
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( (SimpleFeatureType)srcSchema );
        builder.setName( name );
        schema = builder.buildFeatureType();
        context.sendResponse( new GetFeatureTypeResponse( schema ) );
    }

    
    @Produces( GetFeaturesSizeRequest.class )
    public void featuresSizeRequest( GetFeaturesSizeRequest request, ProcessorContext context ) throws Exception {
        Query transformed = transformQuery( request.getQuery(), context );
        context.sendRequest( new GetFeaturesSizeRequest( transformed ) );
    }
    

    @Produces( GetBoundsRequest.class )
    public void boundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        Query transformed = transformQuery( request.query.get(), context );
        context.sendRequest( new GetBoundsRequest( transformed ) );
    }
    

    @Produces( GetFeaturesRequest.class )
    public void featuresRequest( GetFeaturesRequest request, ProcessorContext context ) throws Exception {
        Query transformed = transformQuery( request.getQuery(), context );
        context.sendRequest( new GetFeaturesRequest( transformed ) );
    }
    

    @Produces( GetFeaturesResponse.class )
    public void featuresResponse( GetFeaturesResponse response, ProcessorContext context ) throws Exception {
        assert schema != null : "Target schema is not yet initialized. Call getSchema() first.";
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder( (SimpleFeatureType)schema );

        //log.debug( "       received features: " + chunk.count() );
        List<Feature> result = new ArrayList( response.count() );
        for (Feature feature : response) {
            for (PropertyDescriptor prop : schema.getDescriptors()) {
                Property featureProp = feature.getProperty( prop.getName() );
                // the feature may not contain the property if it was not requested
                if (featureProp != null) {
                    builder.set( prop.getName(), featureProp.getValue() );
                }
            }
            result.add( builder.buildFeature( feature.getIdentifier().getID() ) );
        }
        //log.debug( "       sending features: " + result.size() );
        context.sendResponse( new GetFeaturesResponse( result ) );
    }
    

    @Produces( AddFeaturesRequest.class )
    public void addFeatures( AddFeaturesRequest request, ProcessorContext context ) throws Exception {
        throw new UnsupportedOperationException( "Not yet implemented." );
    }
    

    @Produces( RemoveFeaturesRequest.class )
    public void removeFeatures( RemoveFeaturesRequest request, ProcessorContext context ) throws Exception {
        throw new UnsupportedOperationException( "Not yet implemented." );
    }
    

    @Produces( ModifyFeaturesRequest.class )
    public void modifyFeatures( ModifyFeaturesRequest request, ProcessorContext context ) throws Exception {
        throw new UnsupportedOperationException( "Not yet implemented." );
    }
    

    @Consumes( {EndOfProcessing.class, GetFeaturesSizeResponse.class, GetBoundsResponse.class, ModifyFeaturesResponse.class} )
    @Produces( {EndOfProcessing.class, GetFeaturesSizeResponse.class, GetBoundsResponse.class, ModifyFeaturesResponse.class} )
    public void forwardResponse( ProcessorResponse response, ProcessorContext context ) throws Exception {
        context.sendResponse( response );
    }

    
    protected Query transformQuery( Query query, ProcessorContext context ) throws Exception {
        assert schema != null : "Source schema is not yet initialized. Call getSchema() first.";
        //FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

        // transform query
        Query result = new Query( query );
        result.setTypeName( srcSchema.getName().getLocalPart() );
        if (srcSchema.getName().getNamespaceURI() != null) {
            result.setNamespace( new URI( srcSchema.getName().getNamespaceURI() ) );
        }
        
        // no attribute mapping
        result.setPropertyNames( query.getPropertyNames() );
        result.setFilter( query.getFilter() );
        return result;
    }
    
}
