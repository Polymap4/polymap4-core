/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br√‰utigam. All rights reserved.
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
package org.polymap.core.data.rs.catalog;

import java.util.Map;
import java.util.stream.Collectors;

import java.io.File;
import java.net.URL;

import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.DefaultResourceInfo;
import org.polymap.core.catalog.resolve.DefaultServiceInfo;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.data.rs.RDataStore;
import org.polymap.core.data.rs.lucene.LuceneQueryDialect;
import org.polymap.core.runtime.Streams;
import org.polymap.core.runtime.Streams.ExceptionCollector;

import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br‰utigam</a>
 */
public class RServiceInfo
        extends DefaultServiceInfo {

    private static Log log = LogFactory.getLog( RServiceInfo.class );
    
    public static RServiceInfo of( IMetadata metadata, Map<String,String> params ) throws Exception {
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );
        File storeDir = new File( new URL( url ).toURI() );
        LuceneRecordStore store = new LuceneRecordStore( storeDir, false );
        return new RServiceInfo( metadata, new RDataStore( store, new LuceneQueryDialect() ) );
    }



    // instance *******************************************
    
    private RDataStore          ds;


    protected RServiceInfo( IMetadata metadata, RDataStore ds ) {
        super( metadata, ds.getInfo() );
        this.ds = ds;
    }

    
    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)ds;
    }


    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception {
        try (ExceptionCollector<?> exc = Streams.exceptions()) {
            return ds.getNames().stream()
                    .map( name -> exc.check( () -> ds.getFeatureSource( name ).getInfo() ) )
                    .map( info -> new RResourceInfo( RServiceInfo.this, info ) )
                    .collect( Collectors.toList() );
        }
    }

    
    public IResourceInfo resource( FeatureSource fs ) {
        return new RResourceInfo( this, fs.getInfo() );
    }
    
    
    /**
     * 
     */
    public class RResourceInfo
            extends DefaultResourceInfo {

        public RResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate ) {
            super( serviceInfo, delegate );
        }
        
    }
    
}
