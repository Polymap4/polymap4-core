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

import java.util.EventListener;
import java.util.List;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.Event;

/**
 * Provides a convenience declaration of an {@link EventHandler} method that
 * receives {@link FeatureChangeEvent}s, within display thread and delayed for 3s.
 * 
 * @see EventManager
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface FeatureChangeListener
        extends EventListener {

    @EventHandler(scope=Event.Scope.Session, delay=1000, display=true)
    public void featureChanges( List<FeatureChangeEvent> ev );
    
}
