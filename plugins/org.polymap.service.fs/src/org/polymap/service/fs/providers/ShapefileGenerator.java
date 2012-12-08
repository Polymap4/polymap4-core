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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.util.RetypingFeatureCollection;
import org.polymap.core.project.ILayer;

import org.polymap.service.fs.FsPlugin;
import org.polymap.service.fs.spi.IContentSite;

/**
 * Builds shapefile for the {@link ShapefileContainer}.
 * <ul>
 *   <li>create folder in plugin's cache dir</li>
 *   <li>create target schema with new field 'orig-fid' added</li>
 *   <li>add the orig-fid field to features when iterating</li>
 * </ul>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ShapefileGenerator {

    private static Log log = LogFactory.getLog( ShapefileGenerator.class );

    /** The file extensions generated with standard settings. */
    public static final String[]            FILE_SUFFIXES = {"shp", "shx", "qix", "fix", "dbf", "prj"};

    public static final String              ORIG_FID_FIELD = "orig-fid"; 
    public static final String              TIMESTAMP_FIELD = "timestamp";

    private static final ThreadLocal<DateFormat>    formatters = new ThreadLocal();
    
    
    /**
     * Returns the timestamp formatter of teh current thread.
     */
    public static DateFormat timestampFormat() {
        DateFormat formatter = formatters.get();
        if (formatter == null) {
            formatter = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
            formatters.set( formatter );
        }
        return formatter;
    }
    
    
    // instance *******************************************
    
    private File                        newFile;
    
    
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
        
        File basedir = new File( tmpDir, username + "@" + projectname + "@" + basename );
        basedir.mkdirs();
        
        this.newFile = new File( basedir, basename + ".shp" );
    }


    public File writeShapefile( FeatureCollection<SimpleFeatureType,SimpleFeature> src )
    throws IOException {
        SimpleFeatureType srcSchema = src.getSchema();
        
        // shapeSchema
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( srcSchema );
        builder.add( ORIG_FID_FIELD, String.class );
        builder.add( TIMESTAMP_FIELD, String.class );
        final SimpleFeatureType shapeSchema = builder.buildFeatureType();
        
        // retyped collection
        final SimpleFeatureBuilder fb = new SimpleFeatureBuilder( shapeSchema );
        final String timestamp = timestampFormat().format( new Date() );
        FeatureCollection<SimpleFeatureType,SimpleFeature> retyped = 
                new RetypingFeatureCollection<SimpleFeatureType,SimpleFeature>( src, shapeSchema ) {

                    protected SimpleFeature retype( SimpleFeature feature ) {
                        for (AttributeDescriptor attrType : shapeSchema.getAttributeDescriptors()) {
                            Object value = feature.getAttribute( attrType.getName() );
                            fb.set( attrType.getName(), value );
                        }
                        fb.set( ORIG_FID_FIELD, feature.getID() );
                        // FIXME this should be the proper global timestamp from FeatureChangeTracker;
                        // otherwise each and every re-generate of the shapfile causes (wrong) exceptions
                        // on save; see ShapefileContainer for more info
                        fb.set( TIMESTAMP_FIELD, timestamp );
                        return fb.buildFeature( feature.getID() );
                    }
        };
        
        ShapefileDataStoreFactory shapeFactory = new ShapefileDataStoreFactory();

        Map<String,Serializable> params = new HashMap<String,Serializable>();
        params.put( "url", newFile.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ShapefileDataStore shapeDs = (ShapefileDataStore)shapeFactory.createNewDataStore( params );
        shapeDs.createSchema( shapeSchema );

        //shapeDs.forceSchemaCRS(DefaultGeographicCRS.WGS84);
        //shapeDs.setStringCharset( )
        
        // write shapefile
//        Transaction tx = new DefaultTransaction( "create" );

        String typeName = shapeDs.getTypeNames()[0];
        FeatureStore<SimpleFeatureType,SimpleFeature> shapeFs 
                = (FeatureStore<SimpleFeatureType, SimpleFeature>)shapeDs.getFeatureSource( typeName );

        // doing this without tx saves a LOT of memory; and tx is not
        // strictly necessary here
//        shapeFs.setTransaction( tx );
//        try {
            shapeFs.addFeatures( retyped );
//            tx.commit();
//        } 
//        catch (IOException e) {
//            tx.rollback();
//            throw e;
//        } 
//        finally {
//            tx.close();
//        }
        
        return newFile;
    }
    
}
