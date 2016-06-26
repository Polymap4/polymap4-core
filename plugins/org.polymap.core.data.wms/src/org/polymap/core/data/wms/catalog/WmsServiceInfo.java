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
package org.polymap.core.data.wms.catalog;

import java.util.Map;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.data.wms.WebMapServer;
import org.geotools.ows.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.DefaultServiceInfo;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.runtime.StreamIterable;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WmsServiceInfo
        extends DefaultServiceInfo {

    private static Log log = LogFactory.getLog( WmsServiceInfo.class );
    
    public static WmsServiceInfo of( IMetadata metadata, Map<String,String> params ) 
            throws ServiceException, MalformedURLException, IOException {
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );
        WebMapServer wms = new WebMapServer( new URL( url ) );
        return new WmsServiceInfo( metadata, wms );
    }


    // instance *******************************************
    
    private WebMapServer wms;
    

    protected WmsServiceInfo( IMetadata metadata, WebMapServer wms ) {
        super( metadata, wms.getInfo() );
        this.wms = wms;
    }

    
    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)wms;
    }


    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) {
        return StreamIterable.of( wms.getCapabilities().getLayerList().stream()
                .map( layer -> wms.getInfo( layer ) )
                .map( info -> new WmsResourceInfo( WmsServiceInfo.this, info ) ) );
    }
    
}
