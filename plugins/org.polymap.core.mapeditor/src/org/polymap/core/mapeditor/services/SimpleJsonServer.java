/* 
 * polymap.org
 * Copyright 2010-2012 Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.ISessionListener;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.runtime.Timer;

import org.polymap.service.ServicesPlugin;

/**
 * SPI/API and factory for HTTP server providing {@link SimpleFeature}s as
 * GeoJSON.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class SimpleJsonServer 
        extends HttpServlet
        implements ISessionListener {

    private static final Log log = LogFactory.getLog( SimpleJsonServer.class );

    public static final int             DEFAULT_MAX_BYTES = 1*1024*1024;
    
    
    // static factory *************************************
    
    public static synchronized SimpleJsonServer instance() {
        SimpleJsonServer instance = SessionSingleton.instance( SimpleJsonServer.class );
        if (!instance.isStarted()) {
            try {
                instance.start();
                SessionContext.current().addSessionListener( instance );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return instance;
    }
    

    // instance *******************************************
    
    /** Default value if layer has no settings. */
    private int                     maxBytes = DEFAULT_MAX_BYTES;
    
    /** Default value 6 is good for EPSG:31468, 3857 and WGS84 */
    private int                     decimals = 6;
    
    private Map<String,JsonEncoder> layers = new HashMap();    
    
    private int                     totalLayers;

    private String                  pathSpec;
    
    private boolean                 started;
    
    
    public SimpleJsonServer() {
        pathSpec = ServicesPlugin.createServicePath( "/mapeditorjson--" + hashCode() );
        log.debug( "Path: " + pathSpec );
    }


    public boolean isStarted() {
        return started;
    }

    public String getPathSpec() {
        return pathSpec;
    }


    protected void start()
    throws Exception {
        CorePlugin.registerServlet( pathSpec, this, null );
        started = true;
    }

    
    public void dispose() {
        if (isStarted()) {
            CorePlugin.unregister( this );
            started = false;
        }
        if (layers != null) {
            layers.clear();
            layers = null;
        }
    }

    
    /* ISessionListener */
    public void beforeDestroy() {
        dispose();
    }


    public JsonEncoder newLayer( Collection<Feature> features, 
            CoordinateReferenceSystem mapCRS, boolean oneShot ) {
        JsonEncoder layer = JsonEncoder.newInstance();
        layer.setDecimals( decimals );
        
        layer.init( "layer"+totalLayers++, features, mapCRS );
        layer.setOneShot( oneShot );
        synchronized (layers) {
            layers.put( layer.name, layer );
        }
        return layer;
    }
    
    
    public void removeLayer( JsonEncoder jsonEncoder ) {
        synchronized (layers) {
            JsonEncoder removed = layers.remove( jsonEncoder.name );
            assert removed != null : "Layer was not registered: " + jsonEncoder.name;
        }
    }


    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        log.info( "Accept-Encoding: " + request.getHeader( "Accept-Encoding" ) );
        log.info( "### JSON: about to encode JSON...." );
        boolean gzip = request.getHeader( "Accept-Encoding" ).toLowerCase().contains( "gzip" );
    
        // prevent caching
        response.setHeader( "Cache-Control", "no-cache" ); // HTTP 1.1
        response.setHeader( "Pragma", "no-cache" ); // HTTP 1.0
        response.setDateHeader( "Expires", 0 ); // prevents caching at the proxy
        response.setCharacterEncoding( "UTF-8" );
        
        Timer timer = new Timer();
        log.debug( "SimpleJsonServer Output: " );
        OutputStream debugOut = /*log.isDebugEnabled() 
                ? new TeeOutputStream( response.getOutputStream(), System.out )
                : */response.getOutputStream();
        
        CountingOutputStream out = null, cout2 = null;
        if (gzip) {
            response.setHeader( "Content-Encoding", "gzip" );
            //response.setHeader( "Content-Type", "text/javascript" );
            cout2 = new CountingOutputStream( debugOut );
            out = new CountingOutputStream( new GZIPOutputStream( cout2 ) );
        }
        else {
            out = new CountingOutputStream( debugOut );
        }
    
        String layerName = StringUtils.substringAfterLast( request.getPathInfo(), "/" );
        JsonEncoder layer = null;
        synchronized (layers) {
            layer = layers.get( layerName );
        }
        layer.encode( out, response.getCharacterEncoding() );
        out.close();
        
        log.info( "    JSON bytes: " + out.getCount() + " (" + timer.elapsedTime() + "ms)" );
        if (cout2 != null) {
            log.info( "    GZIPed bytes written: " + cout2.getCount() );
        }
        
        if (layer.isOneShot()) {
            removeLayer( layer );
            log.info( "    Layer is oneShot -> removed." );
        }
    }


    public int getDecimals() {
        return decimals;
    }
    
    /**
     * The number of decimals to use when encoding floating point numbers.
     */
    public void setDecimals( int decimals ) {
        this.decimals = decimals;
    }

    /**
     * @see #setMaxBytes(int)
     */
    public int getMaxBytes() {
        return maxBytes;
    }
    
    /**
     * Sets the maximum number of bytes this server will send to the client. This is
     * not a straight limit. The server stops encoding new features if the limit
     * is reached.
     * 
     * @see SimpleJsonServer#DEFAULT_MAX_BYTES
     */
    public void setMaxBytes( int maxBytes ) {
        this.maxBytes = maxBytes;
    }


}