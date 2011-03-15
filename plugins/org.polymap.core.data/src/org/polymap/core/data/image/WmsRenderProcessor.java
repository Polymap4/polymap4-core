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

package org.polymap.core.data.image;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wms.WMSServiceImpl;

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
 *         <li>19.10.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class WmsRenderProcessor
        implements ITerminalPipelineProcessor {

    private static final Log log = LogFactory.getLog( WmsRenderProcessor.class );

    private static final ReferencedEnvelope NILL_BOX = 
            new ReferencedEnvelope( 0, 0, 0, 0, DefaultGeographicCRS.WGS84 );
    
    private final static ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {},
            new Class[] {},
            new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    public static boolean isCompatible( IService service ) {
        return service instanceof WMSServiceImpl;
    }
    

    // instance *******************************************

    public void init( Properties props ) {
    }


    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        // GetMapRequest
        if (r instanceof GetMapRequest) {
            getMap( (GetMapRequest)r, context );
        }
        // GetLegendGraphicRequest
        else if (r instanceof GetLegendGraphicRequest) {
            getLegendGraphic( (GetLegendGraphicRequest)r, context );
        }
        // GetLayerTypes
        else if (r instanceof GetLayerTypesRequest) {
            getLayerTypes();
            List<LayerType> types = getLayerTypes();
            context.sendResponse( new GetLayerTypesResponse( types ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }

    
    protected void getLegendGraphic( GetLegendGraphicRequest r, ProcessorContext context ) {
        throw new RuntimeException( "not yet implemented." );
    }


    protected List<LayerType> getLayerTypes() {
        throw new RuntimeException( "not yet implemented." );
    }
    
    
    protected void getMap( GetMapRequest request, ProcessorContext context )
    throws Exception {
        int width = request.getWidth();
        int height = request.getHeight();
        BoundingBox bbox = request.getBoundingBox();
        log.debug( "bbox=" + bbox + ", imageSize=" + width + "x" + height );

        Set<ILayer> layers = context.getLayers();
        IService service = context.getService();
        
        // skip if no layers are visible
        if (layers.isEmpty()) {
            log.debug( "no layers." );
        }
        
        WebMapServer wms = service.resolve( WebMapServer.class, null );
        org.geotools.data.wms.request.GetMapRequest getMap = wms.createGetMapRequest();
        
        getMap.setFormat( request.getFormat() );
        getMap.setDimensions( width, height );
        getMap.setTransparent( true );
        Color color = request.getBgColor();
        if (color != null) {
            getMap.setBGColour( String.format( "#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue() ) );
        }
        //getMap.setBGColour( "0xFFFFFF" );
        

        //CRSEnvelope envelop = calculateRequestBBox( )
        // CRSEnvelope envelop = new CRSEnvelope( "EPSG:31468", 4621529.925,5657790.683333333,4623149.175,5659484.016666667 );
        BoundingBox requestBBox = bbox;
        getMap.setBBox( requestBBox.getMinX() + "," + requestBBox.getMinY() //$NON-NLS-1$
                + "," + requestBBox.getMaxX() //$NON-NLS-1$
                + "," + requestBBox.getMaxY() ); //$NON-NLS-1$
        // FIXME hack for LRA to support EPSG3857, see GeoServerWms
        // check to see if backend wms supports 900913 or not
        String srs = request.getCRS().equals( "EPSG:900913" )
                ? "EPSG:3857" : request.getCRS(); 
        getMap.setSRS( srs );
        
        //getMap.setBBox(  );
        
        //request.setSRS( "EPSG:4326" );
        //request.setBBox( "-131.13151509433965,46.60532747661736,-117.61620566037737,56.34191403281659" );
        // Note: you can look at the layer metadata to determine a layer's bounding box for a SRS

        
//        Map sortedLayersMap = MultiValueMap.decorate( new TreeMap() );
//        for (Iterator it=visibleLayers.referencedLayers(); it.hasNext();) {
//            Layer layer = (Layer)availableLayers.get( it.next() );
//            Integer layerOrder = (Integer)layerOrderMap.get( layer.getName() );
//            // reverse order; request.addLayer() works that way
//            Integer key = Integer.valueOf( Integer.MAX_VALUE - layerOrder.intValue() );
//            sortedLayersMap.put( key, layer );
//        }
        for (ILayer layer : layers) {
            IGeoResource geores = layer.getGeoResource();
            Layer wmsLayer = geores.resolve( org.geotools.data.ows.Layer.class, null );
            getMap.addLayer( wmsLayer );
            log.debug( "    request: layer: " + layer.getLabel() );
        }
        log.debug( "    WMS URL:" + getMap.getFinalURL() );
        
        InputStream in = null;
        try {
            long start = System.currentTimeMillis();
            GetMapResponse wmsResponse = wms.issueRequest( getMap );
            log.debug( "Got repsonse (" + (System.currentTimeMillis()-start) + "ms). providing data: " + wmsResponse.getContentType() );
            
            in = wmsResponse.getInputStream();
            int count = 0;
            byte[] buf = new byte[2048];
            for (int c=in.read( buf ); c!=-1; c=in.read( buf )) {
                context.sendResponse( new EncodedImageResponse( buf, c ) );
                buf = new byte[2048];
                //log.debug( "    --->data sent: " + c );
                count += c;
            }
            if (count == 0) {
                throw new IOException( "WMSResponse is empty." );
            }
            context.sendResponse( ProcessorResponse.EOP );
            log.debug( "...all data send." );
        }
//        catch (Exception e) {
//            throw new RuntimeException( e.getMessage(), e );
//        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    
    public void processResponse( ProcessorResponse reponse, ProcessorContext context )
            throws Exception {
        throw new IllegalStateException( "This is a terminal processor." );
    }

    
    /**
     * Using the viewport bounds and combined wms layer extents, determines an appropriate bounding
     * box by projecting the viewport into the request CRS, intersecting the bounds, and returning
     * the result.
     * 
     * @param wmsLayers all adjacent wms layers we are requesting
     * @param viewport map editor bounds and crs
     * @param requestCRS coordinate reference system supported by the server
     * @return the bbox to ask the server for
     * @throws MismatchedDimensionException
     * @throws TransformException
     * @throws FactoryException
     */
    public static ReferencedEnvelope calculateRequestBBox( List<Layer> wmsLayers,
            ReferencedEnvelope viewport, CoordinateReferenceSystem requestCRS )
            throws MismatchedDimensionException, TransformException, FactoryException {
        /* The bounds of all wms layers on this server combined */
        Envelope layersBBox = getLayersBoundingBox( requestCRS, wmsLayers );
        if (isEnvelopeNull( layersBBox )) {
            // the wms server has no bounds
            log.debug( "Zero width/height envelope: wmsLayers = " + layersBBox ); //$NON-NLS-1$
            layersBBox = null;
            // alternatively, we could impose a reprojected -180,180,-90,90
        }

        /* The viewport bounds projected to the request crs */
        ReferencedEnvelope reprojectedViewportBBox = viewport.transform( requestCRS, true );
        if (isEnvelopeNull( reprojectedViewportBBox )) {
            // viewport couldn't be reprojected
            log.debug( "Zero width/height envelope: reprojected viewport from " + viewport //$NON-NLS-1$
                    + " to " + requestCRS + " returned " + reprojectedViewportBBox ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // alternative for better accuracy: new
        // ReferencedEnvelope(JTS.transform(viewport, null,
        // CRS.findMathTransform(viewportCRS, crs, true), 4), crs);

        /* The intersection of the viewport and the combined wms layers */
        Envelope interestBBox;
        if (layersBBox == null) {
            interestBBox = reprojectedViewportBBox;
        }
        else {
            interestBBox = reprojectedViewportBBox.intersection( layersBBox );
        }
        if (isEnvelopeNull( interestBBox )) {
            // outside of bounds, do not draw
            log.debug( "Bounds of the data are outside the bounds of the viewscreen." ); //$NON-NLS-1$
            return NILL_BOX;
        }

        /* The bounds of the request we are going to make */
        ReferencedEnvelope requestBBox = new ReferencedEnvelope( interestBBox, requestCRS );
        return requestBBox;
    }


    public static Envelope getLayersBoundingBox( CoordinateReferenceSystem crs, List<Layer> layers ) {
        Envelope envelope = null;

        for (Layer layer : layers) {

            GeneralEnvelope temp = layer.getEnvelope( crs );

            if (temp != null) {
                Envelope jtsTemp = new Envelope( temp.getMinimum( 0 ), temp.getMaximum( 0 ), temp
                        .getMinimum( 1 ), temp.getMaximum( 1 ) );
                if (envelope == null) {
                    envelope = jtsTemp;
                }
                else {
                    envelope.expandToInclude( jtsTemp );
                }
            }
        }
        return envelope;
    }

    
    private static boolean isEnvelopeNull( Envelope bbox ) {
        if (bbox.getWidth() <= 0 || bbox.getHeight() <= 0) {
            return true;
        }
        return false;
    }

}
