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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import edu.emory.mathcs.backport.java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.FeatureStoreListener;
import org.polymap.core.data.FeatureChangeTracker;
import org.polymap.core.data.FeatureStoreEvent;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.model.event.ModelChangeTracker;
import org.polymap.core.model.event.ModelHandle;
import org.polymap.core.model.event.ModelChangeTracker.Updater;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.UIJob;
import org.polymap.service.fs.spi.IContentSite;

/**
 * The container of the files of a shapefile used by the content file implementations
 * of the {@link ShapefileContentProvider}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ShapefileContainer
        extends FeatureStoreListener {
    
    private static Log log = LogFactory.getLog( ShapefileContainer.class );

    private static final FilterFactory  ff = CommonFactoryFinder.getFilterFactory( null );
    
    public static final long        UPDATE_JOB_DELAY = 5000;
    
    File                            file;
    
    /** The exception from last shapefile creation, or null if ok. */
    Exception                       exception;
    
    /** The exception from last update, or null if none. */
    Exception                       updateException;
    
    ILayer                          layer;
    
    Date                            lastModified = new Date();

    IContentSite                    site;
    
    private UpdateJob               updateJob;
    
    private ReentrantReadWriteLock  lock = new ReentrantReadWriteLock();
    
    private PipelineFeatureSource   layerFs;
    
    
    public ShapefileContainer( ILayer layer, IContentSite site ) {
        this.layer = layer;
        this.site = site;
        try {
            this.layerFs = PipelineFeatureSource.forLayer( layer, true );
            
            FeatureChangeTracker.instance().addFeatureListener( this );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public void featureChange( FeatureStoreEvent ev ) {
        log.info( "ev= " + ev );
        if (layer.id().equals( ev.getSource().id() )) {
            log.info( "flushing..." );
            flush();
        }
    }


    public void flush() {
        try {
            lock.writeLock().lock();

            if (file != null) {
                for (String fileSuffix : ShapefileGenerator.FILE_SUFFIXES) {
                    File f = resolveFile( fileSuffix );
                    FileUtils.deleteQuietly( f );
                }
                file = null;
                exception = null;
                lastModified = new Date();
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }
    

    public OutputStream getOutputStream( String fileSuffix )
    throws IOException {
        // FIXME locking?
        File f = Path.fromOSString( file.getAbsolutePath() )
                .removeFileExtension().addFileExtension( fileSuffix ).toFile();
        
        // check updateJob
        if (updateJob != null) {
            if (updateJob.getState() == Job.RUNNING) {
                throw new IOException( "Update Job is running, write is not allowed until finished." );
            }
            else if (updateJob.getState() == Job.WAITING || updateJob.getState() == Job.SLEEPING) {
                updateJob.cancel();
                updateJob.schedule( UPDATE_JOB_DELAY );
            }
        }
        else {
            updateJob = new UpdateJob();
            updateJob.schedule( UPDATE_JOB_DELAY );
        }
        
        return new FileOutputStream( f );
    }
    
    
    public InputStream getInputStream( String fileSuffix )
    throws IOException {
        // FIXME locking?
        // init shapefile
        getFileSize( fileSuffix );
        
        if (exception == null) {
            File f = Path.fromOSString( file.getAbsolutePath() )
                    .removeFileExtension().addFileExtension( fileSuffix ).toFile();
            return new FileInputStream( f );
        }
        else {
            throw (IOException)exception;
        }
    }
    

    public synchronized Long getFileSize( String fileSuffix ) {
        if (file == null) {
            try {
                exception = null;

                ShapefileGenerator generator = new ShapefileGenerator( layer, site );
                file = generator.writeShapefile( layerFs.getFeatures() );

                lastModified = new Date();
            }
            catch (Exception e) {
                log.warn( "", e );
                exception = e;
            }
        }
        if (exception == null) {
            return resolveFile( fileSuffix ).length();
        }
        else {
            return null;
        }
    }
    

    protected File resolveFile( String fileSuffix ) {
        return Path.fromOSString( file.getAbsolutePath() )
                .removeFileExtension().addFileExtension( fileSuffix ).toFile();
    }
    
    
    /**
     * Do the update on the underlying layer data store.
     * <ol>
     *   <li>make a copy of the orig shapefile</li>
     *   <li>find modifications</li>
     *   <li>write down to layer data store</li>
     * </ol>
     */
    class UpdateJob
            extends UIJob {

        private File        origDataDir;
        
        private DateFormat  timestampFormat = ShapefileGenerator.timestampFormat();


        public UpdateJob() 
        throws IOException {
            super( "ShapefileContainer.UpdateJob" );

            // make a copy of the original data
            origDataDir = new File( file.getParentFile(), "original" );
            if (origDataDir.exists()) {
                log.warn( "UpdateJob: 'original' data dir already exists: " + origDataDir.getAbsolutePath() );
            }
            else {
                log.debug( "UpdateJob: copying to: " + origDataDir.getAbsolutePath() );
                for (String fileSuffix : ShapefileGenerator.FILE_SUFFIXES) {
                    File src = resolveFile( fileSuffix );
                    File dest = new File( origDataDir, src.getName() );
                    FileUtils.copyFile( src, dest );
                }
            }
        }

        
        protected void runWithException( IProgressMonitor monitor )
        throws Exception {
            log.info( "UpdateJob: starting..." );
            lock.writeLock().lock();

            final Updater updater = ModelChangeTracker.instance().newUpdater();

            try {
                ShapefileDataStoreFactory shapeFactory = new ShapefileDataStoreFactory();

                // modifiedDs
                Map<String,Serializable> params = new HashMap<String,Serializable>();
                params.put( "url", file.toURI().toURL() );
                params.put( "create spatial index", Boolean.TRUE );

                ShapefileDataStore modifiedDs = shapeFactory.createDataStore( params );
                String typeName = modifiedDs.getTypeNames()[0];
                final FeatureSource<SimpleFeatureType, SimpleFeature> modifiedFs = modifiedDs.getFeatureSource( typeName );

                // origDs
                params = new HashMap<String,Serializable>();
                params.put( "url", new File( origDataDir, file.getName() ).toURI().toURL() );
                params.put( "create spatial index", Boolean.TRUE );

                ShapefileDataStore origDs = shapeFactory.createDataStore( params );
                final FeatureSource<SimpleFeatureType, SimpleFeature> origFs = origDs.getFeatureSource( typeName );

                // modifications
                final List<SimpleFeature> added = new ArrayList();
                final List<SimpleFeature[]> modified = new ArrayList();
                final List<SimpleFeature> removed = new ArrayList();

                // find added, modified
                final AtomicInteger newSize = new AtomicInteger( 0 );
                modifiedFs.getFeatures().accepts( new FeatureVisitor() {
                    public void visit( Feature candidate ) {
                        if (updateException != null) {
                            return;
                        }
                        newSize.incrementAndGet();
                        try {
                            Id fid = ff.id( Collections.singleton( candidate.getIdentifier() ) );
                            Object[] orig = origFs.getFeatures( fid ).toArray();
                            
                            if (orig.length == 0) {
                                log.info( "   Feature has been added: " + candidate.getIdentifier() );
                                added.add( (SimpleFeature)candidate );
                            }
                            else if (orig.length == 1
                                    && isFeatureModified( (SimpleFeature)candidate, (SimpleFeature)orig[0] )) {
                                log.info( "   Feature has been modified: " + candidate.getIdentifier() );
                                
                                // FIXME check timestamps; this was done to find out if the features received from client
                                // are based on the last created shapefile, otherwise we cannot figure what properties
                                // the client has actually changed; however, see ShapefileGenerator for correct
                                // creation of timestamps
//                                if (!isSameTimestamp( (SimpleFeature)candidate, (SimpleFeature)orig[0] )) {
//                                    throw new IllegalStateException( "Timestamps of features do not match." );
//                                }
                                
                                // update feature timestamp (check concurrent modifications within this JVM)
                                //ModelHandle key = FeatureChangeTracker.featureHandle( (Feature)orig[0] );
                                String id = (String)((SimpleFeature)candidate).getAttribute( ShapefileGenerator.ORIG_FID_FIELD );
                                String type = FeatureChangeTracker.MODEL_TYPE_PREFIX + layerFs.getSchema().getName().getLocalPart();
                                ModelHandle key = ModelHandle.instance( id, type );

                                Long timestamp = timestamp( (SimpleFeature)candidate );
                                updater.checkSet( key, timestamp, null );
                                
                                modified.add( new SimpleFeature[] { (SimpleFeature)candidate, (SimpleFeature)orig[0] } );
                            }
                        }
                        catch (IOException e) {
                            log.warn( "", e );
                        }
                        catch (Exception e) {
                            updateException = e;
                            log.warn( updateException.getLocalizedMessage(), updateException );
                        }
                    }
                }, null );

                // throw exception from FeaatureVisitor
                if (updateException != null) {
                    throw updateException;
                }
                
                // find removed
                FeatureCollection origFeatures = origFs.getFeatures();
                // check only if necessary
                if (newSize.get() != (origFeatures.size() - added.size())) {
                    origFeatures.accepts( new FeatureVisitor() {
                        public void visit( Feature candidate ) {
                            try {
                                Id fid = ff.id( Collections.singleton( candidate.getIdentifier() ) );
                                Object[] found = modifiedFs.getFeatures( fid ).toArray();

                                if (found.length == 0) {
                                    log.info( "   Feature has been removed: " + candidate.getIdentifier() );
                                    removed.add( (SimpleFeature)candidate );
                                }
                            }
                            catch (IOException e) {
                                log.warn( "", e );
                            }
                        }
                    }, null );
                }
                
                // write own modifications
                Transaction tx = new DefaultTransaction( layer.getLabel() + "-write-back" );
                try {

                    // added
                    if (!added.isEmpty()) {
                        FeatureCollection coll = FeatureCollections.newCollection();
                        coll.addAll( added );
                        layerFs.addFeatures( coll );
                    }
                
                    // removed
                    if (!removed.isEmpty()) {
                        Set<Identifier> removeIds = new HashSet();
                        for (SimpleFeature feature : removed) {
                            removeIds.add( feature.getIdentifier() );
                        }
                        layerFs.removeFeatures( ff.id( removeIds ) );
                    }

                    // modified
                    for (SimpleFeature[] feature : modified) {
                        AttributeDescriptor[] type = {};
                        Object[] value = {};

                        SimpleFeature orig = feature[1];
                        SimpleFeature modi = feature[0];
                        for (Property origProp : orig.getProperties()) {
                            if (origProp.getDescriptor() instanceof AttributeDescriptor) {
                                Name propName = origProp.getName();
                                if (!propName.getLocalPart().equals( ShapefileGenerator.TIMESTAMP_FIELD )) {
                                    Property newProp = modi.getProperty( propName );
                                    if (isPropertyModified( origProp.getValue(), newProp.getValue() )) {
                                        type = (AttributeDescriptor[])ArrayUtils.add( type, origProp.getDescriptor() );
                                        value = ArrayUtils.add( value, newProp.getValue() );

                                        log.info( "    Attribute modified: " + origProp.getDescriptor().getName() + " = " + newProp.getValue() + " (" + orig.getID() + ")" );
                                    }
                                }
                            }
                        }
                        String origFid = (String)orig.getAttribute( ShapefileGenerator.ORIG_FID_FIELD );
                        log.debug( "        fid: shape: " + orig.getID() + ", orig: " + origFid );
                        layerFs.modifyFeatures( type, value, ff.id( Collections.singleton( ff.featureId( origFid ) ) ) );
                    }
                    tx.commit();
                    
                    // apply timestamp updates
                    updater.apply( layer, true );
                    
                    log.debug( "    Transaction committed." );
                }
                catch (Exception e) {
                    log.warn( "    Transaction rolled back!" );
                    tx.rollback();
                    throw e;
                }
                finally {
                    tx.close();
                }
                
//                log.info( "   saving layer buffer..." );
//                LayerFeatureBufferManager buffer = LayerFeatureBufferManager.forLayer( layer, false );
//                buffer.prepareSave( null, new NullProgressMonitor() );
//                buffer.save( null, new NullProgressMonitor() );
                
            }
            finally {
                if (updater != null) {
                    updater.done();
                }

                // delete 'original' dir, even in case of exception; there is no other way
                log.debug( "    deleting: " + origDataDir.getAbsolutePath() );
                FileUtils.deleteDirectory( origDataDir );

                // re-create shapefile in order to update timestamps; otherwise next concurrent
                // modification check would fail; do this even on error so that client sees the
                // 'actual' state of the features
                flush();
                
                lock.writeLock().unlock();

                log.info( "UpdateJob: done." );
                updateJob = null;
            }
        }
        

        private boolean isFeatureModified( SimpleFeature feature, SimpleFeature original ) 
        throws IOException {
            SimpleFeatureType schema = original.getType(); 
            for (AttributeDescriptor attribute : schema.getAttributeDescriptors()) {
                
                Object value1 = feature.getAttribute( attribute.getName() );
                Object value2 = original.getAttribute( attribute.getName() );
                
                if (isPropertyModified( value1, value2 )) {
                    return true;
                }
            }
            return false;
        }

        
        private boolean isPropertyModified( Object value1, Object value2 ) {
            if (value1 instanceof Geometry) {
                if (!((Geometry)value1).equalsExact( (Geometry)value2 )) {
                    return true;
                }
            }
            else if ((value1 != null && !value1.equals( value2 ))
                    || value1 == null && value2 != null) {
                return true;
            }
            return false;
        }

    
        private boolean isSameTimestamp( SimpleFeature feature, SimpleFeature original ) {
            try {
                Long value1 = timestamp( feature ); //(String)feature.getAttribute( ShapefileGenerator.TIMESTAMP_FIELD );
                Long value2 = timestamp( original );  //(String)feature.getAttribute( ShapefileGenerator.TIMESTAMP_FIELD );
                return value1 != null && value2 != null && value1.equals( value2 );
            }
            catch (Exception e) {
                log.debug( "Exception while checking timestamps: " + e, e );
                return false;
            }
        }

        
        private Long timestamp( SimpleFeature feature )
        throws ParseException {
            String value = (String)feature.getAttribute( ShapefileGenerator.TIMESTAMP_FIELD );
            return value != null ? ((Date)timestampFormat.parseObject( value )).getTime() : null;
        }
    }
    
    
//    /**
//     * Encapsulates a modification to a single feature.
//     *
//     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
//     */
//    public abstract class FeatureModification {
//    
//        private String      name;
//        
//        private String      description;
//    
//        
//        public FeatureModification( String name, String description ) {
//            this.name = name;
//            this.description = description;
//        }
//
//        public abstract void perform( FeatureStore fs );
//        
//    }
    
}