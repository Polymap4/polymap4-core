/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.runtime.config;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.EventManager;

/**
 * {@link EventManager#publish(java.util.EventObject, Object...) Fires} a
 * {@link PropertyChangeEvent} when the value of the Property is changed. The source
 * of the event is the host object of the property.
 * {@link PropertyChangeEvent#getOldValue()} value is always null.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyChangeSupport
        extends DefaultPropertyConcern {

    private static Log log = LogFactory.getLog( PropertyChangeSupport.class );

    @Override
    public Object doSet( Object obj, Property prop, Object value ) {
        PropertyInfo info = prop.info();
        EventManager.instance().publish( new PropertyChangeEvent( 
                info.getHostObject(), prop.info().getName(), null, value ) );
        return value;
    }
    
}
