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
import static org.polymap.service.geoserver.GeoServerUtils.simpleName;

import java.util.Collections;
import java.io.File;
import java.io.IOException;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.Keyword;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedType;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.impl.ResourceInfoImpl;
import org.geoserver.catalog.impl.StyleInfoImpl;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerResourceLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;

import org.polymap.service.geoserver.GeoServerUtils;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class P4LayerInfo
        extends LayerInfoImpl
        implements LayerInfo {

    private static Log log = LogFactory.getLog( P4LayerInfo.class );
    
    private Catalog             catalog;

    private ILayer              layer;

    
    public P4LayerInfo( Catalog catalog, ILayer layer, ResourceInfoImpl resInfo, PublishedType type ) {
        super();
        this.catalog = catalog;
        this.layer = layer;
        
        setResource( resInfo );
        setName( GeoServerUtils.simpleName( layer.label.get() ) );
        setTitle( layer.label.get() );
        resInfo.setAbstract( layer.description.get() );
        resInfo.setKeywords( layer.keywords.stream().map( kw -> new Keyword( kw ) ).collect( toList() ) );

        setEnabled( true );
        setAdvertised( true );
        setType( type );
        setId( layer.id() );
        log.info( "    loaded: " + this );
    }

    
    @Override
    public String prefixedName() {
        // XXX Dieser bescheuerte GeoServer macht den Namen des Workspaces (!?!?) als
        // Präfix an den Namen... nur um diesen Namen dann später bei einem GetMap nicht
        // mehr zu kennen. :( Ich hab echt die Schnauze voll.
        return getName();
    }


    public void createFeatureStyleInfo( String sld, GeoServerResourceLoader resourceLoader ) 
            throws IOException {
        // have to serialize a sld file to our workspace as also the default look up
        // tries to resolve the default style from workspace:
        // layerInfo.setDefaultStyle( catalogBuilder.getDefaultStyle(ftInfo) );
        
        // TODO: question is how to initially retrieve all required files to be placed
        // in an local workspace, i.e. copy files from some GeoServer (Spring) jar
        // to our workspace
        
        StyleInfoImpl style = new StyleInfoImpl( catalog );
        String styleName = simpleName( layer.label.get() ) + "-style";
        style.setId( styleName );
        style.setName( styleName );

        File sldFile = new GeoServerDataDirectory( resourceLoader ).config( style ).file();
        if (!sldFile.getParentFile().exists()) {
            sldFile.getParentFile().mkdirs();
        }
        FileUtils.writeStringToFile( sldFile, sld, "UTF-8" );

        style.setFilename( sldFile.getName() );
        catalog.add( style );

        // add
        setStyles( Collections.singleton( style ) );
        setDefaultStyle( style );
    }
    
}
