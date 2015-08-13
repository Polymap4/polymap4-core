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

import java.util.EventObject;
import java.util.Set;

import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private Set<FeatureId>          fids;
    
    private Type                    type;
    
    
    public FeatureChangeEvent( Object source, Type type, Set<FeatureId> fids ) {
        super( source );
        this.type = type;
        this.fids = fids;
    }

//    public ILayer getSource() {
//        return (ILayer)super.getSource();
//    }

    public Set<FeatureId> getFids() {
        return fids;
    }
    
    public Type getType() {
        return type;
    }

}
