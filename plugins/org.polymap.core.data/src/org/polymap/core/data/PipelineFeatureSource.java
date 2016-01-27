/*
 * polymap.org
 * Copyright (C) 2009-2013 Polymap GmbH. All rights reserved.
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
package org.polymap.core.data;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.util.ProgressListener;
import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureEvent.Type;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.geotools.util.SimpleInternationalString;

import org.polymap.core.data.feature.AddFeaturesRequest;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
import org.polymap.core.data.feature.ModifyFeaturesRequest;
import org.polymap.core.data.feature.ModifyFeaturesResponse;
import org.polymap.core.data.feature.RemoveFeaturesRequest;
import org.polymap.core.data.feature.TransactionRequest;
import org.polymap.core.data.pipeline.DepthFirstStackExecutor;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineExecutor;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.session.SessionContext;

/**
 * This <code>FeatureSource</code> provides the features of an {@link ILayer}
 * (its underlaying {@link IService}), processed by the layer specific
 * {@link Pipeline}, instantiated for use-case {@link LayerUseCase#FEATURES}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("deprecation")
public class PipelineFeatureSource
        extends AbstractFeatureSource
        implements FeatureStore<SimpleFeatureType,SimpleFeature> {

    private static final Log log = LogFactory.getLog( PipelineFeatureSource.class );

    /** XXX defaults to {@link DepthFirstStackExecutor}. */
    @Mandatory
    public Config2<PipelineFeatureSource,PipelineExecutor>  pipelineExecutor;
    
    private Pipeline                pipeline;

    private PipelineDataStore       store;

    private Transaction             tx = Transaction.AUTO_COMMIT;

    private SessionContext          sessionContext = SessionContext.current();


    public PipelineFeatureSource( Pipeline pipeline ) {
        ConfigurationFactory.inject( this );
        this.pipeline = pipeline;
        this.pipelineExecutor.set( new DepthFirstStackExecutor() );
        
        // FIXME this has to be unique instance per session
        this.store = new PipelineDataStore( this );
    }


    public DataStore getDataStore() {
        return store;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }


    @Override
    public ReferencedEnvelope getBounds( Query query ) throws IOException {
        // XXX optimize getBounds via dedicated request
        log.info( "XXX: getBounds: iterating over collection!" );
        ReferencedEnvelope result = new ReferencedEnvelope();
        FeatureIterator<SimpleFeature> it = getFeatures( query ).features();
        try {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                result.include( feature.getBounds() );
            }
        }
        finally {
            it.close();
        }
        return result;
    }


    @Override
    public int getCount( Query query )
            throws IOException {
        return getFeaturesSize( query );
    }


    @Override
    public SimpleFeatureType getSchema() {
        // XXX caching schema is not allowed since the pipeline and/or processors
        // may change; maybe we could add a listener API to the pipeline

        // avoid synchronize; doing this in parallel is ok
        GetFeatureTypeRequest request = new GetFeatureTypeRequest();
        try {
            final FeatureType[] type = new FeatureType[1];
            pipelineExecutor.get().execute( pipeline, request, new ResponseHandler() {
                public void handle( ProcessorResponse r ) throws Exception {
                    GetFeatureTypeResponse response = (GetFeatureTypeResponse)r;
                    type[0] = response.getFeatureType();
                }
            });
            return (SimpleFeatureType)type[0];
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public SimpleFeatureCollection getFeatures( Query query ) throws IOException {
        log.debug( "query= " + query );
        //assert query.getFilter() != null : "No filter in query.";
        if (query.getFilter() == null) {
            log.warn( "Filter is NULL -> changing to EXCLUDE to prevent unwanted loading of all features! Use INCLUDE to get all." );
            query = new Query( query );
            query.setFilter( Filter.EXCLUDE );
        }
        return new AsyncPipelineFeatureCollection( this, query, sessionContext );
    }


    /**
     * Called by {@link SyncPipelineFeatureCollection} to fetch feature chunks.
     */
    protected void fetchFeatures( Query query, final FeatureResponseHandler handler ) throws Exception {
        try {
            log.debug( "fetchFeatures(): maxFeatures= " + query.getMaxFeatures() );
            GetFeaturesRequest request = new GetFeaturesRequest( query );

            pipelineExecutor.get().execute( pipeline, request, new ResponseHandler() {
                public void handle( ProcessorResponse r ) throws Exception {
                    GetFeaturesResponse response = (GetFeaturesResponse)r;
                    handler.handle( response.getFeatures() );
                }
            });
        }
//        catch (RuntimeException e) {
//            throw e;
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
        finally {
            handler.endOfResponse();
        }
    }


    protected interface FeatureResponseHandler {

        public void handle( List<Feature> features )
        throws Exception;

        public void endOfResponse()
        throws Exception;
    }


    /**
     * Called by {@link SyncPipelineFeatureCollection} to determine the size
     * of the result collection of the given query.
     */
    protected int getFeaturesSize( Query query ) {
        GetFeaturesSizeRequest request = new GetFeaturesSizeRequest( query );
        try {
            final int[] result = new int[1];
            pipelineExecutor.get().execute( pipeline, request, (ProcessorResponse r) -> {
                    GetFeaturesSizeResponse response = (GetFeaturesSizeResponse)r;
                    result[0] = response.getSize();
            });
            return result[0];
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public void addFeatureListener( FeatureListener l ) {
        store.listeners.addFeatureListener( this, l );
    }

    public void removeFeatureListener( FeatureListener l ) {
        store.listeners.removeFeatureListener( this, l );
    }


    // FeatureStore ***************************************

    @Override
    public void setTransaction( Transaction tx ) {
        try {
            TransactionRequest request = new TransactionRequest( tx );
            pipelineExecutor.get().execute( pipeline, request, (ProcessorResponse r) -> {});
            
            this.tx = tx;
            if (tx != Transaction.AUTO_COMMIT) {
                if (tx.getState( store ) == null) {
                    tx.putState( store, new TransactionState() );
                }
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    /**
     * 
     */
    class TransactionState
            implements State {
        @Override
        public void commit() throws IOException {
            store.listeners.fireEvent( getName().getLocalPart(), tx, 
                    new FeatureEvent( PipelineFeatureSource.this, Type.COMMIT, null, null ) );
        }
        @Override
        public void rollback() throws IOException {
            store.listeners.fireEvent( getName().getLocalPart(), tx, 
                    new FeatureEvent( PipelineFeatureSource.this, Type.ROLLBACK, null, null ) );
        }
        @Override
        public void setTransaction( Transaction transaction ) {
        }
        @Override
        public void addAuthorization( String AuthID ) throws IOException {
        }
    }

    
    public List<FeatureId> addFeatures( FeatureCollection features ) throws IOException {
        return addFeatures( features, new NullProgressListener() );
    }


    public List<FeatureId> addFeatures( FeatureCollection<SimpleFeatureType,SimpleFeature> features,
            final ProgressListener monitor )
            throws IOException {
        monitor.started();

//        FeatureType schema = getSchema();
//        if (!schema.equals( features.getSchema() )) {
//            log.warn( "addFeatures(): Given features have different schema - performing retype..." );
//            features = new ReTypingFeatureCollection( features, (SimpleFeatureType)schema );
//        }
        final FeatureCollection fc = features;
        // build a Collection that pipes the features through its Iterator; so
        // the features don't need to be loaded in memory all together; and no
        // chunks are needed; and events are sent correctly
        Collection coll = new AbstractCollection() {
            private volatile int size = -1;
            public int size() {
                return size < 0 ? size = fc.size() : size;
            }
            public Iterator iterator() {
                return new Iterator() {
                    private FeatureIterator it = fc.features();
                    private int count = 0;
                    @Override
                    public boolean hasNext() {
                        if (it != null && !it.hasNext()) {
                            it.close();
                            it = null;
                            return false;
                        }
                        else {
                            return true;
                        }
                    }
                    @Override
                    public Object next() {
                        if ((++count % 100) == 0) {
                            monitor.setTask( new SimpleInternationalString( "" + count ) );
                            monitor.progress( 100 );
                        }
                        return it.next();
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    protected void finalize() throws Throwable {
                        if (it != null) {
                            it.close();
                            it = null;
                        }
                    }
                };
            }
        };

        try {
            final List<FeatureId> fids = new ArrayList( 1024 );
            // request
            AddFeaturesRequest request = new AddFeaturesRequest( features.getSchema(), coll );
            pipelineExecutor.get().execute( pipeline, request, (ProcessorResponse r) -> 
                    fids.addAll( ((ModifyFeaturesResponse)r).getFeatureIds() )
            );

            // fire event
            store.listeners.fireFeaturesAdded( getSchema().getTypeName(), tx, null, false );

            monitor.complete();
            return fids;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public void removeFeatures( Filter filter ) throws IOException {
        try {
            // request
            RemoveFeaturesRequest request = new RemoveFeaturesRequest( filter );
            final ModifyFeaturesResponse[] response = new ModifyFeaturesResponse[1];
            pipelineExecutor.get().execute( pipeline, request, (ProcessorResponse r) -> {
                    response[0] = (ModifyFeaturesResponse)r;
            });
            // fire event
            store.listeners.fireFeaturesRemoved( getSchema().getTypeName(), tx, null, false );
            return;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public void modifyFeatures( Name[] attributeNames, Object[] attributeValues, Filter filter ) throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void modifyFeatures( Name attributeName, Object attributeValue, Filter filter ) throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void modifyFeatures( AttributeDescriptor type, Object value, Filter filter ) throws IOException {
        modifyFeatures( new AttributeDescriptor[] { type }, new Object[] { value }, filter );
    }


    public void modifyFeatures( AttributeDescriptor[] type, Object[] value, Filter filter ) throws IOException {
        try {
            // request
            ModifyFeaturesRequest request = new ModifyFeaturesRequest( type, value, filter );
            final ModifyFeaturesResponse[] response = new ModifyFeaturesResponse[1];
            pipelineExecutor.get().execute( pipeline, request, (ProcessorResponse r) ->
                    response[0] = (ModifyFeaturesResponse)r
            );
            // fire event
            log.debug( "Event: type=" + getName().getLocalPart() );
            store.listeners.fireFeaturesChanged( getName().getLocalPart(), tx, null, false );
//            store.listeners.fireEvent( getName().getLocalPart(), tx,
//                    new FeatureEvent( this, FeatureEvent.Type.CHANGED, null, filter ) );
            return;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e );
        }
    }


    public void setFeatures( FeatureReader reader ) throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
