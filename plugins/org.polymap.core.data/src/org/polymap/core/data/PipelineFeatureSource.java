/*
 * polymap.org
 * Copyright (C) 2009-2018 Polymap GmbH. All rights reserved.
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
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
 * @see <a href="http://docs.geotools.org/maintenance/userguide/tutorial/datastore/index.html">Tutorial</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PipelineFeatureSource
        extends ContentFeatureStore {
        //implements FeatureStore<SimpleFeatureType,SimpleFeature> {

    private static final Log log = LogFactory.getLog( PipelineFeatureSource.class );

    /** XXX defaults to {@link DepthFirstStackExecutor}. */
    @Mandatory
    public Config2<PipelineFeatureSource,Class<? extends PipelineExecutor>>  pipelineExecutor;
    
    private Pipeline                pipeline;

    private Transaction             tx = Transaction.AUTO_COMMIT;

    private SessionContext          sessionContext = SessionContext.current();


    public PipelineFeatureSource( ContentEntry entry, Pipeline pipeline ) {
        super( entry, null );
        ConfigurationFactory.inject( this );
        this.pipeline = pipeline;
        this.pipelineExecutor.set( DepthFirstStackExecutor.class );
    }

    @Override
    protected boolean canReproject() {
        return true;
    }

    @Override
    protected boolean canLimit() {
        return true;
    }

    @Override
    protected boolean canOffset() {
        return true;
    }

    @Override
    protected boolean canFilter() {
        return true;
    }

    @Override
    protected boolean canRetype() {
        return true;
    }

    @Override
    protected boolean canSort() {
        return true;
    }

    @Override
    protected boolean canTransact() {
        return true;
    }

    @Override
    protected boolean canEvent() {
        return true;
    }

    @Override
    protected boolean canLock() {
        return true;
    }

    public Pipeline pipeline() {
        return pipeline;
    }
    
    public PipelineFeatureSource setPipeline( Pipeline pipeline ) {
        this.pipeline = pipeline;
        return this;
    }
    
    protected PipelineExecutor newExecutor() throws InstantiationException, IllegalAccessException {
        return pipelineExecutor.get().newInstance();
    }
    
    
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        // FIXME caching schema is not allowed since the pipeline and/or processors
        // may change; maybe we could add a listener API to the pipeline
    
        // avoid synchronize; doing this in parallel is ok
        GetFeatureTypeRequest request = new GetFeatureTypeRequest();
        try {
            final FeatureType[] type = new FeatureType[1];
            newExecutor().execute( pipeline, request, (ProcessorResponse r) -> {
                GetFeatureTypeResponse response = (GetFeatureTypeResponse)r;
                type[0] = response.getFeatureType();
            });
            return (SimpleFeatureType)type[0];
        }
        catch (Exception e) {
            Throwables.propagateIfInstanceOf( e, IOException.class );
            throw Throwables.propagate( e );
        }
    }


    @Override
    @SuppressWarnings( "hiding" )
    protected ReferencedEnvelope getBoundsInternal( Query query ) throws IOException {
        try {
            GetBoundsRequest request = new GetBoundsRequest( query );
            AtomicReference<ReferencedEnvelope> result = new AtomicReference();
            newExecutor().execute( pipeline, request, (GetBoundsResponse r) -> {
                result.set( r.bounds.get() );
            });
            return result.get();
        }
        catch (Exception e) {
            Throwables.propagateIfInstanceOf( e, IOException.class );
            throw Throwables.propagate( e );
        }
    }

    
    @Override
    @SuppressWarnings( "hiding" )
    protected int getCountInternal( Query query ) throws IOException {
        GetFeaturesSizeRequest request = new GetFeaturesSizeRequest( query );
        try {
            final int[] result = new int[1];
            newExecutor().execute( pipeline, request, (ProcessorResponse r) -> {
                GetFeaturesSizeResponse response = (GetFeaturesSizeResponse)r;
                result[0] = response.getSize();
            });
            return result[0];
        }
        catch (Exception e) {
            Throwables.propagateIfInstanceOf( e, IOException.class );
            throw Throwables.propagate( e );
        }
    }


    @Override
    @SuppressWarnings( "hiding" )
    protected FeatureReader<SimpleFeatureType,SimpleFeature> getReaderInternal( Query query ) throws IOException {
        log.debug( "query= " + query );
        //assert query.getFilter() != null : "No filter in query.";
        if (query.getFilter() == null) {
            log.warn( "Filter is NULL -> changing to EXCLUDE to prevent unwanted loading of all features! Use INCLUDE to get all." );
            query = new Query( query );
            query.setFilter( Filter.EXCLUDE );
        }
        return new PipelineAsyncFeatureReader( this, query, sessionContext );
    }


    /**
     * Called by {@link SyncPipelineFeatureCollection} to fetch feature chunks.
     */
    @SuppressWarnings( "hiding" )
    protected void fetchFeatures( Query query, final FeatureResponseHandler handler ) throws Exception {
        try {
            log.debug( "fetchFeatures(): maxFeatures= " + query.getMaxFeatures() );
            GetFeaturesRequest request = new GetFeaturesRequest( query );

            newExecutor().execute( pipeline, request, new ResponseHandler() {
                public void handle( ProcessorResponse r ) throws Exception {
                    GetFeaturesResponse response = (GetFeaturesResponse)r;
                    handler.handle( response.getFeatures() );
                }
            });
        }
        finally {
            handler.endOfResponse();
        }
    }


    /**
     * Handler interface for #fetchFeatures.
     */
    protected interface FeatureResponseHandler {

        public void handle( List<Feature> features ) throws Exception;

        public void endOfResponse() throws Exception;
    }


    @Override
    @SuppressWarnings( "hiding" )
    protected FeatureWriter<SimpleFeatureType,SimpleFeature> getWriterInternal( Query query, int flags )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    

//    /**
//     * Called by {@link SyncPipelineFeatureCollection} to determine the size
//     * of the result collection of the given query.
//     */
//    protected int getFeaturesSize( Query query ) {
//        GetFeaturesSizeRequest request = new GetFeaturesSizeRequest( query );
//        try {
//            final int[] result = new int[1];
//            createExecutor().execute( pipeline, request, (ProcessorResponse r) -> {
//                    GetFeaturesSizeResponse response = (GetFeaturesSizeResponse)r;
//                    result[0] = response.getSize();
//            });
//            return result[0];
//        }
//        catch (RuntimeException e) {
//            throw e;
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }
//
//
//    public void addFeatureListener( FeatureListener l ) {
//        store.listeners.addFeatureListener( this, l );
//    }
//
//    public void removeFeatureListener( FeatureListener l ) {
//        store.listeners.removeFeatureListener( this, l );
//    }
//
//
//    // FeatureStore ***************************************
//
//    @Override
//    public void setTransaction( Transaction tx ) {
//        try {
//            TransactionRequest request = new TransactionRequest( tx );
//            createExecutor().execute( pipeline, request, (ProcessorResponse r) -> {});
//            
//            this.tx = tx;
//            if (tx != Transaction.AUTO_COMMIT) {
//                if (tx.getState( store ) == null) {
//                    tx.putState( store, new TransactionState() );
//                }
//            }
//        }
//        catch (RuntimeException e) {
//            throw e;
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }
//
//    
//    /**
//     * 
//     */
//    class TransactionState
//            implements State {
//        @Override
//        public void commit() throws IOException {
//            store.listeners.fireEvent( getName().getLocalPart(), tx, 
//                    new FeatureEvent( PipelineFeatureSource.this, Type.COMMIT, null, null ) );
//        }
//        @Override
//        public void rollback() throws IOException {
//            store.listeners.fireEvent( getName().getLocalPart(), tx, 
//                    new FeatureEvent( PipelineFeatureSource.this, Type.ROLLBACK, null, null ) );
//        }
//        @Override
//        public void setTransaction( Transaction transaction ) {
//        }
//        @Override
//        public void addAuthorization( String AuthID ) throws IOException {
//        }
//    }
//
//    
//    public List<FeatureId> addFeatures( FeatureCollection features ) throws IOException {
//        return addFeatures( features, new NullProgressListener() );
//    }
//
//
//    public List<FeatureId> addFeatures( FeatureCollection<SimpleFeatureType,SimpleFeature> features,
//            final ProgressListener monitor )
//            throws IOException {
//        monitor.started();
//
////        FeatureType schema = getSchema();
////        if (!schema.equals( features.getSchema() )) {
////            log.warn( "addFeatures(): Given features have different schema - performing retype..." );
////            features = new ReTypingFeatureCollection( features, (SimpleFeatureType)schema );
////        }
//        final FeatureCollection fc = features;
//        // build a Collection that pipes the features through its Iterator; so
//        // the features don't need to be loaded in memory all together; and no
//        // chunks are needed; and events are sent correctly
//        Collection coll = new AbstractCollection() {
//            private volatile int size = -1;
//            public int size() {
//                return size < 0 ? size = fc.size() : size;
//            }
//            public Iterator iterator() {
//                return new Iterator() {
//                    private FeatureIterator it = fc.features();
//                    private int count = 0;
//                    @Override
//                    public boolean hasNext() {
//                        if (it != null && !it.hasNext()) {
//                            it.close();
//                            it = null;
//                            return false;
//                        }
//                        else {
//                            return true;
//                        }
//                    }
//                    @Override
//                    public Object next() {
//                        if ((++count % 100) == 0) {
//                            monitor.setTask( new SimpleInternationalString( "" + count ) );
//                            monitor.progress( 100 );
//                        }
//                        return it.next();
//                    }
//                    @Override
//                    public void remove() {
//                        throw new UnsupportedOperationException();
//                    }
//                    @Override
//                    protected void finalize() throws Throwable {
//                        if (it != null) {
//                            it.close();
//                            it = null;
//                        }
//                    }
//                };
//            }
//        };
//
//        try {
//            final List<FeatureId> fids = new ArrayList( 1024 );
//            // request
//            AddFeaturesRequest request = new AddFeaturesRequest( features.getSchema(), coll );
//            createExecutor().execute( pipeline, request, (ProcessorResponse r) -> 
//                    fids.addAll( ((ModifyFeaturesResponse)r).getFeatureIds() )
//            );
//
//            // fire event
//            store.listeners.fireFeaturesAdded( getSchema().getTypeName(), tx, null, false );
//
//            monitor.complete();
//            return fids;
//        }
//        catch (RuntimeException e) {
//            throw e;
//        }
//        catch (IOException e) {
//            throw e;
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }
//
//
//    public void removeFeatures( Filter filter ) throws IOException {
//        try {
//            // request
//            RemoveFeaturesRequest request = new RemoveFeaturesRequest( filter );
//            final ModifyFeaturesResponse[] response = new ModifyFeaturesResponse[1];
//            createExecutor().execute( pipeline, request, (ProcessorResponse r) -> {
//                    response[0] = (ModifyFeaturesResponse)r;
//            });
//            // fire event
//            store.listeners.fireFeaturesRemoved( getSchema().getTypeName(), tx, null, false );
//            return;
//        }
//        catch (RuntimeException e) {
//            throw e;
//        }
//        catch (IOException e) {
//            throw e;
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }
//
//
//    @Override
//    public void modifyFeatures( Name[] attributeNames, Object[] attributeValues, Filter filter ) throws IOException {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }
//
//
//    @Override
//    public void modifyFeatures( Name attributeName, Object attributeValue, Filter filter ) throws IOException {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }
//
//
//    public void modifyFeatures( AttributeDescriptor type, Object value, Filter filter ) throws IOException {
//        modifyFeatures( new AttributeDescriptor[] { type }, new Object[] { value }, filter );
//    }
//
//
//    public void modifyFeatures( AttributeDescriptor[] type, Object[] value, Filter filter ) throws IOException {
//        try {
//            // request
//            ModifyFeaturesRequest request = new ModifyFeaturesRequest( type, value, filter );
//            final ModifyFeaturesResponse[] response = new ModifyFeaturesResponse[1];
//            createExecutor().execute( pipeline, request, (ProcessorResponse r) ->
//                    response[0] = (ModifyFeaturesResponse)r
//            );
//            // fire event
//            log.debug( "Event: type=" + getName().getLocalPart() );
//            store.listeners.fireFeaturesChanged( getName().getLocalPart(), tx, null, false );
////            store.listeners.fireEvent( getName().getLocalPart(), tx,
////                    new FeatureEvent( this, FeatureEvent.Type.CHANGED, null, filter ) );
//            return;
//        }
//        catch (RuntimeException e) {
//            throw e;
//        }
//        catch (IOException e) {
//            throw e;
//        }
//        catch (Exception e) {
//            throw new IOException( e );
//        }
//    }
//
//
//    public void setFeatures( FeatureReader reader ) throws IOException {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }

}
