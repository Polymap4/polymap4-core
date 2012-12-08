/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;

import java.io.IOException;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.polymap.core.runtime.Polymap;

/**
 * API: holds information about a 'layer' provided by a {@link SimpleJsonServer}.
 * <p/>
 * SPI: interface of a JSON encoding service. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class JsonEncoder {

    private static final Log log = LogFactory.getLog( SimpleJsonServer.class );

    public static JsonEncoder newInstance() {
        // GsJsonEncoder might be slower than GtJsonEncoder but it handles
        // transformation in all cases; see GtJsonEncoder for a list of issues
        return new GtJsonEncoder();
    }
    
    
    // instance *******************************************
    
    protected String                    name;
    
    protected Collection<Feature>       features;
    
    protected CoordinateReferenceSystem mapCRS;

    protected int                       maxBytes = SimpleJsonServer.DEFAULT_MAX_BYTES;
    
    protected int                       decimals;

    protected Display                   display;

    private boolean                     oneShot;

    
    @SuppressWarnings("hiding")
    public void init( String name, Collection<Feature> features, CoordinateReferenceSystem mapCRS ) {
        this.name = name;
        this.features = features;
        this.mapCRS = mapCRS;
        this.display = Polymap.getSessionDisplay();
    }


    public abstract void encode( CountingOutputStream out, String encoding )
    throws IOException;
    
    
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


    /**
     * Update the features of this server. Subsequent request will receive the
     * new features.
     * <p/>
     * XXX transform complex features.
     * 
     * @param features Collections of {@link SimpleFeature}s. 
     */
    public void setFeatures( Collection<Feature> features ) {
        log.info( "new features: " + features.size() );
        this.features = new ArrayList( features.size() );
        for (Feature feature : features) {
            if (feature instanceof SimpleFeature) {
                this.features.add( feature );
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
        this.features = new ArrayList( 1024 );
        FeatureIterator it = fc.features();
        try {
            while (it.hasNext()) {
                features.add( it.next() );
            }
        }
        finally {
            it.close();
        }
    }


    public void setOneShot( boolean oneShot ) {
        this.oneShot = oneShot;
    }

    public boolean isOneShot() {
        return oneShot;
    }

    public String getName() {
        return name;
    }
    
}
