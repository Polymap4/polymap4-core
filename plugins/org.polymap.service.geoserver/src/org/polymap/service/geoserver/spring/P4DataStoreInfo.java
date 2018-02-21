/* 
 * polymap.org
 * Copyright (C) 2010-2016-2018, Polymap GmbH. All rights reserved.
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

import java.util.Optional;

import java.io.IOException;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.PipelineDataStore;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;
import org.polymap.core.data.pipeline.ProcessorDescriptor;
import org.polymap.core.project.ILayer;

import org.polymap.service.geoserver.GeoServerServlet;
import org.polymap.service.geoserver.GeoServerUtils;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class P4DataStoreInfo
        extends DataStoreInfoImpl
        implements DataStoreInfo {

    private static final Log log = LogFactory.getLog( P4DataStoreInfo.class );
    
    /**
     * Returns a newly created {@link P4DataStoreInfo}, or null if the layer is not
     * connected to a {@link FeatureStore}.
     * 
     * @throws Exception 
     */
    public static P4DataStoreInfo canHandle( Catalog catalog, ILayer layer ) throws Exception {
        GeoServerServlet server = GeoServerServlet.instance.get();
        Optional<Pipeline> pipeline = server.getOrCreatePipeline( layer, FeaturesProducer.class );
        if (pipeline.isPresent()) {
            PipelineFeatureSource fs = new PipelineDataStore( pipeline.get() ).getFeatureSource();
            
            // check with P4FeatureTypeInfo
            Name name = new NameImpl( GeoServerUtils.defaultNsInfo.get().getName(), GeoServerUtils.simpleName( layer.label.get() ) );
            Params params = new Params();
            FeatureRenameProcessor.NAME.rawput( params, name );
            PipelineProcessorSite procSite = new PipelineProcessorSite( params );
            ProcessorDescriptor proc = new ProcessorDescriptor( FeatureRenameProcessor.class, params );
            proc.processor().init( procSite );
            fs.pipeline().addFirst( proc );

            return new P4DataStoreInfo( catalog, layer, fs );
        }
        else {
            return null;
        }
    }

    // instance *******************************************

    private ILayer                  layer;
    
    private FeatureSource           fs;
    
    
    protected P4DataStoreInfo( Catalog catalog, ILayer layer, PipelineFeatureSource fs ) {
        super( catalog );
        assert layer != null && fs != null;
        this.layer = layer;
        this.fs = fs;

        setId( (String)layer.id() );
        setName( layer.label.get() );
        setDescription( "DataStore of ILayer: " + layer.label.get() );
        setType( "PipelineDataStore" );
//        Map<String,Serializable> params = new HashMap<String,Serializable>();
//        // FIXME params.put( PipelineDataStoreFactory.PARAM_LAYER.key, layer );
//        setConnectionParameters( params );
        setEnabled( true );
        log.debug( "DataStore: " + this );
    }

    
    public ILayer getLayer() {
        return layer;
    }


    @Override
    public DataAccess<? extends FeatureType, ? extends Feature> getDataStore( ProgressListener listener )
            throws IOException {
        return fs.getDataStore();
    }


    /**
     * {@link PipelineFeatureSource} if this is a {@link Feature} layer, or
     * {@link ContentFeatureSource} and {@link MemoryDataStore} if layer is WMS.
     */
    public FeatureSource getFeatureSource() {
        return fs;
    }
    
}
