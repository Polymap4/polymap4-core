/* 
 * polymap.org
 * Copyright (C) 2011-2018, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.unitofwork;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.io.IOException;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.event.EventManager;

/**
 * UnitOfWork is a more model-driven, session-oriented API to work with FeatureStore
 * and friends.
 * <p/>
 * A UnitOfWork tracks modifications of a set of {@link Feature} instances. Modified
 * features are held in memory until all modifications are committed in a single
 * {@link Transaction}. On commit it automatically finds modified properties and
 * updates {@link FeatureStore} accordingly.
 * <p/>
 * <b>Usage:</b>
 * <ul>
 * <li>create instance for a given {@link FeatureStore}</li>
 * <li>{@link #track(Feature)} features
 * <li>modify features via {@link Property#setValue(Object)}</li>
 * <li>{@link #prepare(IProgressMonitor) commit} modifications to underlying store
 * </li>
 * <li>handle {@link ConcurrentModificationException}</li>
 * </ul>
 * <p/>
 * <b>Concurrent modifications</b> are checked against the original copy of the
 * features that the buffer is providing via {@link FeatureBufferState}. This
 * strategy might be memory consuming but it is also a robust way to check concurrent
 * changes that does not depend on a timestamp in the data type.
 * <p/>
 * <b>Events</b> of type {@link FeatureEvent.Type#COMMIT} and
 * {@link FeatureEvent.Type#ROLLBACK} are passed to {@link EventManager}. This makes
 * it easy to get informed about commit/rollback.
 * <p/>
 * TODO Automatically track features requested from (Pipeline)FeatureStore.
 * <p/>
 * TODO Optimized write back logic for {@link RFeatureStore}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UnitOfWork 
        implements AutoCloseable, FeatureListener {

    /* 
     * XXX Currently the original state of an modified feature is requested from the
     * underlying store not before the modification request arrives. This may lead to
     * <b>lost updates</b> if the feature has changed meanwhile. In order to detect
     * <b>every</b> modification the {@link FeatureBufferProcessor} presets the timestamp
     * in the feature and {@link FeatureBufferState#timestamp()} recognizes this.
     * {@link FeatureStateTracker} should be able to detect concurrent modification now.
     * Not fully tested.
     */

    private static final Log log = LogFactory.getLog( UnitOfWork.class );

    public static final FilterFactory   ff = CommonFactoryFinder.getFilterFactory( null );

    
    // instance *******************************************
    
    /** The underlying {@link FeatureStore}. */
    private FeatureStore                        fs;
    
    /**
     * Holds modified features in memory.
     * <p/>
     * Later we may change this into a {@link Cache} on top of a backing
     * {@link FeatureStore} in order to avoid holding the entiry working set in
     * memory.
     */
    private Map<FeatureId,FeatureBufferState>   modified = new ConcurrentHashMap( 128 );
    
    private Transaction                         tx;
    
    private long                                layerTimestamp;


    public UnitOfWork( FeatureStore fs ) {
        assert fs != null : "Param must not be null: fs";
        this.fs = fs;
        this.layerTimestamp = System.currentTimeMillis();
        
        fs.addFeatureListener( this );
    }

    
    @Override
    public void close() throws Exception {
        fs.removeFeatureListener( this );
    }


    @Override
    protected void finalize() throws Throwable {
        close();
    }


    @Override
    public void changed( FeatureEvent ev ) {
        EventManager.instance().publish( ev );
    }


    public void track( Feature feature ) {
        assert fs.getSchema().equals( feature.getType() );
        assert !modified.containsKey( feature.getIdentifier() );
        
        modified.computeIfAbsent( feature.getIdentifier(), key -> FeatureBufferState.forModified( feature ) );
    }
    
    
//    protected void fireFeatureChangeEvent( FeatureChangeEvent.Type type, Collection<Feature> features ) {
//        FidSet fids = new FidSet( features.size() * 2 );
//        for (Feature feature : features) {
//            fids.add( feature.getIdentifier() );
//        }
//        FeatureChangeEvent ev = new FeatureChangeEvent( layer, type, fids );
//        EventManager.instance().publish( ev );
//    }
    
    
    public long getLayerTimestamp() {
        return layerTimestamp;
    }

    
    protected String getLabel() {
        return "UnitOfWork";
    }

    /**
     * Starts a new {@link Transaction} for the underlying {@link FeatureStore}, 
     * checks concurrent modifications and updates {@link FeatureStore}.
     *
     * @param monitor
     * @throws IOException 
     * @throws {@link ConcurrentModificationException} 
     */
    public void prepare( IProgressMonitor monitor ) throws ConcurrentModificationException, IOException {
        assert tx == null : "Pending transaction found.";
        if (modified.isEmpty()) {
            return;
        }
        monitor.beginTask( "Prepare commit: " + getLabel(), modified.size() );

        // set transaction
        tx = new DefaultTransaction( getClass().getSimpleName() + "-" + hashCode() );
        fs.setTransaction( tx );
    
        DefaultFeatureCollection added = new DefaultFeatureCollection();
        Set<Identifier> removed = new HashSet();
    
        int count = 0;
        for (FeatureBufferState buffered : modified.values()) {
            if (monitor.isCanceled()) {
                return;
            }
            if ((++count % 100) == 0) {
                monitor.subTask( "(" + count + ")" );
            }
            if (buffered.isAdded()) {
                // no check if fid was created already since it is propably the 'primary key'
                added.add( (SimpleFeature)buffered.feature() );
            }
            else if (buffered.isModified()) {
                checkSubmitModified( buffered );
            }
            else if (buffered.isRemoved()) {
                removed.add( buffered.feature().getIdentifier() );
            }
            else {
                log.warn( "Buffered feature is not added/removed/modified!" );
            }
            monitor.worked( 1 );
        }

        if (!added.isEmpty()) {
            fs.addFeatures( added );
            monitor.worked( added.size() );
        }
        
        if (!removed.isEmpty()) {
            fs.removeFeatures( ff.id( removed ) );
            monitor.worked( added.size() );
        }
        monitor.done();
    }


    /**
     * Commits a previously {@link #prepare(IProgressMonitor)} transaction.
     *
     * @param monitor
     * @throws IOException
     */
    public void commit( IProgressMonitor monitor ) throws IOException {
        assert tx != null : "Call prepare() before commit()!";
        monitor.beginTask( getLabel() + " commit" , 1 );

        try {
            tx.commit();
            modified.clear();
        }
        finally {
            tx.close();
            tx = null;
        }
        monitor.done();
    }


    public void rollback( IProgressMonitor monitor ) throws IOException {
        assert tx != null : "Call prepare() before commit()!";
        monitor.beginTask( getLabel() + " commit" , 1 );
        
        try {
            tx.rollback();
        }
        finally {
            tx.close();
            tx = null;

            monitor.done();
        }
    }


//    public void revert( IProgressMonitor monitor ) {
//        revert( Filter.INCLUDE, monitor );
//    }
//
//
//    /**
//     *
//     * @param filter Specifies what features to revert. null for all features.
//     * @param monitor
//     */
//    public void revert( Filter filter, IProgressMonitor monitor ) {
//        assert filter != null;
//        monitor.beginTask( getLabel() + " revert", modified.size() );
//
//        List<Feature> reverted = new ArrayList( modified.size() );
//        
//        for (FeatureBufferState buffered : modified.values()) {
//            if (filter.evaluate( buffered.original() )) {
//                buffer.unregisterFeatures( Collections.singletonList( buffered.feature() ) );
//                reverted.add( buffered.feature() );
//            }
//            monitor.worked( 1 );
//        }
//        fireFeatureChangeEvent( FeatureChangeEvent.Type.FLUSHED, reverted );
//
//        monitor.done();
//    }
    

    /**
     * Check concurrent modifications with the store.
     */
    protected void checkSubmitModified( FeatureBufferState buffered ) throws ConcurrentModificationException, IOException {
        FeatureId fid = buffered.feature().getIdentifier();
        Id fidFilter = ff.id( Collections.singleton( fid ) );
        
        // check concurrent modification
        Feature[] stored = (Feature[])fs.getFeatures( fidFilter ).toArray( new Feature[1] );         
        if (stored.length == 0) {
            throw new ConcurrentModificationException( "Feature has been removed concurrently: " + fid );
        }
        else if (stored.length > 1) {
            throw new IllegalStateException( "More than one feature for id: " + fid + "!?" );
        }
        if (isFeatureModified( stored[0], buffered.original() )) {
            throw new ConcurrentModificationException( "Objekt wurde von einem anderen Nutzer gleichzeitig geändert: " + fid );
        }
        
        // write down
        AttributeDescriptor[] type = {};
        Object[] value = {};
        
        for (Property origProp : buffered.original().getProperties()) {
            if (origProp.getDescriptor() instanceof AttributeDescriptor) {
                Property newProp = buffered.feature().getProperty( origProp.getName() );
                if (isPropertyModified( origProp.getValue(), newProp.getValue() )) {
                    type = (AttributeDescriptor[])ArrayUtils.add( type, origProp.getDescriptor() );
                    value = ArrayUtils.add( value, newProp.getValue() );
                    
                    log.info( "Attribute modified: " + origProp.getDescriptor().getName() + " = " + newProp.getValue() + " (" + fid.getID() + ")" );
                }
            }
        }
        fs.modifyFeatures( type, value, fidFilter );
    }

    
    private boolean isFeatureModified( Feature feature, Feature original ) throws IOException {
        // XXX complex features
        SimpleFeatureType schema = ((SimpleFeature)original).getType(); 
        for (AttributeDescriptor attribute : schema.getAttributeDescriptors()) {
            
            Object value1 = ((SimpleFeature)feature).getAttribute( attribute.getName() );
            Object value2 = ((SimpleFeature)original).getAttribute( attribute.getName() );
            
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
    
}
