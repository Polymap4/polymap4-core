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
package org.polymap.core.data.feature.filter;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.polymap.core.data.DataPlugin;
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
 * This processors filters the requested features.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class FeatureFilterProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( FeatureFilterProcessor.class );

    private static final ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
            new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
            new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class},
            new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    static final FilterFactory      ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

    
    // instance *******************************************

    private FeatureType             sourceFeatureType;
    
    private Filter                  filter;


    public void init( Properties props ) {
        try {
            filter = Filter.INCLUDE;

            Object filterProp = props.get( "filter" );
            if (filterProp instanceof Filter) {
                filter = (Filter)filterProp;
            }
            else if (filterProp instanceof String) {
                filter = FeatureFilterProcessorConfig.decodeFilter( (String)filterProp );
            }
            else {
                throw new Exception( "Unknown filterProp type: " + filterProp );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        // GetFeatures
        if (r instanceof GetFeaturesRequest) {
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
            context.sendRequest( r );
        }
    }


    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        context.sendResponse( r );
    }


    protected Query transformQuery( Query query, ProcessorContext context )
    throws Exception {

//        if (sourceFeatureType == null) {
//            throw new RuntimeException( "The upstream FeatureType is not yet know. Call getSchema() on the PipelineFeatureSource first." );
//        }
        log.debug( "DataSource schema: " + sourceFeatureType );

        // new query
        DefaultQuery result = new DefaultQuery( query );

        // transform filter: avoid the enclosing AND(...) as this causes problems in Atlas
        // POI indexing for WFS resources
        if (query.getFilter().equals( Filter.INCLUDE )) {
            result.setFilter( filter );
        }
        else {
            result.setFilter( ff.and( filter, query.getFilter() ) );
        }
        return result;
    }

}
