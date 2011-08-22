/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
 */
package org.polymap.service.fs.providers;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;

import org.polymap.service.fs.FsPlugin;
import org.polymap.service.fs.spi.IContentSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ShapefileGenerator {

    private static Log log = LogFactory.getLog( ShapefileGenerator.class );

    /** The file extensions generated with standard settings. */
    public static final String[]        FILE_SUFFIXES = {"shp", "shx", "qix", "fix", "dbf", "prj"};

    
    private File                newFile;
    
    
    public ShapefileGenerator( File newFile ) {
        this.newFile = newFile;
    }


    /**
     * Create a temporary file in {@link FsPlugin#getCacheDir()} to store the new shapefile in.
     */
    public ShapefileGenerator( ILayer layer, IContentSite site ) {
        File tmpDir = FsPlugin.getDefault().getCacheDir();
        String basename = FilenameUtils.normalize( layer.getLabel() );
        String projectname = FilenameUtils.normalize( layer.getMap().getLabel() );
        String username = site.getUserName() != null ? site.getUserName() : "null";
        this.newFile = new File( tmpDir, username + "@" + projectname + "@" + basename + ".shp" );
    }


    public File writeShapefile( FeatureCollection<SimpleFeatureType,SimpleFeature> src )
    throws IOException {
        SimpleFeatureType srcSchema = src.getSchema();
        
        ShapefileDataStoreFactory shapeFactory = new ShapefileDataStoreFactory();

        Map<String,Serializable> params = new HashMap<String,Serializable>();
        params.put( "url", newFile.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ShapefileDataStore shapeDs = (ShapefileDataStore)shapeFactory.createNewDataStore( params );
        shapeDs.createSchema( srcSchema );

        //shapeDs.forceSchemaCRS(DefaultGeographicCRS.WGS84);
        //shapeDs.setStringCharset( )
        
        // write shapefile
        Transaction tx = new DefaultTransaction( "create" );

        String typeName = shapeDs.getTypeNames()[0];
        FeatureStore<SimpleFeatureType,SimpleFeature> shapeFs 
                = (FeatureStore<SimpleFeatureType, SimpleFeature>)shapeDs.getFeatureSource( typeName );

        shapeFs.setTransaction( tx );
        try {
            shapeFs.addFeatures( src );
            tx.commit();
        } 
        catch (IOException e) {
            tx.rollback();
            throw e;
        } 
        finally {
            tx.close();
        }
        
        return newFile;
    }
    
}
