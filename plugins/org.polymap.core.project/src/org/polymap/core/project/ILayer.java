/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.project;

import static org.polymap.core.data.pipeline.ProcessorExtension.forType;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.security.SecurityContext;

import org.polymap.model2.Association;
import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Concerns;
import org.polymap.model2.Defaults;
import org.polymap.model2.Mixins;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.ValueInitializer;
import org.polymap.model2.runtime.event.PropertyChangeSupport;

/**
 *  
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns({
    PropertyChangeSupport.class
    // ACLCheckConcern.class
})
@Mixins({
    //ACL.class
})
public class ILayer
        extends ProjectNode {

    private static final Log log = LogFactory.getLog( ILayer.class );

    public static final Comparator<ILayer>  ORDER_KEY_ORDERING = (l1,l2) -> l1.orderKey.get().compareTo( l2.orderKey.get() );
    
    public static ILayer            TYPE;
    
    public Property<String>         resourceIdentifier;

    @Nullable
    public Property<String>         styleIdentifier;

    @Defaults
    public Property<Integer>        orderKey;
    
    /**
     * Configuration properties of processors that are set up for this layer. This
     * might by {@link PipelineProcessor}s or any other kind of additional processing
     * thing.
     * <p/>
     * Use {@link ProcessorConfig#init(ProcessorExtension)} to create a new entry.
     */
    @Defaults
    public CollectionProperty<ProcessorConfig> processorConfigs;
    
    /**  */
    public static class ProcessorConfig
            extends Composite {
    
        public static final ValueInitializer<ProcessorConfig> init( ProcessorExtension ext ) {
            return (ProcessorConfig proto) -> {
                proto.id.set( UUID.randomUUID().toString() );
                proto.type.set( ext.getProcessorType().getName() );
                return proto;
            };
        }
        
        public Property<String>     id;
        
        public Property<String>     type;
        
        public Lazy<Optional<ProcessorExtension>>   ext = new LockedLazyInit( () -> forType( type.get() ) );
        
        @Defaults
        public CollectionProperty<KeyValue>         params;
        
        public UnitOfWork belongsTo() {
            return context.getUnitOfWork();
        }
        
        @Override
        public boolean equals( Object obj ) {
            return obj instanceof ProcessorConfig
                    ? ((ProcessorConfig)obj).id.get().equals( id.get() )
                    : false;
        }

        public Params params() {
            Params result = new Params();
            params.forEach( param -> result.put( param.key.get(), param.value.get() ) );
            return result;
        }
        
        public void updateParams( Params newParams ) {
            params.clear();
            newParams.entrySet().forEach( param -> params.createElement( (KeyValue proto) -> {
                proto.key.set( param.getKey() ); proto.value.set( (String)param.getValue() ); return proto;
            }));
            
        }
    }

    /**  */
    public static class KeyValue
            extends Composite {
        
        public Property<String>     key;
        
        public Property<String>     value;
    }
    
    /**
     * The user settings for this ProjectNode and the current user/session (
     * {@link SecurityContext#getUser()}).
     * <p/>
     * The instance is queried initially and then cached.
     */
    public Lazy<LayerUserSettings>  userSettings = new LockedLazyInit( () -> 
            findUserSettings( LayerUserSettings.class, LayerUserSettings.TYPE.layer ) );

    /**  */
    public static class LayerUserSettings
            extends UserSettings {

        @SuppressWarnings( "hiding" )
        public static LayerUserSettings TYPE;

        /**
         * This Entity belongs to the UnitOfWork of the {@link LayerUserSettings}, so
         * don't equals() with other ILayer nor modify properties.
         */
        protected Association<ILayer>   layer;

        /**
         * The id of the {@link ILayer} this instance belongs to.
         */
        public String layerId() {
            return layer.get().id();
        }
    }


    /**
     * Bubble up {@link #orderKey} one step.
     * <p/>
     * This changes {@link #orderKey} if this and other layers. Should be called
     * inside an operation and a nested {@link UnitOfWork}.
     */
    public void orderUp( IProgressMonitor monitor ) {
        List<ILayer> ordered = orderedLayers();
        int index = ordered.indexOf( this );
        assert index < ordered.size()-1 : "index >= size-1, we are the highest orderKey: " + this;
        ordered.remove( index );
        ordered.add( index+1, this );
        updateOrderKeys( ordered );
    }

    
    /**
     * Bubble down {@link #orderKey} one step.
     * <p/>
     * This changes {@link #orderKey} if this and other layers. Should be called
     * inside an operation and a nested {@link UnitOfWork}.
     */
    public void orderDown( IProgressMonitor monitor ) {
        List<ILayer> ordered = orderedLayers();
        int index = ordered.indexOf( this );
        assert index > 0 : "index <= 0, we are the lowest orderKey: " + this;
        ordered.remove( index );
        ordered.add( index-1, this );
        updateOrderKeys( ordered );
    }

    
    /**
     * The highest {@link #orderKey} of all layers of my {@link ProjectNode#parentMap}. 
     */
    public Integer maxOrderKey() {
        List<ILayer> ordered = orderedLayers();
        return ordered.isEmpty() ? 0 : ordered.get( ordered.size()-1 ).orderKey.get();
    }
    
    
    /**
     * The lowest {@link #orderKey} of all layers of my {@link ProjectNode#parentMap}. 
     */
    public Integer minOrderKey() {
        List<ILayer> ordered = orderedLayers();
        return ordered.isEmpty() ? 0 : ordered.get( 0 ).orderKey.get();
    }
    
    
    public List<ILayer> orderedLayers() {
        return parentMap.get().layers.stream()
                .sorted( (l1,l2) -> l1.orderKey.get().compareTo( l2.orderKey.get() ) )
                .collect( Collectors.toCollection( () -> new LinkedList() ) );
    }
    
    
    public void updateOrderKeys( List<ILayer> ordered ) {
        int key = 1;
        for (ILayer l : ordered) {
            l.orderKey.set( key++ );
        }
    }
    
}
