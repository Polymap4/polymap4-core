/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.feature.buffer;

import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.SessionSingletonBase;

import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerFeatureBufferManager {

    private static Log log = LogFactory.getLog( LayerFeatureBufferManager.class );


    /**
     * The Session holds the managers of the session.
     */
    static class Session
            extends SessionSingletonBase { 
        
        protected WeakHashMap<ILayer,LayerFeatureBufferManager> managers = new WeakHashMap();
        
        public static Session instance() {
            return (Session)getInstance( Session.class );
        }
        
    }
    
    
    /**
     * Gets the buffer manager for the given layer of the current session. If no
     * manager exists yet than a new one is created with default buffer type/impl
     * and settings.
     * 
     * @param layer
     * @return The buffer manager for the given layer.
     */
    public static final synchronized LayerFeatureBufferManager forLayer( ILayer layer ) {
        assert layer != null;
        
        WeakHashMap<ILayer, LayerFeatureBufferManager> managers = Session.instance().managers;
        
        LayerFeatureBufferManager result = managers.get( layer );
        if (result == null) {
            result = new LayerFeatureBufferManager( layer );
            managers.put( layer, result );
        }
        return result;
    }
    
   
    // instance *******************************************
    
    private ILayer                  layer;

    private IFeatureBuffer          buffer;
    
    private FeatureBufferProcessor  processor;
    

    protected LayerFeatureBufferManager( ILayer layer ) {
        super();
        this.layer = layer;
        buffer = new MemoryFeatureBuffer();
        processor = new FeatureBufferProcessor( buffer );
    }

    public ILayer getLayer() {
        return layer;
    }
    
    public IFeatureBuffer getBuffer() {
        return buffer;
    }
    
    public FeatureBufferProcessor getProcessor() {
        return processor;
    }
    

//    public void flushBuffer() {
//        
//    }
    
}
