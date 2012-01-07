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
package org.polymap.core.data.operations.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.data.util.RetypingFeatureCollection;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapeExportFeaturesOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( ShapeExportFeaturesOperation.class );

    /** The file extensions generated with standard settings. */
    public static final String[]            FILE_SUFFIXES = {"shp", "shx", "qix", "fix", "dbf", "prj"};
    

    public Status execute( final IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(),
                context.features().size() );
  
        // complex type?
        FeatureCollection features = context.features();
        if (!(features.getSchema() instanceof SimpleFeatureType)) {
            throw new UnsupportedOperationException( "Complex features are not supported yet.");
        }
        
        SimpleFeatureType srcSchema = (SimpleFeatureType)features.getSchema();
        
        // shapeSchema
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName( FilenameUtils.normalize( srcSchema.getTypeName() ) );
        ftb.setCRS( srcSchema.getCoordinateReferenceSystem() );
        
        // attributes
        final Map<String,String> nameMap = new HashMap();
        for (AttributeDescriptor attr : srcSchema.getAttributeDescriptors()) {
            // attribute name (shapefile: 10 max)
            String targetName = StringUtils.left( attr.getLocalName(), 10 );
            for (int i=1; nameMap.containsValue( targetName ); i++) {
                targetName = StringUtils.left( attr.getLocalName(), 10-(i/10+1) ) + i;
                log.info( "    Shapefile: " + attr.getLocalName() + " -> " + targetName );
            }
            nameMap.put( attr.getLocalName(), targetName );
            
            ftb.add( targetName, attr.getType().getBinding() );
        }
        final SimpleFeatureType shapeSchema = ftb.buildFeatureType();
        
        // retyped collection
        final SimpleFeatureBuilder fb = new SimpleFeatureBuilder( shapeSchema );
        FeatureCollection<SimpleFeatureType,SimpleFeature> retyped = 
            new RetypingFeatureCollection<SimpleFeatureType,SimpleFeature>( features, shapeSchema ) {
                private int count = 0;
                protected SimpleFeature retype( SimpleFeature feature ) {
                    if (monitor.isCanceled()) {
                        throw new RuntimeException( "Operation canceled." );
                    }
                    for (Property prop : feature.getProperties()) {
                        Object value = prop.getValue();
                        // Shapefile has length limit 254
                        if (value instanceof String) {
                            value = StringUtils.abbreviate( (String)value, 254 );
                        }
                        fb.set( nameMap.get( prop.getName().getLocalPart() ), value );
                    }
                    if (++count % 100 == 0) {
                        monitor.worked( 100 );
                        monitor.subTask( "Objekte: " + count );
                    }
                    return fb.buildFeature( feature.getID() );
                }
            };
        
        ShapefileDataStoreFactory shapeFactory = new ShapefileDataStoreFactory();

        Map<String,Serializable> params = new HashMap<String,Serializable>();
        final String basename = shapeSchema.getTypeName();
        final File shapefile = File.createTempFile( basename+"-", ".shp" );
        shapefile.deleteOnExit();
        params.put( "url", shapefile.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ShapefileDataStore shapeDs = (ShapefileDataStore)shapeFactory.createNewDataStore( params );
        shapeDs.createSchema( shapeSchema );

        //shapeDs.forceSchemaCRS(DefaultGeographicCRS.WGS84);
        //shapeDs.setStringCharset( )
        
        String typeName = shapeDs.getTypeNames()[0];
        FeatureStore<SimpleFeatureType,SimpleFeature> shapeFs 
                = (FeatureStore<SimpleFeatureType, SimpleFeature>)shapeDs.getFeatureSource( typeName );

        // no tx needed; without tx saves alot of memory
        shapeFs.addFeatures( retyped );
        
        // open download        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                String url = DownloadServiceHandler.registerContent( new ContentProvider() {

                    public String getContentType() {
                        return "application/zip";
                    }

                    public String getFilename() {
                        return basename + ".shp.zip";
                    }

                    public InputStream getInputStream() throws Exception {
                        ByteArrayOutputStream bout = new ByteArrayOutputStream( 1024 * 1024 );
                        ZipOutputStream zipOut = new ZipOutputStream( bout );

                        for (String fileSuffix : FILE_SUFFIXES) {
                            zipOut.putNextEntry( new ZipEntry( basename + "." + fileSuffix ) );
                            File f = new File( shapefile.getParent(), StringUtils.substringBefore( shapefile.getName(), "." ) + "." + fileSuffix );
                            InputStream in = new BufferedInputStream( new FileInputStream( f ) ); 
                            IOUtils.copy( in, zipOut );
                            in.close();
                            f.delete();
                        }
                        zipOut.close();
                        return new ByteArrayInputStream( bout.toByteArray() );
                    }

                    public void done( boolean success ) {
                        // all files deleted in #getInputStream()
                    }
                });
                
                log.info( "Shapefile: download URL: " + url );
                ExternalBrowser.open( "download_window", url,
                        ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
            }
        });
        monitor.done();
        return Status.OK;
    }


    public Status undo( IProgressMonitor monitor )
    throws Exception {
        return Status.OK;
    }


    public Status redo( IProgressMonitor monitor )
    throws Exception {
        return Status.OK;
    }


    public boolean canExecute() {
        return true;
    }

    public boolean canRedo() {
        return false;
    }

    public boolean canUndo() {
        return false;
    }
    
}
