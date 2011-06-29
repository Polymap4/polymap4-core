/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.qi4j.event;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.TransientProperty;

/**
 * Signals that the property that was changed was a stored property, instead of an
 * event that was fired via {@link ModelProperty} or {@link TransientProperty}. This
 * event is fired by {@link PropertyChangeSupport}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StoredPropertyChangeEvent
        extends PropertyChangeEvent {

    private static Log log = LogFactory.getLog( StoredPropertyChangeEvent.class );

    public StoredPropertyChangeEvent( Object source, String propertyName, Object oldValue,
            Object newValue ) {
        super( source, propertyName, oldValue, newValue );
    }

    
}
