/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
import java.util.concurrent.Callable;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.IRecordStore.Updater;

/**
 * A {@link FeatureStore} based on the {@link IRecordStore} API.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class RFeatureStore
        extends AbstractFeatureSource
        implements FeatureStore {

    private static Log log = LogFactory.getLog( RFeatureStore.class );

    public static final FilterFactory   ff = CommonFactoryFinder.getFilterFactory( null );
    
    protected RDataStore                ds;
    
    protected FeatureType               schema;
    
    protected Transaction               tx = Transaction.AUTO_COMMIT;
    
    /** The tranaction state if {@link #tx} == {@link Transaction#AUTO_COMMIT}. */
    protected TransactionState          txState = new TransactionState();
    

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


    public RFeatureCollection getFeatures( Query query )
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
        return schema instanceof SimpleFeatureType
                ? new RSimpleFeature( state, schema )
                : new RFeature( state, schema );
    }
    
    
    // FeatureStore ***************************************
    
    public List addFeatures( FeatureCollection features )
    throws IOException {
        final List<FeatureId> fids = new ArrayList();

        try {
            startModification();
            features.accepts( new FeatureVisitor() {
                public void visit( Feature feature ) {
                    //assert feature instanceof RFeature : "Added features must be RFeatures. See RFeatureStore#newFeature().";
                    try {
                        // RFeature
                        if (feature instanceof RFeature) {
                            txState.updater().store( ((RFeature)feature).state );    
                            fids.add( feature.getIdentifier() );
                        }
                        // SimpleFeature -> convert
                        else if (feature instanceof SimpleFeature) {
                            RFeature newFeature = newFeature();
                            for (Property prop : feature.getProperties()) {
                                newFeature.getProperty( prop.getName() ).setValue( prop.getValue() );
                            }
                            txState.updater().store( newFeature.state );
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
            completeModification( true );
        }
        catch (IOException e) {
            completeModification( false );
            throw e;
        }
        catch (Throwable e) {
            completeModification( false );
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
    public void modifyFeatures( AttributeDescriptor[] types, Object[] values, Filter filter )
    throws IOException {
        assert types != null && types.length > 0;
        assert values != null && values.length > 0 && types.length == values.length;
        
        try {
            startModification();
            
            RFeatureCollection features = getFeatures( new DefaultQuery( null, filter ) );
            for (RFeature feature : features) {
                for (int i=0; i<types.length; i++) {
//                  PropertyName xpath = ff. property( types[i].getLocalName() );
//                  Attribute attribute = xpath.evaluate( feature, Attribute.class );
//                  attribute.setValue( values[i] );
                    
                    String name = types[i].getLocalName();
                    // FIXME bug in SimpleFeaturePropertyAccessorFactory prevents prop name like aaa-bbb
//                    PropertyAccessor accessor = PropertyAccessors.findPropertyAccessor( 
//                            feature, name, Attribute.class, null );
//                    accessor.set( feature, types[i].getLocalName(), values[i], null );
                    if (feature instanceof SimpleFeature) {
                        if (Geometry.class.isAssignableFrom( types[i].getType().getBinding() ) ) {
                            ((SimpleFeature)feature).setDefaultGeometry( values[i] );
                        }
                        else {
                            ((SimpleFeature)feature).setAttribute( name, values[i] );
                        }
                    }
                    else {
                        throw new RuntimeException( "FIXME: bug in SimpleFeaturePropertyAccessorFactory prevents prop name like xxx-yyy" );
                    }
                    
                    txState.updater().store( feature.state );                    
                }
            }
            
            completeModification( true );
        }
        catch (IOException e) {
            completeModification( false );
            throw e;
        }
        catch (Throwable e) {
            log.warn( "", e );
            completeModification( false );
            throw new RuntimeException( e );
        }
    }

    
    @Override
    public void modifyFeatures( AttributeDescriptor type, Object value, Filter filter )
    throws IOException {
        modifyFeatures( new AttributeDescriptor[] {type}, new Object[] {value}, filter );
    }

    
    @Override
    public void removeFeatures( Filter filter ) throws IOException {
        try {
            startModification();
            
            RFeatureCollection features = getFeatures( new DefaultQuery( null, filter ) );
            for (RFeature feature : features) {
                txState.updater().remove( feature.state );                    
            }
            
            completeModification( true );
        }
        catch (IOException e) {
            completeModification( false );
            throw e;
        }
        catch (Throwable e) {
            log.warn( "", e );
            completeModification( false );
            throw new RuntimeException( e );
        }
    }

    
    @Override
    public void setFeatures( FeatureReader reader ) throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    protected void startModification() {
    }

    
    protected void completeModification( boolean success ) throws IOException {
        if (tx == Transaction.AUTO_COMMIT) {
            if (success) {
                txState.commit();
            } else {
                txState.rollback();
            }
        }
    }
    
    
    public Transaction getTransaction() {
        return tx;
    }


    public void setTransaction( Transaction tx ) {
//        if (tx != this.tx) {
//            this.tx.commit();
//        }
        this.tx = tx;
        this.txState = new TransactionState();
        if (tx != Transaction.AUTO_COMMIT) {
            this.tx.putState( this, txState );
        }
    }

    
    /**
     * 
     */
    class TransactionState
            implements State {

        private Updater             updater;

        public Updater updater() {
            if (updater == null) {
                updater = ds.getStore().prepareUpdate();
            }
            return updater;
        }
        
        @Override
        public void addAuthorization( String AuthID ) throws IOException {
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public void commit() throws IOException {
            if (updater != null) {
                updater.apply();
                updater = null;
            }
        }

        @Override
        public void rollback() throws IOException {
            if (updater != null) {
                updater.discard();
                updater = null;
            }            
        }

        @Override
        public void setTransaction( Transaction tx ) {
        }
        
    }

    
    /**
     * 
     */
    class CommitJob
            implements Callable {

        @Override
        public Object call() throws Exception {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
        
    }
    
}
