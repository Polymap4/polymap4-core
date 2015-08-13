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
package org.polymap.core.data;

import org.geotools.data.FeatureListener;
import org.opengis.feature.Feature;
import org.polymap.core.runtime.entity.EntityStateTracker;

/**
 * Provides an extension of the {@link EntityStateTracker} related to {@link Feature}s.
 * <p/>
 * Register a listener if you want to get informed about feature changes in any
 * session of this VM. This does refer to changes of an underlying data store only!
 * For local changes see {@link LayerFeatureBufferManager}. This differs from GeoTools
 * {@link FeatureListener} as... you know.
 * <ul>
 * <li>processing is done inside a Job so that it does not block current thread</li>
 * <li>listener references are <b>weak</b></li>
 * </ul>
 *
 * @deprecated Not yet ported to Polymap4
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureStateTracker {

//    private static Log log = LogFactory.getLog( FeatureStateTracker.class );
//
//    public static final String                  MODEL_TYPE_PREFIX = "feature:";
//
//    private static final FeatureStateTracker    instance = new FeatureStateTracker();
//    
//    
//    public static FeatureStateTracker instance()  {
//        return instance;
//    }
//    
//    
//    // instance *******************************************
//    
////    public ModelChangeTracker delegate() {
////        return ModelChangeTracker.instance();
////    }
//
//    /**
//     * Adds the given handler of {@link EntityStateEvent} events.
//     * 
//     * @see EntityStateTracker#addListener(Object, EventFilter...)
//     * @param handler An {@link FeatureStateListener} or any other
//     *        {@link EventHandler annotated} object.
//     * @param filters
//     */
//    public void addFeatureListener( Object handler, EventFilter... filters ) {
//        EntityStateTracker.instance().addListener( handler, concat( new EventFilter<EntityStateEvent>() {
//            public boolean apply( EntityStateEvent ev ) {
//                return ev.getSource() instanceof ILayer;
//            }
//        }, filters ) );
//    }
//    
//
//    public void removeFeatureListener( Object handler ) {
//        EntityStateTracker.instance().removeListener( handler );
//    }
//
//
//    /**
//     * Creates a handle for the features of an entire layer.
//     * 
//     * @param layer
//     * @return The EntityHandle, or null if no geores could be found for the given layer.
//     */
//    public static EntityHandle layerHandle( ILayer layer ) {
//        // several layers may map to the same geores
//        //String id = layer.id();
//        IGeoResource geores = layer.getGeoResource();
//        if (geores != null) {
//            String id = geores.getIdentifier().toString();
//            String type = "features:" + layer.getEntityType().getName();
//            return EntityHandle.instance( id, type );
//        }
//        else {
//            log.warn( "No geores found for layer: " + layer );
//            return null;
//        }
//    }
//
//    
//    /**
//     * Creates a handle for the given feature.
//     * 
//     * @param feature
//     */
//    public static EntityHandle featureHandle( Feature feature ) {
//        String id = feature.getIdentifier().getID();
//        String type = FeatureStateTracker.MODEL_TYPE_PREFIX + feature.getType().getName().getLocalPart();
//        return EntityHandle.instance( id, type );
//    }
//
//    
//    /**
//     * Creates a handle for the given feature.
//     * 
//     * @param feature
//     */
//    public static EntityHandle featureHandle( FeatureId fid, String typeName ) {
//        String id = fid.getID();
//        String type = FeatureStateTracker.MODEL_TYPE_PREFIX + typeName;
//        return EntityHandle.instance( id, type );
//    }

}
