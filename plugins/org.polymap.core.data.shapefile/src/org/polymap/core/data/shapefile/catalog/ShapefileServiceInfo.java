/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.shapefile.catalog;

import java.util.Collections;
import java.util.Map;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.data.ResourceInfo;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.ows.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.DefaultResourceInfo;
import org.polymap.core.catalog.resolve.DefaultServiceInfo;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapefileServiceInfo
        extends DefaultServiceInfo {

    private static Log log = LogFactory.getLog( ShapefileServiceInfo.class );
    
    public static ShapefileServiceInfo of( IMetadata metadata, Map<String,String> params ) 
            throws ServiceException, MalformedURLException, IOException {
        
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );
        ShapefileDataStore ds = new ShapefileDataStore( new URL( url ) );
        return new ShapefileServiceInfo( metadata, ds );
    }



    // instance *******************************************
    
    private ShapefileDataStore ds;


    protected ShapefileServiceInfo( IMetadata metadata, ShapefileDataStore ds ) {
        super( metadata, ds.getInfo() );
        this.ds = ds;
    }

    
    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)ds;
    }


    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception {
        ResourceInfo info = ds.getFeatureSource().getInfo();
        return Collections.singletonList( new ShapefileResourceInfo( this, info ) );
    }

    
    /**
     * 
     */
    class ShapefileResourceInfo
            extends DefaultResourceInfo {

        public ShapefileResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate ) {
            super( serviceInfo, delegate );
        }
        
    }
    
}
