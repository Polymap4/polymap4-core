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
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.buffer.LayerFeatureBufferManager;
import org.polymap.core.model.event.ModelChangeTracker;
import org.polymap.core.model.event.ModelHandle;
import org.polymap.core.project.ILayer;

/**
 * Provides an extension of the {@link ModelChangeTracker}.
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
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureChangeTracker {

    private static Log log = LogFactory.getLog( FeatureChangeTracker.class );

    public static final String                  MODEL_TYPE_PREFIX = "feature:";

    private static final FeatureChangeTracker   instance = new FeatureChangeTracker();
    
    
    public static FeatureChangeTracker instance()  {
        return instance;
    }
    
    
    // instance *******************************************
    
    public ModelChangeTracker delegate() {
        return ModelChangeTracker.instance();
    }
    
    
    public boolean addFeatureListener( FeatureStoreListener listener ) {
        return ModelChangeTracker.instance().addListener( listener );
    }
    

    public boolean removeFeatureListener( FeatureStoreListener listener ) {
        return ModelChangeTracker.instance().removeListener( listener );
    }


    /**
     * Creates a handle for the features of an entire layer.
     * 
     * @param layer
     */
    public static ModelHandle layerHandle( ILayer layer ) {
        String id = layer.id();
        String type = "features:" + layer.getEntityType().getName();
        return ModelHandle.instance( id, type );
    }

    
    /**
     * Creates a handle for the given feature.
     * 
     * @param feature
     */
    public static ModelHandle featureHandle( Feature feature ) {
        String id = feature.getIdentifier().getID();
        String type = FeatureChangeTracker.MODEL_TYPE_PREFIX + feature.getType().getName().getLocalPart();
        return ModelHandle.instance( id, type );
    }

    /**
     * Creates a handle for the given feature.
     * 
     * @param feature
     */
    public static ModelHandle featureHandle( FeatureId fid, String typeName ) {
        String id = fid.getID();
        String type = FeatureChangeTracker.MODEL_TYPE_PREFIX + typeName;
        return ModelHandle.instance( id, type );
    }

}
