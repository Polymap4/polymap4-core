/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.raster;

import java.io.File;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.gce.geotiff.GeoTiffReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Mutex;

/**
 *
 * Initializing several readers (for different services/files) in concurrent
 * threads results in deadlocks. Does anybody know why?
 *
 * @author Falko Bräutigam
 */
public class GridCoverageReaderFactory {

    private static final Log log = LogFactory.getLog( GridCoverageReaderFactory.class );
    
    protected static final Mutex lock = new Mutex();

    /**
     * Creates a new {@link GridCoverage2DReader} for the given file.
     *
     * @param f
     * @return Newly created reader.
     * @throws Exception
     */
    public static AbstractGridCoverage2DReader open( File f ) throws Exception {
        return lock.lockedInterruptibly( () -> {
//            Hints hints = new Hints();
//            hints.put( Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, CRS.decode( "EPSG:9001" ) );    
//            hints.put( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE );
            
            AbstractGridFormat format = GridFormatFinder.findFormat( f );
            AbstractGridCoverage2DReader reader = format.getReader( f );
            return reader; 
        });
    }

    /**
     * Creates a new {@link GridCoverage2DReader} for the given file.
     *
     * @param f
     * @return Newly created reader.
     * @throws Exception
     */
    public static AbstractGridCoverage2DReader openGeoTiff( File f ) throws Exception {
        return lock.lockedInterruptibly( () -> {
            return new GeoTiffReader( f ); 
        });
    }

}
