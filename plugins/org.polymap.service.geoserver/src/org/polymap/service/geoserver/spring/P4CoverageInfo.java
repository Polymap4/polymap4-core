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

import static java.util.stream.Collectors.toList;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.Keyword;
import org.geoserver.catalog.impl.CoverageInfoImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;

/**
 * An upstream WMS.
 *
 * @deprecated Feature layers and upstream WMS are represented same way; all
 *             rendering is done by PipelineMapResponse. So this is not currently
 *             used.
 * @author Falko Bräutigam
 */
public class P4CoverageInfo
        extends CoverageInfoImpl
        implements CoverageInfo {

    private static Log log = LogFactory.getLog( P4CoverageInfo.class );

    public P4CoverageInfo( Catalog catalog, P4DataStoreInfo dsInfo ) {
        super( catalog );
        setStore( dsInfo );

        ILayer layer = dsInfo.getLayer();
        setNamespace( Utils.defaultNsInfo.get() );
        setName( Utils.simpleName( layer.label.get() ) );
        setTitle( layer.label.get() );
        setDescription( "Coverage of ILayer: " + layer.label.get() );
        setKeywords( layer.keywords.stream().map( kw -> new Keyword(kw) ).collect( toList() ) );
    }
    
}
