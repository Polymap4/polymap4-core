/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.raster.catalog;

import java.util.Map;
import java.io.IOException;
import java.net.MalformedURLException;

import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.ows.ServiceException;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TiffServiceInfo
        extends GridServiceInfo {

    
    public static TiffServiceInfo of( IMetadata metadata, Map<String,String> params ) 
            throws ServiceException, MalformedURLException, IOException {
        
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );
        GeoTiffReader grid = new GeoTiffReader( url, null );
        return new TiffServiceInfo( metadata, grid );
    }

    
    protected TiffServiceInfo( IMetadata metadata, GridCoverage2DReader grid ) {
        super( metadata, grid );
    }

}
