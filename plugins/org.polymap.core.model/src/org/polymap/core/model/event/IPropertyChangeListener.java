/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH, and individual contributors
 * as indicated by the @authors tag.
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
package org.polymap.core.model.event;

import java.util.EventListener;

import java.beans.PropertyChangeEvent;

import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * Provides a convenience declaration of an {@link EventHandler} method that receives
 * {@link PropertyChangeEvent}s, within display thread and delayed for 3s.
 * 
 * @see EventManager
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public interface IPropertyChangeListener
        extends EventListener {

    @EventHandler(scope=Event.Scope.Session, delay=3000, display=true)
    public void propertyChange( ModelChangeEvent ev );
    
}
