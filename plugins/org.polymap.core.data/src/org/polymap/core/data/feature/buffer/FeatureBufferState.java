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

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.FeatureChangeTracker;
import org.polymap.core.model.event.IModelHandleable;
import org.polymap.core.model.event.ModelHandle;

/**
 * A {@link Feature} facade that handles the buffer state of the feature.
 * <p>
 * This holds a copy of the original feature. This allows to 1.) figure what
 * properties have been changed and 2.) check if the underlying feature in the backend
 * store has been changed.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureBufferState
        implements IModelHandleable {

    private static Log log = LogFactory.getLog( FeatureBufferState.class );
    
    public static final String  BUFFER_STATE_KEY = "buffer_state";
    public static final String  TIMESTAMP_KEY = "timestamp";
    
    public enum State {
        /** 
         * Feature is registered with the buffer but has no modifications yet. Or
         * the feature was added and removed right afterwards.
         */
        REGISTERED,
        REMOVED,
        ADDED,
        MODIFIED
    }
    
    private Feature             feature;
    
    private Feature             original;

    private State               state;
    
    private long                timestamp;
    
    
    public FeatureBufferState( Feature original ) {
        super();
        this.original = original;
        this.state = State.REGISTERED;
        
        // copy-on-write in evolveState()
        this.feature = original;
        this.feature.getUserData().put( BUFFER_STATE_KEY, state.toString() );
        
        //
        Long featureTimestamp = (Long)feature.getUserData().get( TIMESTAMP_KEY );
        timestamp = featureTimestamp != null ? featureTimestamp : System.currentTimeMillis();
    }
    
    /**
     *
     * @param modify Indicates that the returned feature is intended to be modified by the caller.
     */
    public Feature feature() {
        return feature;
    }
    
    public Feature original() {
        return original;    
    }

    public long timestamp() {
        return timestamp;
    }

    public ModelHandle handle() {
        return FeatureChangeTracker.featureHandle( feature );
    }

    
    /**
     * Evolve the state of this feature. 
     * <p/>
     * This method implementy copy-on-write for the underlying feature. So this MUST
     * be called <b>before</b> {@link #feature()} is called to modify the underlying
     * feature.
     * 
     * @param newState
     * @return The new state.
     */
    public State evolveState( State newState ) {
        if (newState == State.ADDED) {
            if (state != State.REGISTERED && state != State.ADDED) {
                throw new IllegalStateException( "Attempt to add 'old' feature: " + original.toString() );
            }
            state = newState;
        }
        else if (newState == State.MODIFIED) {
            // copy original feature            
            if (state == State.REGISTERED) {
                this.feature = SimpleFeatureBuilder.copy( (SimpleFeature)original );
            }

            if (state == State.REMOVED) {
                throw new IllegalStateException( "Attempt to modify removed feature: " + original.toString() );
            }
            state = state == State.ADDED ? State.ADDED : newState;
        }
        else if (newState == State.REMOVED) {
            state = state == State.ADDED ? State.REGISTERED : newState;
        }
        feature.getUserData().put( BUFFER_STATE_KEY, state.toString() );
        return state;
    }
    
    public boolean isAdded() {
        return state == State.ADDED;
    }

    public boolean isModified() {
        return state == State.MODIFIED;
    }

    public boolean isRemoved() {
        return state == State.REMOVED;
    }
    
    public boolean isRegistered() {
        return state == State.REGISTERED;
    }
    
}
