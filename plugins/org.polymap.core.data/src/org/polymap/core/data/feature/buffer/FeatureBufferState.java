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

import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link Feature} facade that handles the buffer state of the feature. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureBufferState {

    private static Log log = LogFactory.getLog( FeatureBufferState.class );
    
    public static final String  USER_DATA_KEY = "buffer_state";
    public static final String  USER_DATA_REMOVED = "removed";
    public static final String  USER_DATA_ADDED = "added";
    public static final String  USER_DATA_MODIFIED = "modified";
    
    private Feature             feature;

    private String              state;

    
    public FeatureBufferState( Feature feature ) {
        super();
        this.feature = feature;
        this.state = (String)feature.getUserData().get( USER_DATA_KEY );
    }
    
    public Feature feature() {
        return feature;
    }
    
    @SuppressWarnings("hiding")
    public void updateFeature( Feature feature ) {
        this.feature = feature;
    }

    public void markRemoved( boolean removed ) {
        state = USER_DATA_REMOVED;
        feature.getUserData().put( USER_DATA_KEY, state );
    }
    
    public boolean isRemoved() {
        return USER_DATA_REMOVED.equals( state );
    }

    public FeatureBufferState markAdded() {
        state = USER_DATA_ADDED;
        feature.getUserData().put( USER_DATA_KEY, state );
        return this;
    }
    
    public boolean isAdded() {
        return USER_DATA_ADDED.equals( state );
    }
    
    public FeatureBufferState markModified() {
        state = USER_DATA_MODIFIED;
        feature.getUserData().put( USER_DATA_KEY, state );
        return this;
    }
    
    public boolean isModified() {
        return USER_DATA_MODIFIED.equals( state );
    }
    
}
