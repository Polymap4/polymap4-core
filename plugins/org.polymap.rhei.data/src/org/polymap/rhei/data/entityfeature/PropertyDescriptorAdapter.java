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
package org.polymap.rhei.data.entityfeature;

import java.util.List;
import java.util.Map;

import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.EntityType.Property;


/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyDescriptorAdapter
        implements PropertyDescriptor, PropertyType {

    private static Log log = LogFactory.getLog( PropertyDescriptorAdapter.class );

    private Property            delegate;

    public PropertyDescriptorAdapter( Property delegate ) {
        assert delegate != null;
        this.delegate = delegate;
    }


    public Name getName() {
        return new NameImpl( delegate.getName() );
    }


    public PropertyType getType() {
        return this;
    }


    public int getMaxOccurs() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public int getMinOccurs() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public Map<Object, Object> getUserData() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public boolean isNillable() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    // PropertyType ***************************************

    public Class<?> getBinding() {
        return delegate.getType();
    }


    public InternationalString getDescription() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public List<Filter> getRestrictions() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public PropertyType getSuper() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public boolean isAbstract() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
