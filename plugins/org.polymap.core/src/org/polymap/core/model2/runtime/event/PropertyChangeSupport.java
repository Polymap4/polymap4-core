/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.runtime.event;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyConcern;
import org.polymap.core.model2.PropertyConcernBase;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.runtime.event.EventManager;

/**
 * Fires {@link PropertyChangeEvent}s via {@link EventManager} when a
 * {@link Property} changes.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyChangeSupport
        extends PropertyConcernBase
        implements PropertyConcern {

    private static final Log log = LogFactory.getLog( PropertyChangeSupport.class );

    
    @Override
    public Object get() {
        return ((Property)delegate).get();
    }

    @Override
    public Object createValue( ValueInitializer initializer ) {
        return ((Property)delegate).createValue( initializer );
    }

    @Override
    public void set( Object value ) {
        ((Property)delegate).set( value );
        
        PropertyInfo info = delegate.getInfo();
        Entity entity = context.getCompositePart( Entity.class );
        PropertyChangeEvent event = new PropertyChangeEvent( entity, info.getName(), null, value );
        EventManager.instance().publish( event );
    }

}
