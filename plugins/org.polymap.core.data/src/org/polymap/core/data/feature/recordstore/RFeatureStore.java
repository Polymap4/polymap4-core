/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.feature.recordstore;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.IRecordStore.Updater;

/**
 * A {@link FeatureStore} based on the {@link IRecordStore} API.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RFeatureStore
        extends AbstractFeatureSource
        implements FeatureStore {

    private static Log log = LogFactory.getLog( RFeatureStore.class );

    protected RDataStore                ds;
    
    protected FeatureType               schema;
    

    protected RFeatureStore( RDataStore ds, FeatureType schema ) {
        this.ds = ds;
        this.schema = schema;
    }


    public FeatureType getSchema() {
        return schema;
    }


    public DataStore getDataStore() {
        return (DataStore)ds;
    }

    
    public QueryCapabilities getQueryCapabilities() {
        return ds.queryDialect.getQueryCapabilities();
    }


    public ReferencedEnvelope getBounds( Query query )
    throws IOException {
        return ds.queryDialect.getBounds( this, query );
    }


    public int getCount( Query query )
    throws IOException {
        return ds.queryDialect.getCount( this, query );
    }


    public FeatureCollection getFeatures( Query query )
    throws IOException {
        return new RFeatureCollection( this, schema, query, ds.queryDialect );
    }


    public void addFeatureListener( FeatureListener listener ) {
        ds.listeners.addFeatureListener( this, listener );
    }

    public void removeFeatureListener( FeatureListener listener ) {
        ds.listeners.addFeatureListener( this, listener );
    }

    
    /**
     * Factory for new, empty {@link RFeature} instances. The newly created
     * features needs to be added via {@link #addFeatures(FeatureCollection)}
     * in order to get persistently stored in this store.
     *
     * @return Newly created, empty feature instance.
     */
    public RFeature newFeature() {
        IRecordState state = ds.store.newRecord();
        return new RFeature( state, schema );
    }
    
    
    // FeatureStore ***************************************
    
    public List addFeatures( FeatureCollection features )
    throws IOException {
        final List<FeatureId> fids = new ArrayList();

        // FIXME auto commit!?
        final Updater tx = ds.getStore().prepareUpdate();
        try {
            features.accepts( new FeatureVisitor() {
                public void visit( Feature feature ) {
                    //assert feature instanceof RFeature : "Added features must be RFeatures. See RFeatureStore#newFeature().";
                    try {
                        // RFeature
                        if (feature instanceof RFeature) {
                            tx.store( ((RFeature)feature).state );    
                            fids.add( feature.getIdentifier() );
                        }
                        // SimpleFeature -> convert
                        else if (feature instanceof SimpleFeature) {
                            RFeature newFeature = newFeature();
                            for (Property prop : feature.getProperties()) {
                                newFeature.getProperty( prop.getName() ).setValue( prop.getValue() );
                            }
                            tx.store( newFeature.state );    
                            fids.add( newFeature.getIdentifier() );
                        }
                        else {
                            throw new UnsupportedOperationException( "Added features must be instance of RFeature or SimpleFeature" );
                        }
                    }
                    catch (Exception e) {
                        log.warn( "", e );
                    }
                }
            }, null );
            tx.apply();
        }
        catch (IOException e) {
            tx.discard();
            throw e;
        }
        catch (Throwable e) {
            tx.discard();
            throw new RuntimeException( e );
        }
        return fids;
    }


//    /**
//     * Handles <b>null</b> values and <b>collection</b> values. Delegates handling of
//     * single values to {@link #writeSingleValue(IRecordState, StoreKey, Property)}.
//     */
//    protected void write( IRecordState state, StoreKey basekey, Property prop ) {
//        StoreKey key = basekey.appendProperty( prop.getName().getLocalPart() );
//        // null
//        if (prop.getValue() == null) {
//            assert prop.getDescriptor().isNillable();
//            state.remove( key.toString() );
//        }
//        // collection
//        else if (prop.getValue() instanceof Collection) {
//            assert prop.getDescriptor().getMaxOccurs() != 1;
//            int index = 0;
//            for (Object elm : (Collection)prop.getValue()) {
//                writeSingleValue( state, key.appendCollectionIndex( index++ ), prop, elm );
//            }
//            state.put( key.appendCollectionLength().toString(), index );
//        }
//        // single value
//        else {            
//            writeSingleValue( state, key, prop, prop.getValue() );
//        }
//    }
//
//
//    /**
//     * Handles different property types.
//     */
//    protected void writeSingleValue( IRecordState state, StoreKey basekey, Property prop, Object value ) {
//        StoreKey key = basekey.appendProperty( prop.getName().getLocalPart() );
//
//        // complex (check more special types first!)
//        if (prop instanceof ComplexAttribute) {
//            ComplexAttribute complex = (ComplexAttribute)prop;
//            for (Property childProp : complex.getProperties()) {
//                write( state, key, childProp );
//            }
//        }
//        // attribute
//        else if (prop instanceof Attribute) {
//            state.put( key.toString(), value );
//        }
//        else {
//            throw new RuntimeException( "Unhandled property type: " + prop.getClass() );
//        }
//    }
    
    
    @Override
    public void modifyFeatures( AttributeDescriptor[] type, Object[] value, Filter filter )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void modifyFeatures( AttributeDescriptor type, Object value, Filter filter )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void removeFeatures( Filter filter )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setFeatures( FeatureReader reader )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    public Transaction getTransaction() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void setTransaction( Transaction transaction ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
