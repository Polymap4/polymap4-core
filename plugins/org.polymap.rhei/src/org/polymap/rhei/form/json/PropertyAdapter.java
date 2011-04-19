/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag.
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
 *
 * $Id$
 */
package org.polymap.rhei.form.json;

import java.util.Map;

import org.geotools.feature.NameImpl;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

/**
 * Adapter between a JSON property and a OGC property. Used by {@link JsonForm}
 * instances to handle complex attributes.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision:$)
 * @since 3.0
 */
public class PropertyAdapter
        implements Property {

    private String      name;
    
    private Object      value;
    
    private Object      defaultValue;
    
    
    
    public PropertyAdapter( String name, Object defaultValue ) {
        super();
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public Name getName() {
        return new NameImpl( name );
    }

    public PropertyType getType() {
        throw new RuntimeException( "not yet implemented." );
    }

    public PropertyDescriptor getDescriptor() {
        // signal that we are a 'complex' property
        // see FormEditor#doSave() for implementation detail
        return null;
    }

    public Object getValue() {
        return value;
    }

    public void setValue( Object value ) {
        this.value = value;
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}
