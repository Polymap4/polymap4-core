/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.wms;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.polymap.core.data.image.GetLayerTypesRequest;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageProducer;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;

/**
 * Creates a buffered image out of the WMS content.
 *
 * @author Steffen Stundzig
 */
public class WmsImageRenderProcessor
        extends AbstractWmsRenderProcessor
        implements ImageProducer {

    private static final Log log = LogFactory.getLog( WmsRenderProcessor.class );


    /**
     * Only here to support the Override annotations
     */
    @Override
    public void getLegendGraphicRequest( GetLegendGraphicRequest request, ProcessorContext context ) throws Exception {
        super.getLegendGraphicRequest( request, context );
    }


    /**
     * Only here to support the Override annotations
     */
    @Override
    public void getLayerTypesRequest( GetLayerTypesRequest request, ProcessorContext context ) throws Exception {
        super.getLayerTypesRequest( request, context );
    }


    /**
     * Only here to support the Override annotations
     */
    @Override
    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception {
        super.getMapRequest( request, context );
    }


    @Override
    protected void handleResponse( InputStream in, ProcessorContext context ) throws Exception {
        BufferedImage image = ImageIO.read( in );
        context.sendResponse( new ImageResponse( image ) );
    }


    @Override
    protected void prepareRequest( org.geotools.data.wms.request.GetMapRequest getMap ) {
        getMap.setFormat( "image/png" );
    }
}
