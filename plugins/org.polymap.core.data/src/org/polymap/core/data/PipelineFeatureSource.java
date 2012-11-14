/*
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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
import java.util.Set;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.util.ProgressListener;

import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.geotools.util.SimpleInternationalString;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

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
import org.polymap.core.data.pipeline.DefaultPipelineIncubator;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.runtime.SessionContext;

/**
 * This <code>FeatureSource</code> provides the features of an {@link ILayer}
 * (its underlaying {@link IService}), processed by the layer specific
 * {@link Pipeline}, instantiated for use-case {@link LayerUseCase#FEATURES}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class PipelineFeatureSource
        extends AbstractFeatureSource
        implements FeatureStore<SimpleFeatureType, SimpleFeature> {

    private static final Log log = LogFactory.getLog( PipelineFeatureSource.class );

    private static DefaultPipelineIncubator pipelineIncubator = new DefaultPipelineIncubator();


    // static factory *************************************

    /**
     * Instantiates a new pipelined {@link FeatureSource} for the given layer.
     * <p>
     * This method may block execution while accessing the back-end service.
     * 
     * @return The newly created <code>FeatureSource</code>, or null if no
     *         {@link FeatureSource} could be created for this layer because its is a
     *         raster layer or no appropriate processors could be found.
     * @throws PipelineIncubationException
     * @throws IOException
     * @throws IllegalStateException If the geo resource for the given layer could
     *         not be find.
     */
    public static PipelineFeatureSource forLayer( ILayer layer, boolean transactional )
    throws PipelineIncubationException, IOException {
        // find service for layer
        log.debug( "layer: " + layer + ", label= " + layer.getLabel() + ", visible= " + layer.isVisible() );

        IGeoResource res = layer.getGeoResource();
        if (res == null) {
            throw new IllegalStateException( "Unable to find geo resource of layer: " + layer );
        }
        IService service = res.service( null );
        log.debug( "service: " + service );

        // create pipeline
        LayerUseCase useCase = transactional
                ? LayerUseCase.FEATURES_TRANSACTIONAL
                : LayerUseCase.FEATURES;
        Pipeline pipe = pipelineIncubator.newPipeline( useCase, layer.getMap(), layer, service );

        return pipe.length() > 0
                // create FeatureSource
                ? new PipelineFeatureSource( pipe )
                : null;
    }


    // instance *******************************************

    private Pipeline            pipeline;

    private PipelineDataStore   store;

    private Transaction         tx = Transaction.AUTO_COMMIT;

    private SessionContext      sessionContext = SessionContext.current();


    public PipelineFeatureSource( Pipeline pipeline ) {
        super();
        this.pipeline = pipeline;
        // FIXME this has to be unique instance per session
        this.store = new PipelineDataStore( this );
    }


    public DataStore getDataStore() {
        return store;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public ILayer getLayer() {
        Set<ILayer> layers = getPipeline().getLayers();
        assert layers.size() == 1;
        return layers.iterator().next();
    }


    public ReferencedEnvelope getBounds( Query query )
            throws IOException {
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


    public int getCount( Query query )
            throws IOException {
        return getFeaturesSize( query );
    }


    public SimpleFeatureType getSchema() {
        // XXX caching schema is not allowed since the pipeline and/or processors
        // may change; maybe we could add a listener API to the pipeline

        // avoid synchronize; doing this in parallel is ok
        GetFeatureTypeRequest request = new GetFeatureTypeRequest();
        try {
            final FeatureType[] type = new FeatureType[1];
            pipeline.process( request, new ResponseHandler() {
                public void handle( ProcessorResponse r )
                throws Exception {
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


    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures( Query query )
    throws IOException {
        log.debug( "query= " + query );
        //assert query.getFilter() != null : "No filter in query.";
        if (query.getFilter() == null) {
            log.warn( "Filter is NULL -> changing to EXCLUDE to prevent unwanted loading of all features! Use INCLUDE to get all." );
            query = new DefaultQuery( query );
            ((DefaultQuery)query).setFilter( Filter.EXCLUDE );
        }
        return new AsyncPipelineFeatureCollection( this, query, sessionContext );
    }


    /**
     * Called by {@link SyncPipelineFeatureCollection} to fetch feature chunks.
     */
    protected void fetchFeatures( Query query, final FeatureResponseHandler handler )
    throws Exception {
        try {
            log.debug( "fetchFeatures(): maxFeatures= " + query.getMaxFeatures() );
            GetFeaturesRequest request = new GetFeaturesRequest( query );

            pipeline.process( request, new ResponseHandler() {
                public void handle( ProcessorResponse r )
                throws Exception {
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
            pipeline.process( request, new ResponseHandler() {
                public void handle( ProcessorResponse r )
                throws Exception {
                    GetFeaturesSizeResponse response = (GetFeaturesSizeResponse)r;
                    result[0] = response.getSize();
                }
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

    public void setTransaction( Transaction transaction ) {
        log.warn( "PipelinedFeatureSource: no transaction support as updates are bufferd by LayerFeatureBufferManager!" );
    }


    public List<FeatureId> addFeatures( FeatureCollection<SimpleFeatureType, SimpleFeature> features )
    throws IOException {
        return addFeatures( features, new NullProgressListener() );
    }


    public List<FeatureId> addFeatures( final FeatureCollection<SimpleFeatureType, SimpleFeature> features,
            final ProgressListener monitor )
            throws IOException {
        monitor.started();

        FeatureType type = features.getSchema();
        try {
            // build a Collection that pipes the features through it Iterator; so
            // the features don't need to be loaded in memory al together; and no
            // chunks are needed; and events are sent correctly
            Collection coll = new AbstractCollection() {
                private int size = -1;
                public int size() {
                    return size < 0 ? size = features.size() : size;
                }
                public Iterator iterator() {
                    return new Iterator() {
                        private FeatureIterator it = features.features();
                        private int count = 0;
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
                        public Object next() {
                            if ((++count % 100) == 0) {
                                monitor.setTask( new SimpleInternationalString( "" + count ) );
                                monitor.progress( 100 );
                            }
                            return it.next();
                        }
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                        protected void finalize() throws Throwable {
                            if (it != null) {
                                it.close();
                                it = null;
                            }
                        }
                    };
                }
            };

            final List<FeatureId> fids = new ArrayList( 1024 );

            // request
            AddFeaturesRequest request = new AddFeaturesRequest( type, coll );
            pipeline.process( request, new ResponseHandler() {
                public void handle( ProcessorResponse r )
                throws Exception {
                    fids.addAll( ((ModifyFeaturesResponse)r).getFeatureIds() );
                }
            });

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
        finally {
//            it.close();
        }
    }


    public void removeFeatures( Filter filter )
    throws IOException {
        try {
            // request
            RemoveFeaturesRequest request = new RemoveFeaturesRequest( filter );
            final ModifyFeaturesResponse[] response = new ModifyFeaturesResponse[1];
            pipeline.process( request, new ResponseHandler() {
                public void handle( ProcessorResponse r )
                throws Exception {
                    response[0] = (ModifyFeaturesResponse)r;
                }
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


    public void modifyFeatures( AttributeDescriptor type, Object value, Filter filter )
    throws IOException {
        modifyFeatures( new AttributeDescriptor[] { type, }, new Object[] { value, }, filter );
    }


    public void modifyFeatures( AttributeDescriptor[] type, Object[] value, Filter filter )
    throws IOException {
        try {
            // request
            ModifyFeaturesRequest request = new ModifyFeaturesRequest( type, value, filter );
            final ModifyFeaturesResponse[] response = new ModifyFeaturesResponse[1];
            pipeline.process( request, new ResponseHandler() {
                public void handle( ProcessorResponse r )
                throws Exception {
                    response[0] = (ModifyFeaturesResponse)r;
                }
            });
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


    public void setFeatures( FeatureReader<SimpleFeatureType, SimpleFeature> reader )
    throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
