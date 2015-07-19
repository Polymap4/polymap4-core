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

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.wms.WebMapServer;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResolvableInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WmsServiceResolver
        implements IMetadataResourceResolver {

    public static final String      CONNECTION_TYPE = "WMS";
    
    
    @Override
    public boolean canResolve( Map<String,String> params ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    @Override
    public IResolvableInfo resolve( Map<String,String> params, IProgressMonitor monitor ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    @Override
    public Map<String,String> createParams( Object service ) {
        assert service instanceof WebMapServer : "Service has to be an instanceof WebMapServer.";
        WebMapServer wms = (WebMapServer)service;
        
        Map<String,String> result = new HashMap();
        result.put( CONNECTION_PARAM_TYPE, CONNECTION_TYPE );
        result.put( CONNECTION_PARAM_URL, wms.getInfo().getSource().toString() );
        return result;
    }

    
    public static Map<String,String> createParams( String serviceUrl ) {
        Map<String,String> result = new HashMap();
        result.put( CONNECTION_PARAM_TYPE, CONNECTION_TYPE );
        result.put( CONNECTION_PARAM_URL, serviceUrl );
        return result;
    }
    
}
