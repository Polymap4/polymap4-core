/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *    Copyright (C) 2012-2013, Falko Bräutigam. All rights reserved.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.polymap.core.data.feature.recordstore;

import java.util.HashMap;
import java.util.Map;

import org.geotools.resources.Utilities;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * Implementation of Property.
 * <p/>
 * Initially taken from GeoTools source tree in order to fix a few performance
 * issues and to implement value handling via direct access to the underlying
 * {@link IRecordState}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("deprecation")
abstract class RProperty 
        implements Property {

    protected RFeature              feature;
    
    protected StoreKey              key;

    protected PropertyDescriptor    descriptor;

    private Map<Object,Object>      userData;
    
    
    protected RProperty( RFeature feature, StoreKey baseKey, PropertyDescriptor descriptor ) {
        assert this instanceof RFeature || feature != null : "Property feature must not be null for non-Feature types.";
        assert baseKey != null : "Property storeKey must not be null or empty.";
        assert descriptor != null : "Property descriptor must not be null.";

        this.feature = feature;
        this.key = baseKey.appendProperty( descriptor.getName().getLocalPart() );
        this.descriptor = descriptor;

        assert key.length() > 0;
    }
    
    
    public PropertyDescriptor getDescriptor() {
        return descriptor;
    }

    public Name getName() {
        return getDescriptor().getName();
    }

    public PropertyType getType() {
        return getDescriptor().getType();
    }

    public boolean isNillable() {
        return getDescriptor().isNillable();
    }
    
    public Map<Object, Object> getUserData() {
        if (userData == null) {
            userData = new HashMap();
        }
        return userData;
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RProperty) {
            RProperty other = (RProperty) obj;
            if (!Utilities.equals(descriptor, other.descriptor)) {
                return false;
            }
            if (!Utilities.deepEquals(getValue(), other.getValue())) {
                return false;   
            }
            return true;
        }
        return false;
    }
    
    public int hashCode() {
        Object value = getValue();
        return 37 * descriptor.hashCode()
            + (37 * (value == null ? 0 : value.hashCode()));
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder( getClass().getSimpleName() ).append( ":" );
        sb.append( getDescriptor().getName().getLocalPart() );
        sb.append( "<" );
        sb.append( getDescriptor().getType().getName().getLocalPart() );
        sb.append( ">=" );
        sb.append( getValue() );

        return sb.toString();
    }
    
}
