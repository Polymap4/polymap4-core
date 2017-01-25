/* 
 * polymap.org
 * Copyright (C) 2016-2017, the @authors. All rights reserved.
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
package org.polymap.core.style.model;

import java.util.EventObject;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.CollectionPropertyConcern;
import org.polymap.model2.PropertyConcernAdapter;
import org.polymap.model2.runtime.PropertyInfo;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * This event is fired when a {@link StylePropertyValue} has been changed.
 *
 * @author Falko Bräutigam
 */
public class StylePropertyChange
        extends EventObject {

    private PropertyInfo        prop;
    
    
    public StylePropertyChange( FeatureStyle featureStyle, PropertyInfo prop ) {
        super( featureStyle );
        this.prop = prop;
    }

    @Override
    public FeatureStyle getSource() {
        return (FeatureStyle)super.getSource();
    }

    public PropertyInfo getProp() {
        return prop;
    }

    
    /**
     * Fires a {@link StylePropertyChange} event. 
     */
    public static class Concern
            extends PropertyConcernAdapter
            implements CollectionPropertyConcern {

        protected void fireEvent() {
            PropertyInfo propInfo = info();
            FeatureStyle featureStyle = context.getEntity();
            EventManager.instance().publish( new StylePropertyChange( featureStyle, propInfo ) );
        }
        
        @Override
        public Object createValue( ValueInitializer initializer ) {
            try { return super.createValue( initializer ); } 
            finally { fireEvent(); }
        }

        @Override
        public Object createElement( ValueInitializer initializer ) {
            try { return ((CollectionProperty)delegate()).createElement( initializer ); } 
            finally { fireEvent(); }
        }

        @Override
        public void set( Object value ) {
            try { super.set( value ); } 
            finally { fireEvent(); }
        }
    }
    
}
