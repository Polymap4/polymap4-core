/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.service.geoserver.spring;

import java.io.IOException;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.impl.WMSStoreInfoImpl;
import org.geotools.data.FeatureStore;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.polymap.core.data.image.ImageProducer;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineBuilderException;
import org.polymap.core.data.wms.WmsRenderProcessor;
import org.polymap.core.project.ILayer;

import org.polymap.service.geoserver.GeoServerServlet;

/**
 * Upstream WMS.
 * <p/>
 * Started to support WMS and raster.
 *
 * @author Falko Bräutigam
 */
public class P4ImageStoreInfo
        extends WMSStoreInfoImpl
        implements WMSStoreInfo {

    private static final Log log = LogFactory.getLog( P4ImageStoreInfo.class );
    
    /**
     * Returns a newly created {@link P4DataStoreInfo}, or null if the layer is not
     * connected to a {@link FeatureStore}.
     * 
     * @throws Exception 
     */
    public static P4ImageStoreInfo canHandle( Catalog catalog, ILayer layer ) throws Exception {
        try {
            GeoServerServlet server = GeoServerServlet.instance.get();
            Pipeline pipeline = server.getOrCreatePipeline( layer, ImageProducer.class );
            if (pipeline.length() == 0 || !(pipeline.getLast().processor() instanceof WmsRenderProcessor)) {
                throw new PipelineBuilderException( "No ImageProducer pipeline found : " + layer.label.get() );
            }
            return new P4ImageStoreInfo( catalog, layer, pipeline );
        }
        catch (PipelineBuilderException e) {
            return null;
        }
    }

    // instance *******************************************

    private ILayer                      layer;
    
    private Pipeline                    pipeline;
    
    
    protected P4ImageStoreInfo( Catalog catalog, ILayer layer, Pipeline pipeline ) {
        super( catalog );
        assert layer != null;
        this.layer = layer;
        this.pipeline = pipeline;

        setId( (String)layer.id() );
        setName( layer.label.get() );
        setDescription( "ImageStore of ILayer: " + layer.label.get() );
        setType( "Image: WMS or Coverage" );
        setEnabled( true );
        log.debug( "    loaded: " + this );
    }

    
    @Override
    public WebMapServer getWebMapServer( ProgressListener listener ) throws IOException {
        try {
            WmsRenderProcessor proc = (WmsRenderProcessor)pipeline.getLast().processor();
            return new WebMapServer( proc.getWms().getCapabilities() ) {
                /**
                 * This is used by GetMap to figure if two layers can be requested (merged)
                 * from same server; we don't want this in no case
                 */
                @Override
                public boolean equals( Object obj ) {
                    return false;
                }
            };
        }
        catch (Exception e) {
            throw Throwables.propagate( e );
        }
    }


    public Layer getWmsLayer() {
        try {
            WmsRenderProcessor proc = (WmsRenderProcessor)pipeline.getLast().processor();
            return proc.getWmsLayer();
        }
        catch (Exception e) {
            throw Throwables.propagate( e );
        }
    }


    public ILayer getLayer() {
        return layer;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }
    
}
