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
 */
package org.polymap.rhei.data.entityfeature;

import java.util.Map;

import org.geotools.feature.NameImpl;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import com.google.common.base.Joiner;

/**
 * Adapter between a Qi4j {@link org.qi4j.api.property.Property} and a OGC
 * property. Used by {@link IFormPageProvider} instances to handle complex
 * attributes.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class PropertyAdapter
        implements Property {

    private org.qi4j.api.property.Property  delegate;
    
    private String                          prefix;
    
    private boolean                         readOnly;


    public PropertyAdapter( org.qi4j.api.property.Property delegate ) {
        this.delegate = delegate;
    }

    public PropertyAdapter( String prefix, org.qi4j.api.property.Property delegate ) {
        this.delegate = delegate;
        this.prefix = prefix;
    }

    protected org.qi4j.api.property.Property delegate() {
        return delegate;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public PropertyAdapter setReadOnly( boolean readOnly ) {
        this.readOnly = readOnly;
        return this;
    }

    public Name getName() {
        return new NameImpl( Joiner.on( "_" ).skipNulls().join( prefix, delegate.qualifiedName().name() ) );
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
        return delegate.get();
    }

    public void setValue( Object value ) {
        if (!readOnly) {
            delegate.set( value );
        }
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}
