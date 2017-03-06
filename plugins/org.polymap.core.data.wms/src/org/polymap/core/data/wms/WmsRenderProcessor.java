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

import java.util.List;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.Request;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.AbstractGetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;

import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.image.EncodedImageProducer;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.TerminalPipelineProcessor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WmsRenderProcessor
        implements TerminalPipelineProcessor, EncodedImageProducer {

    private static final Log log = LogFactory.getLog( WmsRenderProcessor.class );

    private static final ReferencedEnvelope NILL_BOX = 
            new ReferencedEnvelope( 0, 0, 0, 0, DefaultGeographicCRS.WGS84 );

    private WebMapServer            wms;
    
    private String                  layerName;

    private Layer                   layer;

    private PipelineProcessorSite   site;
    

    // instance *******************************************

    @Override
    public void init( @SuppressWarnings("hiding") PipelineProcessorSite site ) throws Exception {
        this.site = site;
        wms = (WebMapServer)site.dsd.get().service.get();
        layerName = site.dsd.get().resourceName.get();
        
//        layer = new Layer( layerName );
//        layer.setName( layerName );

//        log.info( "Layers" + wms.getCapabilities().getLayerList().stream().map( l -> l.getName() ).collect( Collectors.toList() ) );
        layer = wms.getCapabilities().getLayerList().stream()
                .filter( l -> layerName.equals( l.getName() ) ).findFirst()
                .orElseThrow( () -> new RuntimeException( "No layer found for name: " + layerName ) );
    }


    @Override
    public boolean isCompatible( DataSourceDescription dsd ) {
        return dsd.service.get() instanceof WebMapServer;
    }
    

    public WebMapServer getWms() {
        return wms;
    }
    
    public Layer getWmsLayer() {
        return layer;
    }


    @Override
    public void getLegendGraphicRequest( GetLegendGraphicRequest request, ProcessorContext context ) throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void getBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        List<CRSEnvelope> bboxes = layer.getLayerBoundingBoxes();
//        log.info( "BBOXES: " + bboxes );
        ReferencedEnvelope result = new ReferencedEnvelope( FluentIterable.from( bboxes ).first().get() );
        context.sendResponse( new GetBoundsResponse( result ) );
    }


    @Override
    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception {
        int width = request.getWidth();
        int height = request.getHeight();
        BoundingBox bbox = request.getBoundingBox();
        log.debug( "bbox=" + bbox + ", imageSize=" + width + "x" + height );

        org.geotools.data.wms.request.GetMapRequest getMap = wms.createGetMapRequest();
        
        getMap.setFormat( request.getFormat() );
        getMap.setDimensions( width, height );
        getMap.setTransparent( true );
        Color color = request.getBgColor();
        if (color != null) {
            getMap.setBGColour( String.format( "#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue() ) );
        }

        setBBox( getMap, bbox );
        getMap.setSRS( request.getCRS() );
        
        getMap.addLayer( layer );
//        log.info( "    WMS URL:" + getMap.getFinalURL() );
        
        InputStream in = null;
        try {
            long start = System.currentTimeMillis();
            GetMapResponse wmsResponse = wms.issueRequest( getMap );
            log.debug( "Got repsonse (" + (System.currentTimeMillis()-start) + "ms). providing data: " + wmsResponse.getContentType() );
            
            in = wmsResponse.getInputStream();
            int count = 0;
            byte[] buf = new byte[4*1024];
            for (int c=in.read( buf ); c!=-1; c=in.read( buf )) {
                context.sendResponse( new EncodedImageResponse( buf, c ) );
                buf = new byte[8*1024];
                log.debug( "    ---> data sent: " + c );
                count += c;
            }
            if (count == 0) {
                throw new IOException( "WMSResponse is empty." );
            }
            context.sendResponse( ProcessorResponse.EOP );
            log.debug( "...all data send." );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }


    protected void setBBox( org.geotools.data.wms.request.GetMapRequest getMap, Envelope envelope ) {
        // code is from AbstractGetMapRequest
        String version = getMap.getProperties().getProperty( Request.VERSION );
        boolean forceXY = version == null || !version.startsWith( "1.3" );
        String srsName = CRS.toSRS( envelope.getCoordinateReferenceSystem() );
        
        CoordinateReferenceSystem crs = AbstractGetMapRequest.toServerCRS( srsName, forceXY );
        Envelope bbox = null;
        try {
            bbox = CRS.transform( envelope, crs );
        } 
        catch (TransformException e) {
            bbox = envelope;
        }
        // FIXME
        String s = srsName.contains( "31468" ) && version.startsWith( "1.3" )
                ? Joiner.on( ',' ).join( bbox.getMinimum(1), bbox.getMinimum(0), bbox.getMaximum(1), bbox.getMaximum(0) )
                : Joiner.on( ',' ).join( bbox.getMinimum(0), bbox.getMinimum(1), bbox.getMaximum(0), bbox.getMaximum(1) );
        //log.info( "Requested BBOX: " + s );
        getMap.setBBox( s );
    }


//    /**
//     * Using the viewport bounds and combined wms layer extents, determines an appropriate bounding
//     * box by projecting the viewport into the request CRS, intersecting the bounds, and returning
//     * the result.
//     * 
//     * @param wmsLayers all adjacent wms layers we are requesting
//     * @param viewport map editor bounds and crs
//     * @param requestCRS coordinate reference system supported by the server
//     * @return the bbox to ask the server for
//     * @throws MismatchedDimensionException
//     * @throws TransformException
//     * @throws FactoryException
//     */
//    public static ReferencedEnvelope calculateRequestBBox( List<Layer> wmsLayers,
//            ReferencedEnvelope viewport, CoordinateReferenceSystem requestCRS )
//            throws MismatchedDimensionException, TransformException, FactoryException {
//        /* The bounds of all wms layers on this server combined */
//        Envelope layersBBox = getLayersBoundingBox( requestCRS, wmsLayers );
//        if (isEnvelopeNull( layersBBox )) {
//            // the wms server has no bounds
//            log.debug( "Zero width/height envelope: wmsLayers = " + layersBBox ); //$NON-NLS-1$
//            layersBBox = null;
//            // alternatively, we could impose a reprojected -180,180,-90,90
//        }
//
//        /* The viewport bounds projected to the request crs */
//        ReferencedEnvelope reprojectedViewportBBox = viewport.transform( requestCRS, true );
//        if (isEnvelopeNull( reprojectedViewportBBox )) {
//            // viewport couldn't be reprojected
//            log.debug( "Zero width/height envelope: reprojected viewport from " + viewport //$NON-NLS-1$
//                    + " to " + requestCRS + " returned " + reprojectedViewportBBox ); //$NON-NLS-1$ //$NON-NLS-2$
//        }
//        // alternative for better accuracy: new
//        // ReferencedEnvelope(JTS.transform(viewport, null,
//        // CRS.findMathTransform(viewportCRS, crs, true), 4), crs);
//
//        /* The intersection of the viewport and the combined wms layers */
//        Envelope interestBBox;
//        if (layersBBox == null) {
//            interestBBox = reprojectedViewportBBox;
//        }
//        else {
//            interestBBox = reprojectedViewportBBox.intersection( layersBBox );
//        }
//        if (isEnvelopeNull( interestBBox )) {
//            // outside of bounds, do not draw
//            log.debug( "Bounds of the data are outside the bounds of the viewscreen." ); //$NON-NLS-1$
//            return NILL_BOX;
//        }
//
//        /* The bounds of the request we are going to make */
//        ReferencedEnvelope requestBBox = new ReferencedEnvelope( interestBBox, requestCRS );
//        return requestBBox;
//    }
//
//
//    public static Envelope getLayersBoundingBox( CoordinateReferenceSystem crs, List<Layer> layers ) {
//        Envelope envelope = null;
//
//        for (Layer layer : layers) {
//
//            GeneralEnvelope temp = layer.getEnvelope( crs );
//
//            if (temp != null) {
//                Envelope jtsTemp = new Envelope( temp.getMinimum( 0 ), temp.getMaximum( 0 ), temp
//                        .getMinimum( 1 ), temp.getMaximum( 1 ) );
//                if (envelope == null) {
//                    envelope = jtsTemp;
//                }
//                else {
//                    envelope.expandToInclude( jtsTemp );
//                }
//            }
//        }
//        return envelope;
//    }
//
//    
//    protected static boolean isEnvelopeNull( Envelope bbox ) {
//        if (bbox.getWidth() <= 0 || bbox.getHeight() <= 0) {
//            return true;
//        }
//        return false;
//    }

    
    // test ***********************************************
    
    public static void main( String[] args ) throws Exception {
        CoordinateReferenceSystem epsg31468 = CRS.decode( "EPSG:31468" );     
        CoordinateReferenceSystem epsg25833 = CRS.decode( "EPSG:25833" );
        System.out.println( "" + epsg31468 );
        System.out.println( "" + epsg25833 );
    }
    
}
