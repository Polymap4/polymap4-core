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

import java.util.Collection;
import java.util.EventObject;

import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureChangeEvent 
        extends EventObject {
        // extends ModelChangeEvent {

    private static Log log = LogFactory.getLog( FeatureChangeEvent.class );
    
    public enum Type {
        ADDED,
        REMOVED,
        MODIFIED,
        /** Signals that a buffer has been flushed and this might have changed status of features. */
        FLUSHED
    }

    private Collection<Feature>     features;
    
    private Type                    type;
    
    
    public FeatureChangeEvent( Object source, Type type, Collection<Feature> features ) {
        super( source );
        this.type = type;
        this.features = features;
    }

    public ILayer getSource() {
        return (ILayer)super.getSource();
    }

    public Collection<Feature> getFeatures() {
        return features;
    }

    public Type getType() {
        return type;
    }

}
