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
package org.polymap.core.data.wfs.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;

import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.ows.ServiceException;
import org.opengis.feature.type.Name;

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
public class WfsServiceInfo
        extends DefaultServiceInfo {

    private static Log log = LogFactory.getLog( WfsServiceInfo.class );
    
    public static WfsServiceInfo of( IMetadata metadata, Map<String,String> params ) 
            throws ServiceException, MalformedURLException, IOException {
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );

//        URL url = (URL)params.get( WFSDataStoreFactory.URL.key );
//        url = WFSDataStoreFactory.createGetCapabilitiesRequest( url );

        Map<String,Serializable> connParams = new HashMap();
        connParams.put( WFSDataStoreFactory.URL.key, url );
        connParams.put( WFSDataStoreFactory.TIMEOUT.key, 10000 );
        WFSDataStoreFactory dsf = new WFSDataStoreFactory();
        WFSDataStore ds = dsf.createDataStore( connParams );
        return new WfsServiceInfo( metadata, ds );
    }


    // instance *******************************************
    
    private WFSDataStore    ds;
    

    protected WfsServiceInfo( IMetadata metadata, WFSDataStore ds ) {
        super( metadata, ds.getInfo() );
        this.ds = ds;
    }

    
    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)ds;
    }

    
    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception {
        List<IResourceInfo> result = new ArrayList();
        for (Name name : ds.getNames()) {
            ResourceInfo info = ds.getFeatureSource( name ).getInfo();
            result.add( new WfsResourceInfo( this, info ) );
        }
        return result;
    }

    
    /**
     * 
     */
    class WfsResourceInfo
            extends DefaultResourceInfo {

        public WfsResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate ) {
            super( serviceInfo, delegate );
        }
        
    }

}
