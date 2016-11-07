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

import static org.polymap.core.data.util.Geometries.WGS84;
import static org.polymap.service.geoserver.GeoServerUtils.simpleName;

import java.io.IOException;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.impl.WMSLayerInfoImpl;
import org.geotools.data.ows.Layer;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineExecutor;
import org.polymap.core.project.ILayer;

import org.polymap.service.geoserver.GeoServerServlet;
import org.polymap.service.geoserver.GeoServerUtils;

/**
 * An image layer, WMS or GridCoverage.
 *
 * @author Falko Bräutigam
 */
public class P4ImageResourceInfo
        extends WMSLayerInfoImpl
        implements WMSLayerInfo {

    private static final Log log = LogFactory.getLog( P4ImageResourceInfo.class );
    
    private P4ImageStoreInfo        imInfo;

    
    public P4ImageResourceInfo( Catalog catalog, P4ImageStoreInfo imInfo ) {
        super( catalog );
        this.imInfo = imInfo;
        setStore( imInfo );

        ILayer layer = imInfo.getLayer();
        
        setNamespace( GeoServerUtils.defaultNsInfo.get() );
        setName( simpleName( layer.label.get() ) );
        setNativeName( simpleName( layer.label.get() ) );
        setTitle( layer.label.get() );
        // description and stuff is set in P4LayerInfo

        // bbox
        try {
            GeoServerServlet server = GeoServerServlet.instance.get();
            Pipeline pipeline = imInfo.getPipeline();
            PipelineExecutor executor = server.createPipelineExecutor();
            executor.execute( pipeline, new GetBoundsRequest(), (GetBoundsResponse r) -> {
                ReferencedEnvelope bbox = r.bounds.get();
                CoordinateReferenceSystem crs = bbox.getCoordinateReferenceSystem();
                setSRS( CRS.toSRS( crs ) );
                setNativeCRS( crs );
                setNativeBoundingBox( bbox );
                setProjectionPolicy( ProjectionPolicy.NONE );
                ReferencedEnvelope latlong = bbox.transform( WGS84.get(), true );
                setLatLonBoundingBox( latlong );
            });            
        }
        catch (Exception e) {
            log.warn( e );
            setLatLonBoundingBox( new ReferencedEnvelope( WGS84.get() ) );
        }
    }


    @Override
    public Layer getWMSLayer( ProgressListener listener ) throws IOException {
        return imInfo.getWmsLayer();
    }
    
}
