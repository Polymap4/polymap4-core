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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.PostgisService2;

import org.polymap.core.data.pipeline.ITerminalPipelineProcessor;
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
public class DataSourceProcessor
        implements ITerminalPipelineProcessor {

    private static final Log log = LogFactory.getLog( DataSourceProcessor.class );

    public static final int                 DEFAULT_CHUNK_SIZE = 100;
    

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        if (usecase == LayerUseCase.FEATURES_TRANSACTIONAL ) {
            return new ProcessorSignature(
                    new Class[] {ModifyFeaturesRequest.class, RemoveFeaturesRequest.class, AddFeaturesRequest.class, GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
                    new Class[] {},
                    new Class[] {},
                    new Class[] {ModifyFeaturesResponse.class, GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
            );
        }
        else {
            return new ProcessorSignature(
                    new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
                    new Class[] {},
                    new Class[] {},
                    new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
            );
        }
    }

    public static boolean isCompatible( IService service ) {
        // Postgres
        if (service instanceof PostgisService2) {
            return true;
        }
        // WFS, Memory, ...
        else {
            try {
                DataStore dataStore = service.resolve( DataStore.class, null );
                if (dataStore != null) {
                    return true;
                }
            }
            catch (IOException e) {
                log.warn( e.getMessage() );
            }
        }
        return false;
    }

    
    // instance *******************************************
    
    public void init( Properties props ) {
    }


    public void processRequest( ProcessorRequest r, ProcessorContext context )
            throws Exception {
        // resolve FeatureSource
        ILayer layer = context.getLayers().iterator().next();
        IGeoResource geores = layer.getGeoResource();
        log.debug( "        Request: " + r + ", geores= " + geores.getIdentifier() );

        // GetFeatureType
        if (r instanceof GetFeatureTypeRequest) {
            FeatureSource fs = geores.resolve( FeatureSource.class, null );
            FeatureType result = getFeatureType( fs );
            context.sendResponse( new GetFeatureTypeResponse( result ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // AddFeatures
        else if (r instanceof AddFeaturesRequest) {
            AddFeaturesRequest request = (AddFeaturesRequest)r;
            FeatureStore fs = geores.resolve( FeatureStore.class, null );
            List<FeatureId> result = addFeatures( (FeatureStore)fs, request.getFeatures() );
            context.sendResponse( new ModifyFeaturesResponse( result ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // RemoveFeatures
        else if (r instanceof RemoveFeaturesRequest) {
            RemoveFeaturesRequest request = (RemoveFeaturesRequest)r;
            FeatureStore fs = geores.resolve( FeatureStore.class, null );
            removeFeatures( (FeatureStore)fs, request.getFilter() );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // ModifyFeatures
        else if (r instanceof ModifyFeaturesRequest) {
            ModifyFeaturesRequest request = (ModifyFeaturesRequest)r;
            FeatureStore fs = geores.resolve( FeatureStore.class, null );
            modifyFeatures( (FeatureStore)fs, request.getType(), request.getValue(), request.getFilter() );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // GetFeatures
        else if (r instanceof GetFeaturesRequest) {
            GetFeaturesRequest request = (GetFeaturesRequest)r;
            FeatureSource fs = geores.resolve( FeatureSource.class, null );
            getFeatures( fs, request.getQuery(), context );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // GetFeaturesSize
        else if (r instanceof GetFeaturesSizeRequest) {
            GetFeaturesSizeRequest request = (GetFeaturesSizeRequest)r;
            FeatureSource fs = geores.resolve( FeatureSource.class, null );
            int result = getFeaturesSize( fs, request.getQuery() );
            context.sendResponse( new GetFeaturesSizeResponse( result ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }
    
    
    protected FeatureType getFeatureType( FeatureSource fs ) {
        return fs.getSchema();
    }
    

    protected int getFeaturesSize( FeatureSource fs, Query query )
    throws IOException {
        // features
        FeatureCollection fc = fs.getFeatures( query );
        int result = fc.size();
        log.debug( "            Features size: " + result );
        return result;
    }
    
    
    protected void getFeatures( FeatureSource fs, Query query, ProcessorContext context )
    throws Exception {
        // features
        // we cannot just cache this since the query specifies chunks instead
        // of the entire collection
        log.debug( "            Filter: " + query.getFilter() );
        FeatureCollection fc = fs.getFeatures( query );
        
        
        // execute should of set all the header information
        // including the lockID
        //
        // execute should also fail if all of the locks could not be aquired
//        List resultsList = featureCollection.getFeature();
        
        
        Iterator it = null;
        try {
            ArrayList<Feature> chunk = new ArrayList( DEFAULT_CHUNK_SIZE );
            for (it=fc.iterator(); it.hasNext(); ) {
                Feature feature = (Feature)it.next();
                //System.out.println( "              feature: " + feature );
                chunk.add( feature );
                if (chunk.size() >= DEFAULT_CHUNK_SIZE) {
                    chunk.trimToSize();
                    log.debug( "                sending chunk: " + chunk.size() );
                    context.sendResponse( new GetFeaturesResponse( chunk ) );
                    chunk = new ArrayList( DEFAULT_CHUNK_SIZE );
                }
            }
            if (!chunk.isEmpty()) {
                chunk.trimToSize();
                log.debug( "                sending chunk: " + chunk.size() );
                context.sendResponse( new GetFeaturesResponse( chunk ) );
            }
        }
        finally {
            if (it != null) {
                fc.close( it );
            }
        }
    }

    
    protected List<FeatureId> addFeatures( FeatureStore fs, Collection<Feature> features )
    throws IOException {
        log.debug( "            Features: " + features.size() );
        // XXX supports SimpleFeatureType only yet
        FeatureCollection<SimpleFeatureType, SimpleFeature> coll = 
                FeatureCollections.newCollection();
        coll.addAll( (Collection<? extends SimpleFeature>)features );
        return fs.addFeatures( coll );
    }

    
    protected void removeFeatures( FeatureStore fs, Filter filter )
    throws IOException {
        log.debug( "            Filter: " + filter );
        fs.removeFeatures( filter );
    }

    
    protected void modifyFeatures( FeatureStore fs, 
            AttributeDescriptor[] type, Object[] value, Filter filter )
            throws IOException {
        log.debug( "            Filter: " + filter );
        fs.modifyFeatures( type, value, filter );
    }

    
    public void processResponse( ProcessorResponse reponse, ProcessorContext context )
    throws Exception {
        throw new RuntimeException( "This is a terminal processor." );
    }

    
    /**** sample code from the old SourceFeaturesProcessor ****
     
    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        GetDataRequest request = (GetDataRequest)r;
        ReferencedEnvelope bbox = request.getBBox();

        // FeatureSource
        IGeoResource geores = layer.getGeoResource();
        FeatureSource fs = geores.resolve( FeatureSource.class, null );
        log.debug( "            FeatureSource: " + fs.getName() );

        //SimpleFeatureType schema = (SimpleFeatureType)fs.getSchema();
        //log.debug( "### Schema: type name: " + schema.getTypeName() );
        //GeometryDescriptor geom = fs.getSchema().getGeometryDescriptor();
        //log.debug( "    Geometry: name=" + geom.getLocalName() + ", type=" + geom.getType().getName() );
        //for (AttributeDescriptor attr : schema.getAttributeDescriptors()) {
        //    log.debug( "    Attribute: name=" + attr.getName() + ", type=" + attr.getType().getName() );
        //}

        // features
        FeatureCollection fc = null;
        if (bbox != null) {
            // transform bbox
            CoordinateReferenceSystem layerCRS = 
                geores.getInfo( new NullProgressMonitor() ).getCRS();
            if (layerCRS == null) {
                log.warn( "### No CRS found for layer. Using map CRS for layer CRS." );
                layerCRS = layer.getMap().getCRS();
            }
            bbox = bbox.transform( layerCRS, true );
            log.debug( "### bbox: " + bbox );

            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
            GeometryDescriptor geomDesc = fs.getSchema().getGeometryDescriptor();
            String geometryAttributeName = geomDesc.getLocalName();
            log.debug( "### geom attr: " + geometryAttributeName );

            // filter to select features that intersect with the bounding box
            Filter filter = ff.bbox( ff.property( geometryAttributeName ), bbox);
            fc = fs.getFeatures( filter );
        }
        else {
            fc = fs.getFeatures();
        }
        GetDataResponse response = new GetDataResponse( fc );
        log.debug( "            Features: " + fc.size() );

        //for (Iterator it=fs.getFeatures( Query.ALL ).iterator(); it.hasNext(); ) {
        //    Object feature = it.next();
        //    System.out.println( "              feature: " + feature );
        //}

        context.sendResponse( response );
        context.sendResponse( ProcessorResponse.EOP );
    }*/
    
}

