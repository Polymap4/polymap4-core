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
package org.polymap.service.geoserver.spring;

import org.opengis.feature.type.FeatureType;

/**
 * This processor sets name and namespace of the {@link FeatureType}. It is
 * used by {@link GeoServerLoader}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureRenameProcessor {
//        implements PipelineProcessor {
//
//    private static final Log log = LogFactory.getLog( FeatureRenameProcessor.class );
//
//    private static final ProcessorSignature signature = new ProcessorSignature(
//            new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
//            new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
//            new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class},
//            new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
//            );
//
//    public static ProcessorSignature signature( LayerUseCase usecase ) {
//        return signature;
//    }
//
//    
//    // instance *******************************************
//    
//    private Name                        name;
//    
//    /** The target {@link FeatureType} build from src with the name set. */
//    private FeatureType                 schema;
//
//    /** The source schema from the upstream processor. */
//    private FeatureType                 srcSchema;
//    
//    
//    public FeatureRenameProcessor( Name name ) {
//        this.name = name;
//    }
//    
//
//    protected void initSchemas( FeatureType _srcSchema ) {
//        this.srcSchema = _srcSchema;
//        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
//        builder.init( (SimpleFeatureType)srcSchema );
//        builder.setName( name );
//        schema = builder.buildFeatureType();
//    }
//    
//    
//    public void init( Properties props ) {
//    }
//
//
//    public void processRequest( ProcessorRequest r, ProcessorContext context )
//            throws Exception {
//
//        // GetFeatureType
//        if (r instanceof GetFeatureTypeRequest) {
//            // first time ask the upstream processor for the src schema
//            // and init target schema on response
//            if (srcSchema == null) {
//                context.sendRequest( r );
//            }
//            else {
//                GetFeatureTypeResponse response = new GetFeatureTypeResponse( schema ); 
//                context.sendResponse( response );
//                context.sendResponse( ProcessorResponse.EOP );
//            }
//        }
//        // GetFeatures
//        else if (r instanceof GetFeaturesRequest) {
//            Query query = ((GetFeaturesRequest)r).getQuery();
//            Query transformed = transformQuery( query, context );
//            context.sendRequest( new GetFeaturesRequest( transformed ) );
//        }
//        // GetFeaturesSize
//        else if (r instanceof GetFeaturesSizeRequest) {
//            Query query = ((GetFeaturesSizeRequest)r).getQuery();
//            Query transformed = transformQuery( query, context );
//            context.sendRequest( new GetFeaturesSizeRequest( transformed ) );
//        }
//        else {
//            throw new IllegalArgumentException( "Unhandled request type: " + r );
//        }
//    }
//    
//
//    public void processResponse( ProcessorResponse r, ProcessorContext context )
//    throws Exception {
//        // GetFeatureType
//        if (r instanceof GetFeatureTypeResponse) {
//            initSchemas( ((GetFeatureTypeResponse)r).getFeatureType() );
//            context.sendResponse( new GetFeatureTypeResponse( schema ) );
//        }
//        // GetFeaturesSize
//        else if (r instanceof GetFeaturesSizeResponse) {
//            context.sendResponse( r );
//        }
//        // GetFeatures
//        else if (r instanceof GetFeaturesResponse) {
//            transformFeatures( (GetFeaturesResponse)r, context );
//        }
//        // EOP
//        else if (r == ProcessorResponse.EOP) {
//            context.sendResponse( r );
//        }
//        else {
//            throw new IllegalArgumentException( "Unhandled response type: " + r );
//        }
//    }
//
//    
//    protected Query transformQuery( Query query, ProcessorContext context ) 
//    throws Exception {
//        assert srcSchema != null : "Source schema is not yet initialized. Call getSchema() first.";
//        FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );
//
//        // transform query
//        DefaultQuery result = new DefaultQuery( query );
//        result.setTypeName( srcSchema.getName().getLocalPart() );
//        if (srcSchema.getName().getNamespaceURI() != null) {
//            result.setNamespace( new URI( srcSchema.getName().getNamespaceURI() ) );
//        }
//        
//        // no attribute mapping
//        result.setPropertyNames( query.getPropertyNames() );
//        result.setFilter( query.getFilter() );
//        return result;
//    }
//    
//    
//    protected void transformFeatures( GetFeaturesResponse chunk, ProcessorContext context )
//    throws Exception {
//        assert srcSchema != null : "Source schema is not yet initialized. Call getSchema() first.";
//        assert schema != null : "Target schema is not yet initialized. Call getSchema() first.";
//        
//        //log.debug( "       received features: " + chunk.count() );
//        try {
//            SimpleFeatureBuilder builder = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
//
//            List<Feature> result = new ArrayList( chunk.count() );
//            for (Feature feature : chunk) {
//                
//                for (PropertyDescriptor prop : schema.getDescriptors()) {
////                    // geometry
////                    if (prop.getType() instanceof GeometryType ) {
////                        builder.set( prop.getName(), feature.getDefaultGeometryProperty().getValue() );
////                    }
//
//                    Property featureProp = feature.getProperty( prop.getName() );
//                    // the feature may not contain the property if it was not requested
//                    if (featureProp != null) {
//                        builder.set( prop.getName(), featureProp.getValue() );
//                    }
//                }
//                result.add( builder.buildFeature( feature.getIdentifier().getID() ) );
//            }
//            //log.debug( "       sending features: " + result.size() );
//            context.sendResponse( new GetFeaturesResponse( result ) );
//        }
//        finally {
//        }
//    }
//
}
