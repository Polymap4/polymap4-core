/*
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
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

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.AdaptorFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.DataSourceDescriptor;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.TerminalPipelineProcessor;

/**
 * Connects a a backend {@link FeatureSource} to a {@link Pipeline}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DataSourceProcessor
        implements TerminalPipelineProcessor, FeaturesProducer {

    private static final Log log = LogFactory.getLog( DataSourceProcessor.class );

    /**
     * As of now the chunk size increazing for each chunk in order to deliver first
     * results quickly and minimize synchronization later. So this defines the the
     * <b>MAX_CHUNK_SIZE</b> now.
     */
    public static final int         DEFAULT_CHUNK_SIZE = 1024;


    // instance *******************************************

    private DataAccess              ds;
    
    private FeatureSource           fs;

    
    @Override
    public boolean isCompatible( DataSourceDescriptor dsd ) {
        return dsd.service.get() instanceof DataAccess;
    }


    @Override
    public void init( PipelineProcessorSite site ) throws Exception {
        if (fs == null) {
            ds = (DataAccess)site.dsd.get().service.get();
            String resName = site.dsd.get().resourceName.get();
            
            List<Name> names = ds.getNames();
            for (Name name : names) {
                if (name.getLocalPart().equals( resName )) {
                    if (fs != null) {
                        throw new UnsupportedOperationException( "Multiple layers have same local name: " + resName );
                    }
                    fs = ds.getFeatureSource( name );
                }
            }
            if (fs == null) {
                throw new IllegalStateException( "Resource not found: " + resName );
            }
        }
    }


    @Override
    public void setTransactionRequest( TransactionRequest request, ProcessorContext context ) throws Exception {
        ((FeatureStore)fs).setTransaction( request.tx.get() );
    }


    @Override
    public void modifyFeaturesRequest( ModifyFeaturesRequest request, ProcessorContext context ) throws Exception {
        log.debug( "            Filter: " + request.getFilter() );
        ((FeatureStore)fs).modifyFeatures( request.getType(), request.getValue(), request.getFilter() );
        context.sendResponse( ProcessorResponse.EOP );
    }


    @Override
    public void removeFeaturesRequest( RemoveFeaturesRequest request, ProcessorContext context ) throws Exception {
        ((FeatureStore)fs).removeFeatures( request.getFilter() );
        context.sendResponse( ProcessorResponse.EOP );
    }


    @Override
    public void addFeaturesRequest( AddFeaturesRequest request, ProcessorContext context ) throws Exception {
        Collection<Feature> features = request.getFeatures();
        log.debug( "addFeatures(): Features: " + features.size() );
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

        List<FeatureId> result = ((FeatureStore)fs).addFeatures( fc );
        context.sendResponse( new ModifyFeaturesResponse( new FidSet( result ) ) );
        context.sendResponse( ProcessorResponse.EOP );
    }


    @Override
    public void getFeatureTypeRequest( GetFeatureTypeRequest request, ProcessorContext context ) throws Exception {
        FeatureType result = fs.getSchema();
        context.sendResponse( new GetFeatureTypeResponse( result ) );
        context.sendResponse( ProcessorResponse.EOP );
    }


    @Override
    public void getFeatureSizeRequest( GetFeaturesSizeRequest request, ProcessorContext context ) throws Exception {
        FeatureCollection fc = fs.getFeatures( request.getQuery() );
        int result = fc.size();
        context.sendResponse( new GetFeaturesSizeResponse( result ) );
        context.sendResponse( ProcessorResponse.EOP );
    }


    @Override
    public void getFeatureBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        ReferencedEnvelope result = request.query.isPresent()
                ? fs.getBounds( request.query.get() )
                : fs.getBounds();
        context.sendResponse( new GetBoundsResponse( result ) );
    }


    @Override
    public void getFeatureRequest( GetFeaturesRequest request, ProcessorContext context ) throws Exception {
        Query query = request.getQuery();
        log.debug( "            Filter: " + query.getFilter() );
        FeatureCollection fc = fs.getFeatures( query );

        int currentChunkSize = 16; //DEFAULT_CHUNK_SIZE;
        ArrayList<Feature> chunk = new ArrayList( currentChunkSize );
        try (
            FeatureIterator it = fc.features();
        ){
            while (it.hasNext() ) {
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
        context.sendResponse( ProcessorResponse.EOP );
    }

}

