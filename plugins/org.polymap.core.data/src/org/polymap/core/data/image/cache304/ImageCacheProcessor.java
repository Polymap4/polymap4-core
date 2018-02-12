/* 
 * polymap.org
 * Copyright (C) 2010-2018, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.image.cache304;

import java.util.concurrent.Callable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.Duration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.polymap.core.data.image.EncodedImageProcessor;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.pipeline.EndOfProcessing;
import org.polymap.core.data.pipeline.Param;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.Timer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImageCacheProcessor
        extends EncodedImageProcessor {

    private static final Log log = LogFactory.getLog( ImageCacheProcessor.class );

    @Param.UI( description="The minimum time before re-requesting a tile from the source" )
    public static final Param<Duration> MIN_UPDATE_TIMEOUT = new Param( "minTimeout", Duration.class, Duration.ofHours( 24 ) );

    static Lazy<File>           cachedir;
    
    /**
     * Initialize the global cache store.
     */
    public static void init( Callable<File> cacheDirSupplier ) {
        assert cachedir == null : "cachedir is set already.";
        cachedir = new LockedLazyInit( () -> {
            try {
                return cacheDirSupplier.call();
            }
            catch (Exception e) {
                throw Throwables.propagate( e );
            }
        });
    }

    // instance *******************************************
    
    private PipelineProcessorSite   site;
    
    private Cache304                cache = Cache304.instance();
    
    @Override
    public void init( @SuppressWarnings( "hiding" ) PipelineProcessorSite site ) {
        this.site = site;
    }


//    protected void updateAndActivate( boolean updateCache ) {
//        log.debug( "CACHE: activating for layer: " + layer.getLabel() );
//        if (!active) {
//            if (updateCache) {
//                Cache304.instance().updateLayer( layer, null );
//            }
//            active = true;
//            layer.setEditable( false );
//        }
//    }

    
    @Override
    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception {
        Timer timer = new Timer();
        CachedTile cachedTile = cache.get( request );

        // cached
        if (cachedTile != null) {
            // check If-Modified-Since
            long modifiedSince = request.getIfModifiedSince();
            long lastModified = cachedTile.lastModified.get();
            if (modifiedSince > 0 && lastModified > modifiedSince) {
                log.info( "### CACHE: 304! :) -- " + timer.elapsedTime() + "ms" );
                context.sendResponse( EncodedImageResponse.NOT_MODIFIED );
                context.sendResponse( ProcessorResponse.EOP );
            }
            // in cache but modified
            else {
                byte[] data = cachedTile.data.get();
                log.info( "### CACHE: Hit (" + data.length + " bytes) -- " + timer.elapsedTime() + "ms" );
                EncodedImageResponse response = new EncodedImageResponse( data, data.length );
                response.setLastModified( cachedTile.lastModified.get() );
                response.setExpires( cachedTile.expires.get() );
                context.sendResponse( response );
                context.sendResponse( ProcessorResponse.EOP );
            }
        }
        
        // not in cache -> send request down the pipeline 
        else {
            log.info( "### CACHE: Miss -- " + timer.elapsedTime() + "ms" );
            ByteArrayOutputStream cacheBuf = new ByteArrayOutputStream( 128*1024 );
            context.put( "cacheBuf", cacheBuf );
            context.put( "request", request );
            context.put( "created", System.currentTimeMillis() );
            context.sendRequest( request );
        }
    }


    @Override
    public void encodedImageResponse( EncodedImageResponse response, ProcessorContext context ) throws Exception {
        ByteArrayOutputStream cacheBuf = (ByteArrayOutputStream)context.get( "cacheBuf" ); 
        response.setLastModified( (Long)context.get( "created" ) );
        context.sendResponse( response );
        cacheBuf.write( response.getChunk(), 0, response.getChunkSize() );
    }


    @Override
    public void endOfProcessing( EndOfProcessing eop, ProcessorContext context ) throws Exception {
        ByteArrayOutputStream cacheBuf = (ByteArrayOutputStream)context.get( "cacheBuf" ); 
        GetMapRequest request = (GetMapRequest)context.get( "request" );
        if (cacheBuf.size() > 0) {
            cache.put( request, cacheBuf.toByteArray(), context.get( "created" ), MIN_UPDATE_TIMEOUT.get( site ).toMillis() );
        }
        else {
            log.warn( "Empty response buf! -> not stored in Cache." );
        }

        context.sendResponse( ProcessorResponse.EOP );
        log.debug( "...all data sent." );
    }


//    /**
//     * Static listener class with weak reference the processor to allow GC to reclaim
//     * the processor.
//     */
//    static class LayerListener {
//    
//        /** The property names of ILayer that forces the cache to deactivate. */
//        private static final Set<String>    layerModProps = Sets.newHashSet( PROP_STYLE, PROP_GEORESID, ILayer.PROP_PROCS );
//                
//        private ILayer                      layer;
//        
//        private WeakReference<ImageCacheProcessor>  procRef;
//        
//        private Set<FeatureId>              modified = new HashSet();
//        
//        
//        public LayerListener( ILayer layer, ImageCacheProcessor processor ) {
//            this.layer = layer;
//            this.procRef = new WeakReference( processor );
//            
//            EventManager.instance().subscribe( this, new EventFilter<EventObject>() {
//                public boolean apply( EventObject ev ) {
//                    // EntityStateEvent
//                    if (ev instanceof EntityStateEvent) {
//                        EntityStateEvent eev = (EntityStateEvent)ev;
//                        return eev.getEventType() == EntityStateEvent.EventType.COMMIT; //ev.isMySession();
//                    }
//                    // FeatureChangeEvent
//                    else if (ev instanceof FeatureChangeEvent
//                            && ev.getSource() instanceof ILayer
//                            && ((ILayer)ev.getSource()).id().equals( LayerListener.this.layer.id() )) {
//                        return true;
//                    }
//                    // PropertyChangeEvent: ILayer
//                    else if (ev instanceof PropertyChangeEvent
//                            && ev.getSource() instanceof ILayer
//                            && ((ILayer)ev.getSource()).id().equals( LayerListener.this.layer.id() )
//                            && layerModProps.contains( ((PropertyChangeEvent)ev).getPropertyName() )) {
//                        return true;
//                    }
//                    return false;
//                }
//            });
//        }
//        
//        public void dispose() {
//            EventManager.instance().unsubscribe( this );
//            LayerListener.this.layer = null;            
//        }
//        
//        @EventHandler(scope=Event.Scope.Session)
//        protected void changesCommitted( EntityStateEvent ev ) {
//            ImageCacheProcessor proc = procRef.get();
//            if (proc != null) {
//                // for entity features this check does not work
//                // if (ev.getSource() == LayerListener.this.layer) {
//                
//                if (!proc.active) {
//                    // XXX if we are not active and some entities are committed, 
//                    // then this is probable a general save
//                    proc.updateAndActivate( true );
//                    modified.clear();
//
//                    // does not work properly for entity features
////                    // XXX so we guess based on the committed fids ...
////                    Set<String> committed = new HashSet();
////                    for (EntityHandle handle : ev) {
////                        committed.add( handle.id() );
////                    }
////                    for (FeatureId fid : modified) {
////                        if (committed.contains( fid.getID() ) ) {
////                            proc.updateAndActivate( true );
////                            modified.clear();
////                            return;
////                        }
////                    }
//                }
//            }
//            else {
//                dispose();
//            }
//        }
//        
//        /** A feature of the layer has changed. */
//        @EventHandler
//        protected void featuresChanged( FeatureChangeEvent ev ) {
//            ImageCacheProcessor proc = procRef.get();
//            if (proc == null) {
//                dispose();
//            }
//            else {
//                // just activate update if changes have been dropped
//                if (ev.getType() == FeatureChangeEvent.Type.FLUSHED) {
//                    proc.updateAndActivate( false );
//                }
//                // deactivate cache when features have been modified
//                // FIXME there is a race cond. with MapEditor.reloadLayer()
//                else {
//                    proc.deactivate();
//                    modified.addAll( ev.getFids() );
//                }
//            }
//        }                        
//
//        /** 
//         * The style or geores or another attribute of the layer has changed.
//         * <p/>
//         * The cache managed style information for each tile, so it is
//         * not strictly necessary to deactivate the cache. However, it
//         * seems to be the "right" way to do it.
//         */
//        @EventHandler
//        protected void layerChanged( PropertyChangeEvent ev ) {
//            ImageCacheProcessor proc = procRef.get();
//            if (proc == null) {
//                dispose();
//            }
//            else {
//                // ClearCacheAction uses layer.setStyle( layer.getStyle() ) to reload layer
//                // check this and don't disable in this case
//                if (!ev.getPropertyName().equals( ILayer.PROP_RERENDER )) {
//                    proc.deactivate();
//                }
//            }
//        }                        
//    }
        
}
