/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.data.entityfeature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.opengis.feature.Feature;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.data.FeatureChangeTracker;
import org.polymap.core.data.FeatureEventManager;
import org.polymap.core.data.feature.buffer.LayerFeatureBufferManager;
import org.polymap.core.model.event.ModelChangeTracker;
import org.polymap.core.model.event.ModelHandle;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.SessionSingleton;

/**
 * 
 * @see LayerFeatureBufferManager
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerEntityBufferManager
        implements IOperationSaveListener {

    private static Log log = LogFactory.getLog( LayerEntityBufferManager.class );
    
    /**
     * The Session holds the managers of the session.
     */
    static class Session
            extends SessionSingleton { 
        
        protected WeakHashMap<ILayer,LayerEntityBufferManager> managers = new WeakHashMap();
        
        public static Session instance() {
            return instance( Session.class );
        }
    }

    /**
     * Gets the buffer manager for the given layer of the current session. If no
     * manager exists yet a new one is created with default buffer type/impl and
     * settings if <code>create</code> is true, otherwise null might be returned.
     * 
     * @param layer
     * @param create True specifies that a new buffer manager is created if
     *        necessary.
     * @return The buffer manager for the given layer.
     */
    public static LayerEntityBufferManager forLayer( ILayer layer, EntityProvider entityProvider ) {
        assert layer != null;
        
        WeakHashMap<ILayer,LayerEntityBufferManager> managers = Session.instance().managers;
        synchronized (managers) {
            LayerEntityBufferManager result = managers.get( layer );
            if (result == null) {
                result = new LayerEntityBufferManager( layer );
                managers.put( layer, result );
            }
            return result;
        }
    }
    
    /*
     * 
     */
    static class Change {
        
        FeatureChangeEvent.Type     type;
        
        FeatureId                   id;
        
        ModelHandle                 handle;

        Change( FeatureId id, ModelHandle handle, FeatureChangeEvent.Type type ) {
            this.id = id;
            this.type = type;
            this.handle = handle;
        }
    }

    
    // instance *******************************************
    
    private ILayer                      layer;
    
    private Map<FeatureId,Change>       changed = new HashMap();
    
    private ModelChangeTracker.Updater  updater;
    

    LayerEntityBufferManager( ILayer layer ) {
        this.layer = layer;
        
        OperationSupport.instance().addOperationSaveListener( this );
    }

    
    public Iterable<FeatureId> added() {
        Iterable<Change> result = Iterables.filter( changed.values(), new Predicate<Change>() {
            public boolean apply( Change input ) {
                return input.type == FeatureChangeEvent.Type.ADDED;
            }
        });
        return Iterables.transform( result, new Function<Change,FeatureId>() {
            public FeatureId apply( Change input ) {
                return input.id;
            }
        });
    }

    
    /**
     * Fires a {@link FeatureChangeEvent} for the layer of the given context.
     * <p/>
     * For other layers this is done by the {@link LayerFeatureBufferManager}. As
     * the LFBM does not handle entity feature sources the event has to be fired
     * here explicitly. 
     *
     * @param context
     * @param features
     * @param eventType
     */
    public void fireFeatureChangeEvent( List<Feature> features, FeatureChangeEvent.Type eventType ) {
        for (Feature feature : features) {
            ModelHandle handle = FeatureChangeTracker.featureHandle( feature );
            try {
                ModelChangeTracker.instance().track( this, handle, System.currentTimeMillis(), false );
                FeatureId fid = feature.getIdentifier();
                changed.put( fid, new Change( fid, handle, eventType ) );
            }
            catch (Exception e) {
                // feature/handle already registered -> ignore
            }
        }
        FeatureChangeEvent ev = new FeatureChangeEvent( layer, eventType, features );
        FeatureEventManager.instance().fireEvent( ev );
    }

    
    public void prepareSave( OperationSupport os, IProgressMonitor monitor )
    throws Exception {
        assert updater == null;
        updater = ModelChangeTracker.instance().newUpdater();
        for (Change change : changed.values()) {
            updater.checkSet( change.handle, null, null );
        }
    }

    
    public void rollback( OperationSupport os, IProgressMonitor monitor ) {
        assert updater != null;
        updater = null;
        
        FeatureChangeEvent ev = new FeatureChangeEvent( layer, FeatureChangeEvent.Type.FLUSHED, null );
        FeatureEventManager.instance().fireEvent( ev );
        changed.clear();
    }

    
    public void save( OperationSupport os, IProgressMonitor monitor ) {
        assert updater != null;
        updater.apply( layer );
        updater = null;
        changed.clear();
    }

    
    public void revert( OperationSupport os, IProgressMonitor monitor ) {
        FeatureChangeEvent ev = new FeatureChangeEvent( layer, FeatureChangeEvent.Type.FLUSHED, null );
        FeatureEventManager.instance().fireEvent( ev );
        changed.clear();
    }



}
