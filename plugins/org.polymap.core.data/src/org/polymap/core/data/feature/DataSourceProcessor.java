/*
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.collection.AdaptorFeatureCollection;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
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
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class DataSourceProcessor
        implements ITerminalPipelineProcessor {

    private static final Log log = LogFactory.getLog( DataSourceProcessor.class );

    public static final int                 DEFAULT_CHUNK_SIZE = 512;


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
        // FIXME Postgres does not resolve to a DataStore!? :( Anyway, isCompatible should
        // receive a IGeiResource instead of an IService
        if (service.getClass().getSimpleName().equals( "PostgisService2" ) ) {
            return true;
        }
        // WFS, Memory, ...
        else {
            try {
                return service.resolve( DataAccess.class, null ) != null;
            }
            catch (IOException e) {
                log.warn( e.getMessage() );
            }
        }
        return false;
    }


    // instance *******************************************

    private IGeoResource        geores;


    public void init( Properties props ) {
    }


    /**
     * Ignore the geores of the source {@link ILayer} from the processor config and use
     * the given geores instead.
     *
     * @param geores The geores to use.
     */
    public void setGeores( IGeoResource geores ) {
        this.geores = geores;
    }


    public void processRequest( ProcessorRequest r, ProcessorContext context )
            throws Exception {
        // find geores
        if (geores == null) {
            ILayer layer = context.getLayers().iterator().next();
            geores = layer.getGeoResource();
        }
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
            List<FeatureId> result = addFeatures( fs, request.getFeatures() );
            context.sendResponse( new ModifyFeaturesResponse( new FidSet( result ) ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // RemoveFeatures
        else if (r instanceof RemoveFeaturesRequest) {
            RemoveFeaturesRequest request = (RemoveFeaturesRequest)r;
            FeatureStore fs = geores.resolve( FeatureStore.class, null );
            removeFeatures( fs, request.getFilter() );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // ModifyFeatures
        else if (r instanceof ModifyFeaturesRequest) {
            ModifyFeaturesRequest request = (ModifyFeaturesRequest)r;
            FeatureStore fs = geores.resolve( FeatureStore.class, null );
            modifyFeatures( fs, request.getType(), request.getValue(), request.getFilter() );
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
        log.debug( "            Filter: " + query.getFilter() );
        FeatureCollection fc = fs.getFeatures( query );

        Iterator it = null;
        int currentChunkSize = 64;
        try {
            ArrayList<Feature> chunk = new ArrayList( currentChunkSize );
            for (it = fc.iterator(); it.hasNext(); ) {
                Feature feature = (Feature)it.next();

                chunk.add( feature );
                if (chunk.size() >= currentChunkSize) {
                    log.debug( "                sending chunk: " + chunk.size() );
                    context.sendResponse( new GetFeaturesResponse( chunk ) );
                    
                    currentChunkSize = Math.min( DEFAULT_CHUNK_SIZE, currentChunkSize * 2 );
                    chunk = new ArrayList( currentChunkSize );
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


    protected List<FeatureId> addFeatures( FeatureStore fs, final Collection<Feature> features )
    throws IOException {
        log.debug( "addFeatures(): Features: " + features.size() );
        // XXX supports SimpleFeatureType only
        // XXX transactions?
//        FeatureType schema = fs.getSchema();
//        for (Feature feature : features) {
//            // adopt schema and properties; I'm not sure if this is the proper place to
//            // do this; here I know the proper target schema and iterating over features
//            // is done anyway
//            if (!feature.getType().equals( schema )) {
//                log.debug( "addFeatures(): FeatureType does not match: " + feature.getType() );
//                SimpleFeatureBuilder builder = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
//
//                for (Property prop : feature.getProperties()) {
//                    PropertyDescriptor desc = schema.getDescriptor( prop.getName().getLocalPart() );
//                    if (desc != null) {
//                        builder.set( desc.getName(), prop.getValue() );
//                    }
//                    else {
//                        log.warn( "addFeatures(): No such property in target: " + prop.getName() );
//                    }
//                }
//                feature = builder.buildFeature( null );
//            }
//            coll.add( (SimpleFeature)feature );
//        }
////        coll.addAll( (Collection<? extends SimpleFeature>)features );

        FeatureCollection fc = new AdaptorFeatureCollection( "features", (SimpleFeatureType)fs.getSchema() ) {
            @Override
            protected void closeIterator( Iterator it ) {
            }
            @Override
            protected Iterator openIterator() {
                return features.iterator();
            }
            @Override
            public int size() {
                return features.size();
            }
        };

        return fs.addFeatures( fc );
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

}

