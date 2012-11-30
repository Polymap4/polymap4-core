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

import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.entity.EntityStateEvent;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.Event;

/**
 * Receives {@link EntityStateEvent}s with source instanceof {@link ILayer}
 * only.
 * 
 * @see FeatureStateTracker
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface FeatureStateListener {

    /**
     * 
     *
     * @param ev The source of the event is always an instance of {@link ILayer}.
     */
    @EventHandler(scope=Event.Scope.JVM)
    public void featureChanged( EntityStateEvent ev );
    
}
