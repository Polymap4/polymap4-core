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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.SessionSingletonBase;

import org.eclipse.jface.dialogs.IPageChangedListener;

import org.eclipse.core.runtime.CoreException;

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
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
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
import org.polymap.core.data.feature.DataSourceProcessor;
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
import org.polymap.core.data.pipeline.IPipelineIncubationListener;
import org.polymap.core.data.pipeline.IPipelineIncubator;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.data.pipeline.PipelineListenerExtension;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.runtime.ListenerList;

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

    private static IPipelineIncubator pipelineIncubator = new DefaultPipelineIncubator();


    // static incubation listeners ************************

    private static Session      global = new Session();
    
    /*
     * 
     */
    static class Session
            extends SessionSingletonBase {

        private ListenerList<IPipelineIncubationListener> listeners = new ListenerList();

        public static Session instance() {
            try {
                return (Session)getInstance( Session.class );
            }
            catch (IllegalStateException e) {
                return global;
            }
        }

        protected Session() {
            for (PipelineListenerExtension ext : PipelineListenerExtension.allExtensions()) {
                try {
                    IPipelineIncubationListener listener = ext.newListener();
                    listeners.add( listener );
                }
                catch (CoreException e) {
                    log.error( "Unable to create a new IPipelineIncubationListener: " + ext.getId() );
                }
            }
        }
        
    }


    /**
     * Add a {@link IPageChangedListener} to the global list of listeners of this
     * session.
     */
    public static void addIncubationListener( IPipelineIncubationListener l ) {
        Session.instance().listeners.add( l );
    }
    
    public static void removeIncubationListener( IPipelineIncubationListener l ) {
        Session.instance().listeners.remove( l );
    }
    
    
    // static factory *************************************

    /**
     * <p>
     * This method may block execution while accessing the back-end service.
     *
     * @return The newly created <code>FeatureSource</code>.
     * @throws PipelineIncubationException
     * @throws IOException
     * @throws IllegalStateException If the geo resource for the given layer
     *         could not be find.
     */
    public static PipelineFeatureSource forLayer( ILayer layer, boolean transactional )
    throws PipelineIncubationException, IOException {
        // find service for layer
        log.debug( "layer: " + layer + ", label= " + layer.getLabel() + ", visible= " + layer.isVisible() );
//        IProgressMonitor monitor = JobMonitors.get();
//        monitor.subTask( "Find geo-resource of layer: " + layer.getLabel() );
        IGeoResource res = layer.getGeoResource();
        if (res == null) {
            throw new IllegalStateException( "Unable to find geo resource of layer: " + layer );
        }
        IService service = res.service( null );
        log.debug( "service: " + service );
//        monitor.worked( 1 );

        // create pipeline
        LayerUseCase useCase = transactional
                ? LayerUseCase.FEATURES_TRANSACTIONAL
                : LayerUseCase.FEATURES;
        Pipeline pipe = pipelineIncubator.newPipeline( useCase, layer.getMap(), layer, service );

        // call listeners
        for (IPipelineIncubationListener listener : Session.instance().listeners) {
            listener.pipelineCreated( pipe );
        }
        
        // create FeatureSource
        PipelineFeatureSource result = new PipelineFeatureSource( pipe );
        return result;
    }


    // instance *******************************************

    private Pipeline            pipeline;

    private PipelineDataStore   store;

    private Transaction         tx = Transaction.AUTO_COMMIT;


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
        return new AsyncPipelineFeatureCollection( this, query );
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
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public List<FeatureId> addFeatures( FeatureCollection<SimpleFeatureType, SimpleFeature> features )
    throws IOException {
        return addFeatures( features, new NullProgressListener() );
    }


    public List<FeatureId> addFeatures( FeatureCollection<SimpleFeatureType, SimpleFeature> features,
            ProgressListener monitor )
            throws IOException {
        monitor.started();
        int chunkSize = DataSourceProcessor.DEFAULT_CHUNK_SIZE;

        FeatureType type = features.getSchema();
        FeatureIterator it = features.features();
        int count = 0;
        try {
            final List<FeatureId> fids = new ArrayList( 1024 );
            while (it.hasNext() && !monitor.isCanceled()) {
                // chunk
                Collection<Feature> chunk = new ArrayList( chunkSize );
                for (int i=0; i<chunkSize && it.hasNext(); i++) {
                    chunk.add( it.next() );
                    count++;
                }
                log.info( "chunk red from source: " + chunk.size() );
                // request
                AddFeaturesRequest request = new AddFeaturesRequest( type, chunk );
                pipeline.process( request, new ResponseHandler() {
                    public void handle( ProcessorResponse r )
                    throws Exception {
                        fids.addAll( ((ModifyFeaturesResponse)r).getFeatureIds() );
                    }
                });
                log.info( "chunk sent down the pipe: " + chunk.size() );
                monitor.setTask( new SimpleInternationalString( "Objekte verarbeitet: " + count ) );
                monitor.progress( 1 );
            }

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
            it.close();
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
            log.info( "Event: type=" + getName().getLocalPart() );
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
