/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.eclipse.swt.widgets.Display;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.services.http.HttpService;

/**
 * SPI/API and factory fpr HTTP server providing {@link SimpleFeature}s as
 * GeoJSON.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class JSONServer 
        extends HttpService {

    private static final Log log = LogFactory.getLog( JSONServer.class );

    public static final int             DEFAULT_MAX_BYTES = 2*1024*1024;
    
    // static factory *************************************
    
    public static JSONServer newServer( String url, Collection<SimpleFeature> features, CoordinateReferenceSystem mapCRS ) 
    throws MalformedURLException {
        // GtJSONSServer is faster but does not properly encode in all situations 
        return new GsJSONServer( url, features, mapCRS );
    }
    
    
    // instance *******************************************
    
    protected Collection<SimpleFeature> features;
    
    protected CoordinateReferenceSystem mapCRS;
    
    protected int                       maxBytes = DEFAULT_MAX_BYTES;
    
    protected int                       decimals;
    
    protected Display                   display;
    
    
    protected JSONServer( String url, Collection<SimpleFeature> features, CoordinateReferenceSystem mapCRS ) 
    throws MalformedURLException {
        super();
        super.init( url, null );
        this.features = features;
        this.mapCRS = mapCRS;
        this.decimals = 5;
        this.display = Polymap.getSessionDisplay();
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
     * @see JSONServer#DEFAULT_MAX_BYTES
     */
    public void setMaxBytes( int maxBytes ) {
        this.maxBytes = maxBytes;
    }


    /**
     * Update the features of this server. Subsequent request will receive the
     * new features.
     * 
     * @param features Collections of {@link SimpleFeature}s. XXX transform
     *        complex features.
     */
    public void setFeatures( Collection<Feature> features ) {
        log.info( "new features: " + features.size() );
        this.features = new ArrayList( features.size() );
        for (Feature feature : features) {
            if (feature instanceof SimpleFeature) {
                this.features.add( (SimpleFeature)feature );
            }
            else {
                throw new IllegalArgumentException( "Complex features are not supported yet." );
            }
        }
    }

    
    /**
     * Update the features of this server. Subsequent request will receive
     * the new features.
     */
    public void setFeatures( FeatureCollection fc ) {
        log.info( "new features: " + features.size() );
        this.features = new ArrayList( 256 );
        FeatureIterator it = fc.features();
        try {
            while (it.hasNext()) {
                features.add( (SimpleFeature)it.next() );
            }
        }
        finally {
            it.close();
        }
    }

    
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        log.info( "Accept-Encoding: " + request.getHeader( "Accept-Encoding" ) );
        log.info( "### JSON: about to encode JSON...." );
        boolean gzip = false;  //request.getHeader( "Accept-Encoding" ).toLowerCase().indexOf( "gzip" ) > -1;

        // prevent caching
        response.setHeader( "Cache-Control", "no-cache" ); // HTTP 1.1
        response.setHeader( "Pragma", "no-cache" ); // HTTP 1.0
        response.setDateHeader( "Expires", 0 ); // prevents caching at the proxy
        response.setCharacterEncoding( "UTF-8" );
        
        long start = System.currentTimeMillis();
        OutputStream debugOut = log.isDebugEnabled() 
                ? new TeeOutputStream( response.getOutputStream(), System.out )
                : response.getOutputStream();
        
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

        encode( out, response.getCharacterEncoding() );
        out.flush();
        
        log.info( "    JSON bytes: " + out.getCount() + " (" + (System.currentTimeMillis()-start) + "ms)" );
        if (cout2 != null) {
            log.info( "    bytes written: " + cout2.getCount() );
        }
    }


    protected abstract void encode( CountingOutputStream out, String encoding )
    throws IOException;
    
}