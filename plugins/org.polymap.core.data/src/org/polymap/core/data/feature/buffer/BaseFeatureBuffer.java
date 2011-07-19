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

import java.util.Collection;

import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.ListenerList;

/**
 * Provides {@link IFeatureChangeListener} handling. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class BaseFeatureBuffer {

    private static Log log = LogFactory.getLog( BaseFeatureBuffer.class );
    
    private ListenerList<IFeatureChangeListener>    listeners = new ListenerList();
    
    
    public void addFeatureChangeListener( IFeatureChangeListener l ) {
        listeners.add( l );
    }
    
    public void removeFeatureChangeListener( IFeatureChangeListener l ) {
        listeners.remove( l );
    }
    
    protected void fireFeatureChangeEvent( FeatureChangeEvent.Type type, Collection<Feature> features ) {
        FeatureChangeEvent ev = new FeatureChangeEvent( this, type, features );
        for (IFeatureChangeListener l : listeners) {
            l.featureChange( ev );
        }
    }
    
}
