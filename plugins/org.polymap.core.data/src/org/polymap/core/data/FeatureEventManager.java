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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.event.IEventFilter;
import org.polymap.core.model.event.ModelEventManager;

/**
 * A simple extension of the {@link ModelEventManager} that allows to register
 * {@link FeatureChangeListener}s.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureEventManager {

    private static Log log = LogFactory.getLog( FeatureEventManager.class );
    
    private static final FeatureEventManager    instance = new FeatureEventManager();
    
    
    public static final FeatureEventManager instance() {
        return instance;    
    }

    
    // instance *******************************************
    
    public void addFeatureChangeListener( FeatureChangeListener l, final IEventFilter f ) {
        IEventFilter filter = new IEventFilter() {
            public boolean accept( EventObject ev ) {
                return (ev instanceof FeatureChangeEvent) && f.accept( ev );
            }
        };
        ModelEventManager.instance().addModelChangeListener( l, filter );
    }

    
    public void removeFeatureChangeListener( FeatureChangeListener l ) {
        ModelEventManager.instance().removeModelChangeListener( l );
    }

    
    public void fireEvent( FeatureChangeEvent ev ) {
        ModelEventManager.instance().fireModelChangeEvent( ev );
    }
    
}
