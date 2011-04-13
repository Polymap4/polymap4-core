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
import org.json.JSONException;
import org.json.JSONObject;
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
class PropertyAdapter
        implements Property {

    private JSONObject              delegate;

    
    public PropertyAdapter( JSONObject field_json ) {
        this.delegate = field_json;
    }

    protected JSONObject delegate() {
        return delegate;    
    }
    
    public Name getName() {
        try {
            return new NameImpl( delegate.getString( "name" ) );
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: " + e, e );
        }
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
        return delegate.opt( "value" );
    }

    public void setValue( Object value ) {
        try {
            delegate.put( "value", value );
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: " + e, e );
        }
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}
