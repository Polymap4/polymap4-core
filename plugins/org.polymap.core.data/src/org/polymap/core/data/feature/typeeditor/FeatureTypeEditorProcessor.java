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
package org.polymap.core.data.feature.typeeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.referencing.CRS;

import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * This processors allows to modify the {@link FeatureType} of the underlying
 * data source. Every target attribute can be mapped to a source attribute or
 * to a constant value. Mappings are specified via {@link AttributeMapping}s.
 * Mappings are configured by {@link FeatureTypeEditorProcessorConfig}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FeatureTypeEditorProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( FeatureTypeEditorProcessor.class );

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

    private FeatureTypeMapping          mappings;

    /** The target {@link FeatureType}. */
    private FeatureType                 featureType;

    private FeatureType                 sourceFeatureType;


    public void init( Properties props ) {
        try {
            // get mappings
            String serializedMappings = props.getProperty( "mappings", null );
            if (serializedMappings != null) {
                log.debug( "Initializing attribute mappings: ..." );
                mappings = new FeatureTypeMapping( serializedMappings );
            }
            // default mappings
            else {
                log.debug( "Initializing default mappings:" );
                mappings = new FeatureTypeMapping();
                AttributeMapping mapping = new AttributeMapping( "name", String.class, null, "ID", "_alles_wieder_gleich_" );
                mappings.put( mapping );
                log.debug( "    mapping: " + mapping );
                mapping = new AttributeMapping( "the_geom", MultiPolygon.class, CRS.decode( "EPSG:31468" ), null, null );
                mappings.put( mapping );
                log.debug( "    mapping: " + mapping );
            }

            // featureType
            String featureTypeName = props.getProperty( "featureTypeName", "_editedFeatureType_" );
            mappings.setFeatureTypeNameName( featureTypeName );
            featureType = mappings.newFeatureType();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }

    public FeatureTypeMapping getMappings() {
        return mappings;
    }
    
    public FeatureType getFeatureType() {
        return featureType;
    }
    
    public FeatureType getSourceFeatureType() {
        return sourceFeatureType;
    }


    public void processRequest( ProcessorRequest r, ProcessorContext context )
            throws Exception {
        // GetFeatureType
        if (r instanceof GetFeatureTypeRequest) {
            // we know the result already but we need the #sourceFeatureType too
            context.sendRequest( r );
        }
        // GetFeatures
        else if (r instanceof GetFeaturesRequest) {
            Query query = ((GetFeaturesRequest)r).getQuery();
            Query transformed = transformQuery( query, context );
            context.sendRequest( new GetFeaturesRequest( transformed ) );
        }
        // GetFeaturesSize
        else if (r instanceof GetFeaturesSizeRequest) {
            Query query = ((GetFeaturesSizeRequest)r).getQuery();
            Query transformed = transformQuery( query, context );
            context.sendRequest( new GetFeaturesSizeRequest( transformed ) );
        }
        else {
            throw new UnsupportedOperationException( Messages.get( "FeatureTypeEditorProcessor_unsupported", r.getClass().getSimpleName() ) );
        }
    }


    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        // GetFeatureType
        if (r instanceof GetFeatureTypeResponse) {
            sourceFeatureType = ((GetFeatureTypeResponse)r).getFeatureType();

            GetFeatureTypeResponse response = new GetFeatureTypeResponse( featureType );
            context.sendResponse( response );
        }
        // GetFeaturesSize
        else if (r instanceof GetFeaturesSizeResponse) {
            context.sendResponse( r );
        }
        // GetFeatures
        else if (r instanceof GetFeaturesResponse) {
            transformFeatures( (GetFeaturesResponse)r, context );
        }
        // EOP
        else if (r == ProcessorResponse.EOP) {
            context.sendResponse( r );
        }
        else {
            throw new IllegalArgumentException( "Unhandled response type: " + r );
        }
    }


    protected Query transformQuery( Query query, ProcessorContext context )
    throws Exception {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

        if (sourceFeatureType == null) {
            throw new RuntimeException( "The upstream FeatureType is not yet know. Call getSchema() on the PipelineFeatureSource first." );
        }
        log.debug( "DataSource schema: " + sourceFeatureType );

        // transform query
        DefaultQuery result = new DefaultQuery( query );
        result.setTypeName( sourceFeatureType.getName().getLocalPart() );
        if (sourceFeatureType.getName().getNamespaceURI() != null) {
            result.setNamespace( new URI( sourceFeatureType.getName().getNamespaceURI() ) );
        }

        // transform properties
        if (query.getPropertyNames() != null) {
            List<String> resultProps = new ArrayList();
            for (String prop : query.getPropertyNames()) {
                AttributeMapping mapping = mappings.get( prop );
                if (mapping == null) {
                    log.warn( "No mapping for: " + prop );
                    resultProps.add( prop );
                }
                else {
                    resultProps.add( mapping.sourceName );
                }
            }
            result.setPropertyNames( resultProps );
        }

        // transform filter
        FilterVisitor visitor = new MappingFilterVisitor();
        Filter filterOnSourceType = (Filter)query.getFilter().accept( visitor, null );
        result.setFilter( filterOnSourceType );

        return result;
    }


    protected void transformFeatures( GetFeaturesResponse chunk, ProcessorContext context )
    throws Exception {
        log.debug( "       received features: " + chunk.count() );
        try {
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder( (SimpleFeatureType)featureType );

            List<Feature> result = new ArrayList( chunk.count() );
            for (Feature feature : chunk) {
                result.add( transformFeature( (SimpleFeature)feature, builder ) );
            }
            log.debug( "       sending features: " + result.size() );
            context.sendResponse( new GetFeaturesResponse( result ) );
        }
        finally {
        }
    }

    
    public SimpleFeature transformFeature( SimpleFeature feature, SimpleFeatureBuilder builder )
    throws Exception {
        for (PropertyDescriptor prop : featureType.getDescriptors()) {

            AttributeMapping mapping = mappings.get( prop.getName().getLocalPart() );
            // geometry
            if (prop.getType() instanceof GeometryType ) {
                builder.set( prop.getName(), feature.getDefaultGeometryProperty().getValue() );
            }
            // mapping
            else if (mapping.sourceName != null) {
                Property featureProp = feature.getProperty( mapping.sourceName );
                // the feature may not contain the property if it was not requested
                if (featureProp != null) {
                    builder.set( prop.getName(), featureProp.getValue() );
                }
            }
            // constant value
            else if (mapping.constantValue != null) {
                builder.set( prop.getName(), mapping.constantValue );
            }
            //
            else {
                log.warn( "No value found in mapping for: " + prop.getName() );
            }
        }
        return builder.buildFeature( feature.getIdentifier().getID() );
    }


    /**
     * The returned object is a copy of the filter. The property names are
     * mapped according to the {@link FeatureTypeEditorProcessor#mappings}.
     */
    class MappingFilterVisitor
            extends DuplicatingFilterVisitor {

        public Object visit( BBOX bbox, Object data ) {
            log.debug( "*** Filter: target BBOX: " + bbox.getPropertyName() );
            AttributeMapping mapping = mappings.get( bbox.getPropertyName() );
            if (mapping == null) {
                log.warn( "No mapping for: " + bbox.getPropertyName() );
                return super.visit( bbox, data );
            }
            else {
                String propertyName = mapping.sourceName;
                double minx = bbox.getMinX();
                double miny = bbox.getMinY();
                double maxx = bbox.getMaxX();
                double maxy = bbox.getMaxY();
                String srs = bbox.getSRS();
                return getFactory( data ).bbox( propertyName, minx, miny, maxx, maxy, srs );
            }
        }

        public Object visit( PropertyName expression, Object data ) {
            log.debug( "*** Filter: target property: " + expression.getPropertyName() );

            AttributeMapping mapping = mappings.get( expression.getPropertyName() );
            if (mapping == null) {
                log.warn( "No mapping for: " + expression.getPropertyName() );
                return getFactory( data ).property( expression.getPropertyName() );
            }
            else {
                return getFactory( data ).property( mapping.sourceName );
            }
        }
    }

}
