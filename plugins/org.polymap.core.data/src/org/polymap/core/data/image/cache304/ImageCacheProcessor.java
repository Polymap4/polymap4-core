/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.image.cache304;

import static org.polymap.core.project.ILayer.PROP_GEORESID;
import static org.polymap.core.project.ILayer.PROP_STYLE;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetLayerTypesRequest;
import org.polymap.core.data.image.GetLayerTypesResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.entity.EntityStateEvent;
import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class ImageCacheProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( ImageCacheProcessor.class );


    private static final ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class},
            new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    
    // instance *******************************************

    private ILayer                  layer;
    
    private Properties              props;
    
    private LayerListener           layerListener;
    
    private volatile boolean        active = true;
    
    
    public void init( Properties _props ) {
        props = _props;
        layer = (ILayer)props.get( "layer" );
        assert layer != null;
        active = !layer.getEditable();

        layerListener = new LayerListener( layer, this );
    }


    @Override
    protected void finalize() throws Throwable {
        layerListener.dispose();
    }


    protected void deactivate() {
        if (active) {
            active = false;
            layer.setEditable( true );
            log.debug( "CACHE deactivated for layer: " + layer.getLabel() );
        }
    }

    
    protected void updateAndActivate( boolean updateCache ) {
        log.debug( "CACHE: activating for layer: " + layer.getLabel() );
        if (!active) {
            if (updateCache) {
                Cache304.instance().updateLayer( layer, null );
            }
            active = true;
            layer.setEditable( false );
        }
    }

    
    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        // active?
        if (!active) {
            context.sendRequest( r );
            return;            
        }
        
        // GetMapRequest
        if (r instanceof GetMapRequest) {
            getMapRequest( (GetMapRequest)r, context );
        }
        // GetLegendGraphicRequest
        else if (r instanceof GetLegendGraphicRequest) {
            context.sendRequest( r );
        }
        // GetLayerTypes
        else if (r instanceof GetLayerTypesRequest) {
            context.sendRequest( r );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }

    
    protected void getMapRequest( GetMapRequest request, ProcessorContext context )
    throws Exception {
        
        Timer timer = new Timer();
        CachedTile cachedTile = Cache304.instance().get( request, context.getLayers(), props );
        
        if (cachedTile != null) {
            // check If-Modified-Since
            long modifiedSince = request.getIfModifiedSince();
            long lastModified = cachedTile.lastModified.get();
            if (modifiedSince > 0 && lastModified > modifiedSince) {
                log.debug( "### CACHE: 304! :) -- " + timer.elapsedTime() + "ms" );
                context.sendResponse( EncodedImageResponse.NOT_MODIFIED );
                context.sendResponse( ProcessorResponse.EOP );
            }
            // in cache but modified
            else {
                byte[] data = cachedTile.data.get();
                log.debug( "### CACHE: Hit. (" + data.length + " bytes) -- " + timer.elapsedTime() + "ms" );
                EncodedImageResponse response = new EncodedImageResponse( data, data.length );
                response.setLastModified( cachedTile.lastModified.get() );
                response.setExpires( cachedTile.expires.get() );
                context.sendResponse( response );
                context.sendResponse( ProcessorResponse.EOP );
            }
        }
        
        // not in cache -> send request down the pipeline 
        else {
            log.debug( "### CACHE: Miss. (...) -- " + timer.elapsedTime() + "ms" );
            ByteArrayOutputStream cacheBuf = new ByteArrayOutputStream( 128*1024 );
            context.put( "cacheBuf", cacheBuf );
            context.put( "request", request );
            context.put( "created", System.currentTimeMillis() );
            context.sendRequest( request );
        }
    }


    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        // active?
        if (!active) {
            context.sendResponse( r );
            return;            
        }

        ByteArrayOutputStream cacheBuf = (ByteArrayOutputStream)context.get( "cacheBuf" ); 
        
        // EncodedImageResponse
        if (r instanceof EncodedImageResponse) {
            EncodedImageResponse response = (EncodedImageResponse)r;
            response.setLastModified( (Long)context.get( "created" ) );
            context.sendResponse( response );
            
            cacheBuf.write( response.getChunk(), 0, response.getChunkSize() );
        }
        // EOP
        else if (r == ProcessorResponse.EOP) {
            GetMapRequest request = (GetMapRequest)context.get( "request" );
            if (cacheBuf.size() > 0) {
                Cache304.instance().put( 
                        request, context.getLayers(), cacheBuf.toByteArray(),
                        (Long)context.get( "created" ), props );
            }
            else {
                log.warn( "Empty response buf! -> not stored in Cache." );
            }

            context.sendResponse( ProcessorResponse.EOP );
            //log.debug( "...all data sent." );
        }
        // GetLayerTypesResponse
        else if (r instanceof GetLayerTypesResponse) {
            context.sendResponse( r );
        }
        else {
            throw new IllegalStateException( "Unhandled response type: " + r );
        }
    }


    /**
     * Static listener class with weak reference the processor to allow GC to reclaim
     * the processor.
     */
    static class LayerListener {
    
        /** The property names of ILayer that forces the cache to deactivate. */
        private static final Set<String>    layerModProps = Sets.newHashSet( PROP_STYLE, PROP_GEORESID, ILayer.PROP_PROCS );
                
        private ILayer                      layer;
        
        private WeakReference<ImageCacheProcessor>  procRef;
        
        private Set<FeatureId>              modified = new HashSet();
        
        
        public LayerListener( ILayer layer, ImageCacheProcessor processor ) {
            this.layer = layer;
            this.procRef = new WeakReference( processor );
            
            EventManager.instance().subscribe( this, new EventFilter<EventObject>() {
                public boolean apply( EventObject ev ) {
                    // EntityStateEvent
                    if (ev instanceof EntityStateEvent) {
                        EntityStateEvent eev = (EntityStateEvent)ev;
                        return eev.getEventType() == EntityStateEvent.EventType.COMMIT; //ev.isMySession();
                    }
                    // FeatureChangeEvent
                    else if (ev instanceof FeatureChangeEvent
                            && ev.getSource() instanceof ILayer
                            && ((ILayer)ev.getSource()).id().equals( LayerListener.this.layer.id() )) {
                        return true;
                    }
                    // PropertyChangeEvent: ILayer
                    else if (ev instanceof PropertyChangeEvent
                            && ev.getSource() instanceof ILayer
                            && ((ILayer)ev.getSource()).id().equals( LayerListener.this.layer.id() )
                            && layerModProps.contains( ((PropertyChangeEvent)ev).getPropertyName() )) {
                        return true;
                    }
                    return false;
                }
            });
        }
        
        public void dispose() {
            EventManager.instance().unsubscribe( this );
            LayerListener.this.layer = null;            
        }
        
        @EventHandler(scope=Event.Scope.Session)
        protected void changesCommitted( EntityStateEvent ev ) {
            ImageCacheProcessor proc = procRef.get();
            if (proc != null) {
                // for entity features this check does not work
                // if (ev.getSource() == LayerListener.this.layer) {
                
                if (!proc.active) {
                    // XXX if we are not active and some entities are committed, 
                    // then this is probable a general save
                    proc.updateAndActivate( true );
                    modified.clear();

                    // does not work properly for entity features
//                    // XXX so we guess based on the committed fids ...
//                    Set<String> committed = new HashSet();
//                    for (EntityHandle handle : ev) {
//                        committed.add( handle.id() );
//                    }
//                    for (FeatureId fid : modified) {
//                        if (committed.contains( fid.getID() ) ) {
//                            proc.updateAndActivate( true );
//                            modified.clear();
//                            return;
//                        }
//                    }
                }
            }
            else {
                dispose();
            }
        }
        
        /** A feature of the layer has changed. */
        @EventHandler
        protected void featuresChanged( FeatureChangeEvent ev ) {
            ImageCacheProcessor proc = procRef.get();
            if (proc == null) {
                dispose();
            }
            else {
                // just activate update if changes have been dropped
                if (ev.getType() == FeatureChangeEvent.Type.FLUSHED) {
                    proc.updateAndActivate( false );
                }
                // deactivate cache when features have been modified
                // FIXME there is a race cond. with MapEditor.reloadLayer()
                else {
                    proc.deactivate();
                    modified.addAll( ev.getFids() );
                }
            }
        }                        

        /** 
         * The style or geores or another attribute of the layer has changed.
         * <p/>
         * The cache managed style information for each tile, so it is
         * not strictly necessary to deactivate the cache. However, it
         * seems to be the "right" way to do it.
         */
        @EventHandler
        protected void layerChanged( PropertyChangeEvent ev ) {
            ImageCacheProcessor proc = procRef.get();
            if (proc == null) {
                dispose();
            }
            else {
                // ClearCacheAction uses layer.setStyle( layer.getStyle() ) to reload layer
                // check this and don't disable in this case
                if (!ev.getPropertyName().equals( ILayer.PROP_RERENDER )) {
                    proc.deactivate();
                }
            }
        }                        
    }
        
}
