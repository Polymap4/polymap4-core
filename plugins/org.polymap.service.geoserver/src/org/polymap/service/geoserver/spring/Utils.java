/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.service.geoserver.spring;

import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.impl.NamespaceInfoImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;
import org.polymap.core.runtime.Stringer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class Utils {

    private static Log log = LogFactory.getLog( Utils.class );

    /** FIXME The namespace of all features delivered via GeoServer. */
    public static final String      NAMESPACE = "http://www.opengis.net/gml";


    public static Lazy<NamespaceInfo> defaultNsInfo = new PlainLazyInit( () -> {
        NamespaceInfoImpl result = new NamespaceInfoImpl();
//            @Override
//            public String getId() {
//                throw new RuntimeException( "not yet implemented." );
//            }
//        };
        result.setPrefix( "gml" );
        result.setURI( NAMESPACE );
        return result;
    });
    
    
    public static String simpleName( String s ) {
        // FIXME make replacement configurable
        return Stringer.of( s ).replaceUmlauts().toURIPath( "" ).toString();
    }

}
