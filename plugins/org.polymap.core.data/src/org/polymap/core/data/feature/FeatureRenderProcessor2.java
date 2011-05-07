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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.simple.SimpleFeature;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.geotools.util.logging.Logging;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.PostgisService2;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.image.GetLayerTypesRequest;
import org.polymap.core.data.image.GetLayerTypesResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.DefaultPipelineIncubator;
import org.polymap.core.data.pipeline.ITerminalPipelineProcessor;
import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.style.geotools.DefaultStyles;

/**
 * This processor renders features using the geotools {@link StreamingRenderer}.
 * The features are fetched through a sub pipeline for usecase
 * {@link LayerUseCase#FEATURES}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FeatureRenderProcessor2
        implements ITerminalPipelineProcessor {

    // see UDig BasisFeatureRenderer 
    
    private static final Log log = LogFactory.getLog( FeatureRenderProcessor2.class );

    private static final ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {},
            new Class[] {},
            new Class[] {ImageResponse.class, GetLayerTypesResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    public static boolean isCompatible( IService service ) {
        // we are compatible to everything a feature pipeline can be build for
        return true;
        //FeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
        
//        // Postgres
//        if (service instanceof PostgisService2) {
//            return true;
//        }
//        // WFS, Memory, ...
//        else {
//            try {
//                DataStore dataStore = service.resolve( DataStore.class, null );
//                if (dataStore != null) {
//                    return true;
//                }
//            }
//            catch (Exception e) {
//                log.warn( e.getMessage() );
//            }
//        }
//        return false;
    }
    

    
    // instance *******************************************
        
    protected MapContext              mapContext;
    
    /** The styles used in the current {@link #mapContext}, used to check if new context is needed. */
    protected Map<ILayer,Style>       styles = new HashMap();
    
    
    public void init( Properties props ) {
    }


    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        // GetMapRequest
        if (r instanceof GetMapRequest) {
            GetMapRequest request = (GetMapRequest)r;
            
            long start = System.currentTimeMillis();
            Image image = getMap( context.getLayers(), request.getWidth(), request.getHeight(), request.getBoundingBox() );
            log.debug( "   ...done: (" + (System.currentTimeMillis()-start) + "ms)." );

            context.sendResponse( new ImageResponse( image ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
//        // GetLegendGraphicRequest
//        else if (r instanceof GetLegendGraphicRequest) {
//            getLegendGraphic( (GetLegendGraphicRequest)r, context );
//        }
//        // GetLayerTypes
//        else if (r instanceof GetLayerTypesRequest) {
//            getLayerTypes();
//            List<LayerType> types = getLayerTypes();
//            context.sendResponse( new GetLayerTypesResponse( types ) );
//            context.sendResponse( ProcessorResponse.EOP );
//        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }

    
    public void processResponse( ProcessorResponse reponse, ProcessorContext context )
            throws Exception {
        throw new IllegalStateException( "This is a terminal processor." );
    }


    protected Image getMap( Set<ILayer> layers, int width, int height, ReferencedEnvelope bbox ) {
        Logger wfsLog = Logging.getLogger( "org.geotools.data.wfs.protocol.http" );
        wfsLog.setLevel( Level.FINEST );

        // mapContext
        synchronized (this) {
            // check style objects
            boolean needsNewContext = false;
            if (mapContext != null) {
                for (ILayer layer : layers) {
                    Style old = styles.get( layer );
                    try {
                        Style current = layer.getStyle().resolve( Style.class, null );
                        if (current != old) {
                            needsNewContext = true;
                            styles.clear();
                            break;
                        }
                    }
                    catch (IOException e) {
                        needsNewContext = true;
                        styles.clear();
                        break;
                    }
                }
            }
            // create mapContext
            if (mapContext == null || needsNewContext) {
                // sort z-priority
                TreeMap<String,ILayer> sortedLayers = new TreeMap();
                for (ILayer layer : layers) {
                    String uniqueOrderKey = String.valueOf( layer.getOrderKey() ) + layer.id();
                    sortedLayers.put( uniqueOrderKey, layer );
                }
                // add to mapContext
                mapContext = new DefaultMapContext( bbox.getCoordinateReferenceSystem() );
                for (ILayer layer : sortedLayers.values()) {
                    try {
                        FeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
                        log.debug( "        FeatureSource: " + fs );
                        log.debug( "            fs.getName(): " + fs.getName() );

                        Style style = layer.getStyle().resolve( Style.class, null );
                        if (style == null) {
                            log.warn( "            fs.getName(): " + fs.getName() );
                            style = new DefaultStyles().findStyle( fs );
                        }
                        mapContext.addLayer( fs, style );
                        styles.put( layer, style );
                    }
                    catch (IOException e) {
                        log.warn( e );
                        // FIXME set layer status and statusMessage
                    }
                    catch (PipelineIncubationException e) {
                        log.warn( "No pipeline.", e );
                    }
                }
            }
            else {
            }
        }
        
        // render
        BufferedImage result = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g = result.createGraphics();
        try {
            StreamingRenderer renderer = new StreamingRenderer();

            // error handler
            renderer.addRenderListener( new RenderListener() {
                public void featureRenderer( SimpleFeature feature ) {
                }
                public void errorOccurred( Exception e ) {
                    log.error( "Renderer error: ", e );
                    drawErrorMsg( g, "Fehler bei der Darstellung.", e );
                }
            });

            // rendering hints
            RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY );
            hints.add( new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON ) );
            hints.add( new RenderingHints(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );

            renderer.setJava2DHints( hints );
//            g.setRenderingHints( hints );

            // render params
            Map rendererParams = new HashMap();
            rendererParams.put( "optimizedDataLoadingEnabled", Boolean.TRUE );
            renderer.setRendererHints( rendererParams );
            
            renderer.setContext( mapContext );
            Rectangle paintArea = new Rectangle( width, height );
            renderer.paint( g, paintArea, bbox );
            return result;
        }
        catch (Throwable e) {
            log.error( "Renderer error: ", e );
            drawErrorMsg( g, null, e );
            return result;
        }
        finally {
            if (g != null) { g.dispose(); }
        }
    }

    
    protected void drawErrorMsg( Graphics2D g, String msg, Throwable e ) {
        g.setColor( Color.RED );
        g.setStroke( new BasicStroke( 1 ) );
        g.getFont().deriveFont( Font.BOLD, 12 );
        if (msg != null) {
            g.drawString( msg, 10, 10 );
        }
        if (e != null) {
            g.drawString( e.toString(), 10, 30 );
        }
    }
    
}
