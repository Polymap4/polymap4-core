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
package org.polymap.service.geoserver;

import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.impl.NamespaceInfoImpl;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;
import org.polymap.core.runtime.Stringer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class GeoServerUtils {

    public static final Lazy<NamespaceInfo> defaultNsInfo = new PlainLazyInit( () -> {
        NamespaceInfoImpl result = new NamespaceInfoImpl();
        result.setPrefix( "mapzone" );  // XXX make it something like: <user>-<project>
        result.setURI( "http://mapzone.io" );  // "http://www.opengis.net/gml"
        return result;
    });
    
    
    public static String simpleName( String s ) {
        // FIXME make replacement configurable
        return Stringer.of( s ).replaceUmlauts().toURIPath( "" ).toString();
    }

}
