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
package org.polymap.core.mapeditor.services;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.ServiceException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import org.polymap.core.data.image.EncodedImageProducer;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.pipeline.DepthFirstStackExecutor;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineExecutor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;
import org.polymap.core.runtime.session.SessionContext;

/**
 * Provides a very simple WMS server to be used by the {@link MapViewer}. It
 * supports just GET method and GetMap request but it is fast to create and run.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class SimpleWmsServer
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( SimpleWmsServer.class );
    
    /** Delimeter for KVPs in the raw string */
    public static final String              KVP_DELIMITER = "&";

    /** Delimeter that seperates keywords from values */
    public static final String              VALUE_DELIMITER = "=";

    /** Delimeter for inner value lists in the KVPs */
    public static final String              INNER_DELIMETER = ",";

    private SessionContext                  sessionContext;
    
    /** Maps layer name into corresponding Pipeline. */
    private LoadingCache<String,Pipeline>   pipelines;
    
    
    /**
     *  
     */
    protected abstract String[] layerNames();


    /**
     * Actually creates a new {@link Pipeline} for the layer with the given name for
     * use case {@link EncodedImageProducer}. Caching of the result is done by the caller.
     * <p/>
     * This is called inside the servlet request thread.
     */
    protected abstract Pipeline createPipeline( String layerName );
    
    
    /**
     * This default implementation creates {@link DepthFirstStackExecutor}.
     *
     * @return Newly created {@link PipelineExecutor}.
     */
    protected PipelineExecutor createPipelineExecutor() {
        return new DepthFirstStackExecutor();
    }
    
    
    @Override
    public void init() throws ServletException {
        this.sessionContext = SessionContext.current();
        this.pipelines = CacheBuilder.newBuilder()
                .concurrencyLevel( 2 )
                .softValues()
                .build( new CacheLoader<String,Pipeline>() {
                    @Override
                    public Pipeline load( String key ) throws Exception {
                        return createPipeline( key );
                    }
                });
    }

    
    @Override
    public void destroy() {
        super.destroy();
    }


    @Override
    protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
            throws ServletException, IOException {
        log.debug( "Request: " + request.getQueryString() );
        
        final Map<String,String> kvp = parseKvpSet( request.getQueryString() );
        
        try {
            sessionContext.execute( new Callable() {
                public Object call() throws Exception {
                    final String layerRenderKey = kvp.get( "LAYERS" );
                    assert !layerRenderKey.contains( INNER_DELIMETER );

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

                    log.debug( "    --layers= " + layerRenderKey );
                    log.debug( "    --imageSize= " + width + "x" + height );
                    log.debug( "    --bbox= " + bbox );
                    crs = bbox.getCoordinateReferenceSystem();
                    log.debug( "    --CRS= " + bbox.getCoordinateReferenceSystem().getName() );

                    // find/create pipeline
                    final Pipeline pipeline = pipelines.get( layerRenderKey );

                    long modifiedSince = request.getDateHeader( "If-Modified-Since" );
                    final ProcessorRequest pr = new GetMapRequest( null, // layers 
                            srsCode, bbox, format, width, height, modifiedSince );  

                    // process
                    Lazy<ServletOutputStream> out = new PlainLazyInit( () -> {
                        try {
                            return response.getOutputStream();
                        }
                        catch (Exception e) {
                            log.warn( "Pipeline exception: " + e, e );
                            response.setStatus( 502 );
                            return null;
                        }
                    });
                    try {
                        createPipelineExecutor().execute( pipeline, pr, new ResponseHandler() {
                            @Override
                            public void handle( ProcessorResponse pipeResponse ) throws Exception {                                
                                if (pipeResponse == EncodedImageResponse.NOT_MODIFIED) {
                                    response.setStatus( 304 );
                                }
                                else {
                                    long lastModified = ((EncodedImageResponse)pipeResponse).getLastModified();
                                    // lastModified is only set if response comes from cache ->
                                    // allow the browser to use a cached tile for max-age without a request
                                    if (lastModified > 0) {
                                        response.setDateHeader( "Last-Modified", lastModified );
                                        response.setHeader( "Cache-Control", "max-age=180,must-revalidate" );
                                    }
                                    // disable browser cache if there is no internal Cache for this layer 
                                    else {
                                        response.setHeader( "Cache-Control", "no-cache,no-store,must-revalidate" );
                                        response.setDateHeader( "Expires", 0 );
                                        response.setHeader( "Pragma", "no-cache" );
                                    }

                                    byte[] chunk = ((EncodedImageResponse)pipeResponse).getChunk();
                                    int len = ((EncodedImageResponse)pipeResponse).getChunkSize();
                                    out.get().write( chunk, 0, len );
                                }
                            }
                        });
                    }
                    catch (Throwable e) {
                        log.warn( "Pipeline exception: " + e, e );
                        response.setStatus( 502 );
                    }
                    if (out.isInitialized()) {
                        out.get().flush();
                    }
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
    
    
    public static Map<String,String> parseKvpSet( String qString ) throws UnsupportedEncodingException {
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
                throw new IllegalArgumentException( "Bounding box coordinate " + i + " is not parsable:" + unparsed[i] );
            }
        }

        //ensure the values are sane
        double minx = bbox[0];
        double miny = bbox[1];
        double maxx = bbox[2];
        double maxy = bbox[3];

        if (minx > maxx) {
            throw new ServiceException( "Illegal bbox, minX: " + minx + " is " + "greater than maxX: " + maxx);
        }
        if (miny > maxy) {
            throw new ServiceException( "Illegal bbox, minY: " + miny + " is " + "greater than maxY: " + maxy);
        }

        //check for crs
        CoordinateReferenceSystem crs = null;
        if (unparsed.length > 4) {
            crs = CRS.decode( unparsed[4] );
        } else {
            //TODO: use the default crs of the system
        }
        return new ReferencedEnvelope( minx, maxx, miny, maxy, crs );
    }

}
