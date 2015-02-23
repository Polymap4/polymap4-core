/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.service.fs.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.DefaultPipelineIncubator;
import org.polymap.core.data.pipeline.IPipelineIncubator;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.session.SessionContext;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.Range;

/**
 * Provides data from the current Workbench session of the user.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WorkbenchContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static final Log log = LogFactory.getLog( WorkbenchContentProvider.class );

    private static final IPipelineIncubator   pipelineIncubator = new DefaultPipelineIncubator();


    @Override
    public List<? extends IContentNode> getChildren( IPath path ) {
        // Workbench folder
        if (path.segmentCount() == 0) {
            return Collections.singletonList( new WorkbenchFolder( path, this ) );
        }
        // MapImageFile
        IContentFolder parent = getSite().getFolder( path );
        if (parent instanceof WorkbenchFolder) {
            return Arrays.asList( 
                    new MapImageFile( path, this, null ),
                    new MapDataFile( path, this, null ),
                    new FeaturesFile( path, this, null ) );
        }
        return null;
    }
    
    
    /**
     * 
     */
    public static class WorkbenchFolder
            extends DefaultContentFolder {

        public WorkbenchFolder( IPath parentPath, IContentProvider provider ) {
            super( "Workbench", parentPath, provider, null );
        }

        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält Daten der <b>aktuell laufenden</b> Sitzung des Nutzers in der Workbench.";
        }
        
    }
    
    
    /**
     * 
     */
    public class FeaturesFile
            extends DefaultContentNode
            implements IContentFile {

        public FeaturesFile( IPath parentPath, IContentProvider provider, Object source ) {
            super( "SelectedFeatures.json", parentPath, provider, source );
        }

        @Override
        public Date getModifiedDate() {
            WorkbenchState state = WorkbenchState.instance( SessionContext.current() );
            return state.getMapModified();
        }

        @Override
        public Long getMaxAgeSeconds() {
            return null;
        }

        @Override
        public String getContentType( String accepts ) {
            return "application/json";
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType )
                throws IOException, BadRequestException {
            WorkbenchState state = WorkbenchState.instance( SessionContext.current() );
            try {
                FeatureCollection features = state.getSelectedFeatures();
                if (features != null) {
                    FeatureJSON encoder = new FeatureJSON();
                    encoder.setEncodeFeatureBounds( false );
                    encoder.setEncodeFeatureCollectionBounds( false );
                    encoder.setEncodeFeatureCollectionCRS( false );
                    encoder.setEncodeFeatureCRS( false );

                    encoder.writeFeatureCollection( features, out );
                }
                else {
                    out.write( "{\"type\": \"FeatureCollection\",\"features\": []}".getBytes( "UTF-8" ) );
                }
            }
            catch (Exception e) {
                log.debug( "", e );
                throw new RuntimeException( e );
            }
        }
    }

    
    /**
     * 
     */
    public class MapDataFile
            extends DefaultContentNode
            implements IContentFile {

        public MapDataFile( IPath parentPath, IContentProvider provider, Object source ) {
            super( "MapData.json", parentPath, provider, source );
        }

        @Override
        public Date getModifiedDate() {
            WorkbenchState state = WorkbenchState.instance( SessionContext.current() );
            return state.getMapModified();
        }

        @Override
        public Long getMaxAgeSeconds() {
            return null;
        }

        @Override
        public String getContentType( String accepts ) {
            return "application/json";
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType )
                throws IOException, BadRequestException {
            WorkbenchState state = WorkbenchState.instance( SessionContext.current() );
            IMap map = state.getMap();
            
            try {
                JSONObject json = new JSONObject();
                json.put( "width", -1 );
                json.put( "height", -1 );
                
                ReferencedEnvelope extent = map.getExtent();
                json.put( "minX", extent.getMinX() );
                json.put( "minY", extent.getMinY() );
                json.put( "maxX", extent.getMaxX() );
                json.put( "maxY", extent.getMaxY() );
                
                out.write( json.toString( 4 ).getBytes( "UTF-8" ) );
            }
            catch (JSONException e) {
                throw new RuntimeException( e );
            }
        }
    }

    
    /**
     * 
     */
    public class MapImageFile
            extends DefaultContentNode
            implements IContentFile {

        private String              contentType = "image/jpeg";
        
        
        public MapImageFile( IPath parentPath, IContentProvider provider, Object source ) {
            super( "MapImage", parentPath, provider, source );
        }

        @Override
        public Date getModifiedDate() {
            WorkbenchState state = WorkbenchState.instance( SessionContext.current() );
            return state.getMapModified();
        }

        @Override
        public Long getMaxAgeSeconds() {
            return null;
        }

        @Override
        public String getContentType( String accepts ) {
            return contentType;
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public void sendContent( OutputStream out, Range range, final Map<String,String> params, String rContentType )
                throws IOException, BadRequestException {
            final int width = params.containsKey( "width" ) ? Integer.parseInt( params.get( "width" ) ) : 300;
            final int height = params.containsKey( "height" ) ? Integer.parseInt( params.get( "height" ) ) : 300;

            List<Job> jobs = new ArrayList();
            final Map<ILayer,Image> images = new HashMap();
            
            // run jobs for all layers
            WorkbenchState state = WorkbenchState.instance( SessionContext.current() );
            final IMap map = state.getMap();
            if (map != null) {
                for (final ILayer layer : map.getLayers()) {
                    if (layer.isVisible()) {
                        UIJob job = new UIJob( getClass().getSimpleName() + ": " + layer.getLabel() ) {
                            protected void runWithException( IProgressMonitor monitor )
                                    throws Exception {
                                try {
                                    IGeoResource res = layer.getGeoResource();
                                    if (res == null) {
                                        throw new RuntimeException( "Unable to find geo resource of layer: " + layer );
                                    }
                                    IService service = res.service( null );
                                    Pipeline pipeline = pipelineIncubator.newPipeline( LayerUseCase.IMAGE, layer.getMap(), layer, service );
                                    if (pipeline.length() == 0) {
                                        throw new RuntimeException( "Unable to build processor pipeline for layer: " + layer );                        
                                    }

                                    // processor request
                                    GetMapRequest request = new GetMapRequest( null, //layers 
                                            map.getCRSCode(), map.getExtent(), 
                                            contentType, width, height, -1 );

                                    // process request
                                    pipeline.process( request, new ResponseHandler() {
                                        public void handle( ProcessorResponse pipeResponse )
                                                throws Exception {
                                            Image image = ((ImageResponse)pipeResponse).getImage();
                                            images.put( layer, image );
                                        }
                                    });
                                }
                                catch (Exception e) {
                                    // XXX put a special image in the map
                                    log.warn( "", e );
                                    images.put( layer, null );
                                    throw e;
                                }
                            }
                        };
                        jobs.add( job );
                        job.schedule();
                    }
                }

                // join jobs
                for (Job job : jobs) {
                    try { job.join(); } catch (InterruptedException e) {
                        // XXX put a special image in the map
                        log.warn( "", e );
                    }
                }
            }
                    
            // put images together (MapContext order)
            Graphics2D g = null;
            try {
                // create image
                BufferedImage result = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );
                g = result.createGraphics();

                // rendering hints
                RenderingHints hints = new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
                hints.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
                hints.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );
                g.setRenderingHints( hints );

                if (map == null) {
                    g.setFont( new Font( "Serif", Font.PLAIN, 14 ) );
                    g.setColor( Color.RED );
                    g.drawString( "Melden Sie sich in der Workbench an, um hier eine Karte zu sehen!", 50, 50 );
                }
                // FIXME honor layer.getOrderKey()
                for (Map.Entry<ILayer,Image> entry : images.entrySet()) {
                    int rule = AlphaComposite.SRC_OVER;
                    float alpha = ((float)entry.getKey().getOpacity()) / 100;

                    g.setComposite( AlphaComposite.getInstance( rule, alpha ) );
                    g.drawImage( entry.getValue(), 0, 0, null );
                }
                
                // encode image
                encodeImage( result, out );
            }
            finally {
                if (g != null) { g.dispose(); }
            }
        }
        
        
        protected void encodeImage( BufferedImage image, OutputStream out ) 
        throws IOException {
            Timer timer = new Timer();
            log.info( "encodeImage(): request format= " + contentType );

            if (contentType.equals( "image/png" )) {
                new ImageWorker( image ).writePNG( out, "PNG", 0.8f, false, false );
            }
            else if (contentType.equals( "image/jpeg" )) {
                new ImageWorker( image ).writeJPEG( out, "JPEG", 0.8f, false );
            }
            else {
                throw new RuntimeException( "Unknown contentType: " + contentType );
            }
            log.info( "    done. (" + timer.elapsedTime() + "ms)" );
        }

    }

}
