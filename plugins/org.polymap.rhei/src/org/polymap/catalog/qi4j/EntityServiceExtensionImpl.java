/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.catalog.qi4j;

import java.util.Map;

import java.io.Serializable;
import java.net.URL;

import net.refractions.udig.catalog.ServiceExtension;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public abstract class EntityServiceExtensionImpl
        implements ServiceExtension {

    /**
     * The service key; used to identify the entities the service will provide.
     */
    public static final String KEY = "org.polymap.catalog.qi4j"; //$NON-NLS-1$

    public static final String PROVIDER_BASE_KEY = "entityProviderClass"; //$NON-NLS-1$

    public static final String NAME_KEY = "name";

    
    public Map<String, Serializable> createParams( URL url ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

//    public IService createService( URL id, Map<String, Serializable> params ) {
//        if (params != null) {
//            // check for the properties service key
//            if (params.containsKey( KEY )) {
//                // found it, create the service handle
//                return new EntityServiceImpl( id, params );
//            }
//        }
//        // key not found
//        return null;
//    }

}
