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
package org.polymap.core.mapeditor.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.ServiceException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.pipeline.DefaultPipelineIncubator;
import org.polymap.core.data.pipeline.IPipelineIncubator;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.runtime.SessionContext;

import org.polymap.service.http.WmsService;

/**
 * Provides a very simple WMS server to be used by the {@link MapEditor}. It
 * supports just GET method and GetMap request but it is fast to create and run.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class SimpleWmsServer
        extends WmsService {

    private static final Log log = LogFactory.getLog( SimpleWmsServer.class );
    
    /** Delimeter for KVPs in the raw string */
    public static final String      KVP_DELIMITER = "&";

    /** Delimeter that seperates keywords from values */
    public static final String      VALUE_DELIMITER = "=";

    /** Delimeter for inner value lists in the KVPs */
    public static final String      INNER_DELIMETER = ",";

    private IPipelineIncubator      pipelineIncubator = new DefaultPipelineIncubator();

    /** Maps layer name into corresponding Pipeline. */
    private Map<String,Pipeline>    pipelines = new HashMap();
    
    private ReentrantReadWriteLock  pipelinesLock = new ReentrantReadWriteLock();
    
    private SessionContext          sessionContext;

    
    public SimpleWmsServer( ) 
    throws MalformedURLException {
    }

    
    public void init( String _pathSpec, IMap _map )
    throws MalformedURLException {
        super.init( _pathSpec, _map );
        this.sessionContext = SessionContext.current();
    }

    
    protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
        log.debug( "Request: " + request.getQueryString() );
        
        final Map<String,String> kvp = parseKvpSet( request.getQueryString() );
        
        try {
            sessionContext.execute( new Callable() {
                public Object call() throws Exception {
                    final String[] layers = StringUtils.split( kvp.get( "LAYERS" ), INNER_DELIMETER );

                    // width/height
                    int width = Integer.parseInt( kvp.get( "WIDTH" ) );
                    int height = Integer.parseInt( kvp.get( "HEIGHT" ) );

                    // BBOX
                    ReferencedEnvelope bbox = parseBBox( kvp.get( "BBOX" ) );
                    String srsCode = kvp.get( "SRS" );
                    // XXX hack support for EPSG:3857 : send different srs and crs
//                    CoordinateReferenceSystem crs = srsCode.equals( "EPSG:3857" )
//                            ? CRS.decode( "EPSG:900913" )
//                            : CRS.decode( srsCode );
                    CoordinateReferenceSystem crs = CRS.decode( srsCode );
                    bbox = new ReferencedEnvelope( bbox, crs );

                    // FORMAT
                    String format = kvp.get( "FORMAT" );
                    format = format != null ? format : "image/png";

                    log.debug( "    --layers= " + layers );
                    log.debug( "    --imageSize= " + width + "x" + height );
                    log.debug( "    --bbox= " + bbox );
                    crs = bbox.getCoordinateReferenceSystem();
                    log.debug( "    --CRS= " + bbox.getCoordinateReferenceSystem().getName() );
            
                    // find/create pipeline
                    final Pipeline pipeline = getOrCreatePipeline( layers, LayerUseCase.ENCODED_IMAGE );

                    long modifiedSince = request.getDateHeader( "If-Modified-Since" );
                    final ProcessorRequest pr = new GetMapRequest( 
                            Arrays.asList( layers ), srsCode, bbox, format, width, height, modifiedSince );  

                    // process
                    log.debug( "HTTP BUFFER: " + response.getBufferSize() );
                    final ServletOutputStream out = response.getOutputStream();

                    pipeline.process( pr, new ResponseHandler() {
                        public void handle( ProcessorResponse pipeResponse )
                        throws Exception {
                            if (pipeResponse == EncodedImageResponse.NOT_MODIFIED) {
                                response.setStatus( 304 );
                                response.flushBuffer();
                            }
                            else {
                                long lastModified = ((EncodedImageResponse)pipeResponse).getLastModified();
                                if (lastModified > 0) {
                                    response.setDateHeader( "Last-Modified", lastModified );
                                    response.setHeader( "Cache-Control", "no-cache,must-revalidate" );
                                }
                                else {
                                    response.setHeader( "Cache-Control", "no-cache,must-revalidate" );
                                    response.setDateHeader( "Expires", 0 );
                                }

                                byte[] chunk = ((EncodedImageResponse)pipeResponse).getChunk();
                                int len = ((EncodedImageResponse)pipeResponse).getChunkSize();
                                out.write( chunk, 0, len );
                            }
                        }
                    });
                    
                    log.debug( "    flushing response stream..." );
                    out.flush();
                    response.flushBuffer();
                    return null;
                }
            });
        }
        catch (IOException e) {
            // assuming that this is an EOF exception
            log.info( "Exception: " + e );
        }
        catch (Exception e) {
            log.warn( e.toString(), e );
        }
        finally {
            // XXX do I have to close out?
            //out.close();
        }
    }
    
    
    /**
     * Creates a new processing {@link Pipeline} for the given {@link ILayer}
     * and usecase.
     * <p>
     * XXX The result needs to be cached
     * 
     * @throws IOException 
     * @throws PipelineIncubationException 
     * @throws ServletException 
     */
    protected Pipeline getOrCreatePipeline( String[] layers, LayerUseCase usecase ) 
    throws IOException, PipelineIncubationException, ServletException {
        if (layers.length == 0 || layers.length > 1) {
            throw new ServletException( "Wrong layers param: " + layers );
        }

        try {
            pipelinesLock.readLock().lock();
            Pipeline pipeline = pipelines.get( layers[0] );
            
            if (pipeline == null) {
                pipelinesLock.readLock().unlock();
                pipelinesLock.writeLock().lock();

                pipeline = pipelines.get( layers[0] );
                if (pipeline == null) {
                    ILayer layer = findLayer( layers[0] );
                    IService service = findService( layer );
                    pipeline = pipelineIncubator.newPipeline( usecase, layer.getMap(), layer, service );
                    
                    if (pipeline.length() == 0) {
                        throw new ServiceException( "Unable to build processor pipeline for layer: " + layer );                        
                    }

                    pipelines.put( layer.getLabel(), pipeline );
                }
            }
            return pipeline;
        }
        finally {
            if (pipelinesLock.writeLock().isHeldByCurrentThread()) {
                pipelinesLock.readLock().lock();
                pipelinesLock.writeLock().unlock();
            }
            pipelinesLock.readLock().unlock();
        }
    }


    /**
     * Find the corresponding {@link ILayer} for the given layer name. The
     * label property is used as name.
     */
    protected ILayer findLayer( String layerName ) {
        log.debug( "findLayer(): layerName=" + layerName );

        for (ILayer layer : getMap().getLayers()) {
            if (layer.getLabel().equals( layerName )) {
                return layer;
            }
        }
        return null;
    }

    
    protected IService findService( ILayer layer ) 
    throws IOException {
        IGeoResource res = layer.getGeoResource();
        if (res == null) {
            throw new ServiceException( "Unable to find geo resource of layer: " + layer );
        }
        // XXX give a reasonable monitor; check state 
        IService service = res.service( null );
        log.debug( "service: " + service );
        return service;
    }

    
    public static Map<String,String> parseKvpSet( String qString ) 
    throws UnsupportedEncodingException {
    
        // uses the request cleaner to remove HTTP junk
        String cleanRequest = java.net.URLDecoder.decode( qString, "UTF-8");

        Map<String,String> kvps = new HashMap();
        StringTokenizer requestKeywords = new StringTokenizer( 
                cleanRequest.trim(), KVP_DELIMITER);

        while (requestKeywords.hasMoreTokens()) {
            String kvp = requestKeywords.nextToken();
            StringTokenizer requestValues = new StringTokenizer( kvp, VALUE_DELIMITER );

            // make sure that there is a key token
            if (requestValues.hasMoreTokens()) {
                // assign key as uppercase to eliminate case conflict
                String key = requestValues.nextToken().toUpperCase();

                // make sure that there is a value token
                if (requestValues.hasMoreTokens()) {
                    String value = requestValues.nextToken();
                    log.debug( "parseKvpSet(): putting kvp: " + key + " = " + value);
                    kvps.put(key, value);
                }
            }
        }
        log.debug( "parseKvpSet(): result= " + kvps );
        return kvps;
    }

    
    public static ReferencedEnvelope parseBBox( String value ) 
    throws NoSuchAuthorityCodeException, FactoryException {
        String[] unparsed = StringUtils.split( value, INNER_DELIMETER );

        // check to make sure that the bounding box has 4 coordinates
        if (unparsed.length < 4) {
            throw new IllegalArgumentException( 
                    "Requested bounding box contains wrong"
                    + "number of coordinates (should have " + "4): " + unparsed.length);
        }

        //if it does, store them in an array of doubles
        double[] bbox = new double[4];
        for (int i=0; i<4; i++) {
            try {
                bbox[i] = Double.parseDouble( unparsed[i] );
            } 
            catch (NumberFormatException e) {
                throw new IllegalArgumentException( 
                        "Bounding box coordinate " + i
                        + " is not parsable:" + unparsed[i] );
            }
        }

        //ensure the values are sane
        double minx = bbox[0];
        double miny = bbox[1];
        double maxx = bbox[2];
        double maxy = bbox[3];

        if (minx > maxx) {
            throw new ServiceException(
                    "Illegal bbox, minX: " + minx + " is " + "greater than maxX: " + maxx);
        }
        if (miny > maxy) {
            throw new ServiceException(
                    "Illegal bbox, minY: " + miny + " is " + "greater than maxY: " + maxy);
        }

        //check for crs
        CoordinateReferenceSystem crs = null;
        if (unparsed.length > 4) {
            crs = CRS.decode( unparsed[4] );
        } else {
            //TODO: use the default crs of the system
        }
        return new ReferencedEnvelope(minx,maxx,miny,maxy,crs);
    }

}
