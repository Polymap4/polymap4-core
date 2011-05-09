/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.data.feature.mdb;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.Serializable;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.internal.shp.ShpServiceExtension;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class MdbImportOperation
        extends AbstractModelChangeOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( MdbImportOperation.class );

    private Database            mdb;
    
    private String[]            tableNames;
    
    private IProgressMonitor    monitor;
    

    protected MdbImportOperation( Database mdb, String[] tableNames ) {
        super( "MS-Access importieren" );
        this.mdb = mdb;
        this.tableNames = tableNames;
    }

    
    protected IStatus doExecute( IProgressMonitor _monitor, IAdaptable info )
    throws Exception {
        this.monitor = _monitor;
        for (String tableName : tableNames) {
            importTable( mdb.getTable( tableName ) );
        }
        return Status.OK_STATUS;
    }
    
    
    protected void importTable( Table table ) 
    throws Exception {
        SubProgressMonitor sub = new SubProgressMonitor( monitor, table.getRowCount() );
        sub.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        
        // schema
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName( table.getName() );
        typeBuilder.setDefaultGeometry( "geom" );
        typeBuilder.add( "geom", Point.class, CRS.decode( "EPSG:31468" ) );
        
        for (Column col : table.getColumns()) {
            log.info( "    column: " + col.getName() );
            switch (col.getType()) {
                case MEMO:
                case TEXT: 
                    typeBuilder.add( col.getName(), String.class ); break;
                case BYTE:
                case INT:
                case LONG:
                    typeBuilder.add( col.getName(), Integer.class ); break;
                case FLOAT:
                    typeBuilder.add( col.getName(), Float.class ); break;
                case DOUBLE:
                    typeBuilder.add( col.getName(), Double.class ); break;
                case SHORT_DATE_TIME:
                    typeBuilder.add( col.getName(), Date.class ); break;
                default:
                    throw new RuntimeException( "Unhandled MDB data type: " + col.getType() );
            }
        }
        SimpleFeatureType schema = typeBuilder.buildFeatureType();
    
        // builder
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder( schema );
        
        // FeatureCollection
        FeatureCollection<SimpleFeatureType, SimpleFeature> coll = 
                FeatureCollections.newCollection();

        // data rows
        GeometryFactory gf = new GeometryFactory();
        Map<String, Object> row = null;
        while ((row = table.getNextRow()) != null) {
            double x = -1; 
            double y = -1;
            for (Map.Entry<String,Object> entry : row.entrySet()) {
                String name = entry.getKey();
                if (name.equalsIgnoreCase( "rechtswert" )) {
                    x = ((Number)entry.getValue()).doubleValue();
                }
                else if (name.equalsIgnoreCase( "hochwert" )) {
                    y = ((Number)entry.getValue()).doubleValue();
                }
                else {
                    builder.set( name, entry.getValue() );
                }
            }
            builder.set( "geom", gf.createPoint( new Coordinate( x, y ) ) );
            coll.add( builder.buildFeature( null ) );
            sub.worked( 1 );
        }
        
        // shapefile
        createShapeStore( schema, coll );
    }
    
    
    protected void createShapeStore( SimpleFeatureType schema, FeatureCollection coll )
    throws Exception {
        SubProgressMonitor sub = new SubProgressMonitor( monitor, 1 );

        ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();

        String shapeName = schema.getTypeName() + ".shp";
        sub.beginTask( "Shapefile: " + shapeName, 1 );

        File newFile = new File( Polymap.getWorkspacePath().toFile(), shapeName );
        DataStoreFactorySpi dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String,Serializable> params = new HashMap();
        params.put( "url", newFile.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ShapefileDataStore newDataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema( schema );
        //newDataStore.forceSchemaCRS( crs );
        //newDataStore.setStringCharset( Charset.forName( "ISO-8859-1" ) );

        // write the features to shape
        Transaction transaction = new DefaultTransaction( "create" );
        String typeName = newDataStore.getTypeNames()[0];
        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore =
                (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore.getFeatureSource( typeName );
        featureStore.setTransaction( transaction );
        try {
            featureStore.addFeatures( coll );
            transaction.commit();
            sub.worked( 1 );
        } 
        catch (Exception ee) {
            transaction.rollback();
            throw ee;
        } 
        finally {
            transaction.close();
        }

        // adding service to catalog
        ShpServiceExtension creator = new ShpServiceExtension();
        params = creator.createParams( newFile.toURI().toURL() );
        IService service = creator.createService( null, params );
        IServiceInfo info = service.getInfo( monitor );

        CatalogPlugin.getDefault().getLocalCatalog().add( service );

    }
    
}
