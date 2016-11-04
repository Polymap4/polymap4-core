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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.ResourceInfo;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.ows.ServiceException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.DefaultResourceInfo;
import org.polymap.core.catalog.resolve.DefaultServiceInfo;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.runtime.Mutex;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class GridServiceInfo
        extends DefaultServiceInfo {

    public static GridServiceInfo of( IMetadata metadata, Map<String,String> params ) 
            throws ServiceException, MalformedURLException, IOException, InterruptedException {
        
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );
        GridCoverage2DReader grid = open( FileUtils.toFile( new URL( url ) ) );
        return new GridServiceInfo( metadata, grid );
    }

    /**
     * 
     *
     * @param f
     * @return Newly created reader.
     * @throws InterruptedException 
     */
    public static AbstractGridCoverage2DReader open( File f ) throws InterruptedException {
        return initLock.lockedInterruptibly( () -> {
            AbstractGridFormat format = GridFormatFinder.findFormat( f );
            AbstractGridCoverage2DReader reader = format.getReader( f );
            return reader; 
        });
    }
    
    /**
     * Initializing several readers (for different services/files) in concurrent
     * threads results in deadlocks.
     */
    protected static final Mutex        initLock = new Mutex();
    
    
    // instance *******************************************
    
    private GridCoverage2DReader        grid;


    protected GridServiceInfo( IMetadata metadata, GridCoverage2DReader grid ) {
        super( metadata, grid.getInfo() );
        this.grid = grid;
    }
    
    @Override
    public String getTitle() {
        return StringUtils.isBlank( super.getTitle() )
                ? FilenameUtils.getBaseName( delegate.getSource().toString() )
                : super.getTitle();
    }

    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)grid;
    }

    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception {
        return Arrays.stream( grid.getGridCoverageNames() )
                .map( name -> new GridResourceInfo( name ) )
                .collect( Collectors.toList() );
    }
    
    /**
     * 
     */
    protected class GridResourceInfo
            extends DefaultResourceInfo {

        private String          coverageName;

        public GridResourceInfo( String coverageName ) {
            super( GridServiceInfo.this, grid.getInfo( coverageName ) );
            this.coverageName = coverageName;
        }

        @Override
        public String getTitle() {
            return isBlank( super.getTitle() ) ? getName() : super.getTitle();
        }

        @Override
        public String getName() {
            return isBlank( super.getName() ) ? coverageName : super.getName();
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable( super.getDescription().orElse( "Coverage" ) );
        }

        @Override
        public ReferencedEnvelope getBounds() {
            ReferencedEnvelope result = super.getBounds();
            if (result == null) {
                result = new ReferencedEnvelope();
            }
            if (result.isNull()) {
                GeneralEnvelope envelope = grid.getOriginalEnvelope( coverageName );
                result = new ReferencedEnvelope( envelope );
            }
            else if (result.isNull()) {
                GeneralEnvelope envelope = grid.getOriginalEnvelope();
                result = new ReferencedEnvelope( envelope );
            }
            return result;
        }
    }

    
    // test ***********************************************
    
    public static void main( String[] args ) throws InterruptedException {
        File f = new File( "/home/falko/Data/tiff/bluemarble.tif" );

        AbstractGridCoverage2DReader reader = open( f );
        System.out.println( "reader: " + reader );
        System.out.println( "reader: " + reader.getInfo().getSource() );
        System.out.println( "reader: " + reader.getInfo().getTitle() );
        System.out.println( "reader: " + Arrays.asList( reader.getGridCoverageNames() ) );
        System.out.println( "reader: " + reader.getFormat().getName() );
        System.out.println( "reader: " + reader.getCoordinateReferenceSystem() );
        System.out.println( "reader: " + reader.getOriginalEnvelope() );
        System.out.println( "reader: " + reader.getOriginalGridRange() );
        
        for (String name : reader.getGridCoverageNames()) {
            ResourceInfo info = reader.getInfo( name );
            System.out.println( "coverage: " + info.getTitle() );
            System.out.println( "coverage: " + info.getName() );
            System.out.println( "coverage: " + info.getDescription() );
            System.out.println( "coverage: " + info.getBounds() );
        }
        reader.dispose();
    }
    
}
