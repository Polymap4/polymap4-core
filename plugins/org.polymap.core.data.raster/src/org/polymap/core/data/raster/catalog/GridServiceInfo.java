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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.DefaultResourceInfo;
import org.polymap.core.catalog.resolve.DefaultServiceInfo;
import org.polymap.core.catalog.resolve.IResourceInfo;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class GridServiceInfo
        extends DefaultServiceInfo {

    private GridCoverage2DReader        grid;


    protected GridServiceInfo( IMetadata metadata, GridCoverage2DReader grid ) {
        super( metadata, grid.getInfo() );
        this.grid = grid;
    }

    
    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)grid;
    }


    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception {
        return Arrays.stream( grid.getGridCoverageNames() )
                .map( name -> new DefaultResourceInfo( GridServiceInfo.this, grid.getInfo( name ) ) )
                .collect( Collectors.toList() );
    }

    
//    /**
//     * 
//     */
//    class ArcGridResourceInfo
//            extends DefaultResourceInfo {
//
//        public ArcGridResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate ) {
//            super( serviceInfo, delegate );
//        }
//        
//    }
    
}
