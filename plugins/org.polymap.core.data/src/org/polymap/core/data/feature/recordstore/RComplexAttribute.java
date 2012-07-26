/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.feature.recordstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RComplexAttribute
        extends RAttribute
        implements ComplexAttribute {

    private static Log log = LogFactory.getLog( RComplexAttribute.class );

    private IRecordState            state;
    
    /** Lazily initialized cache of the value of this attribute. */
    private Collection<Property>    value;
    
    
    public RComplexAttribute( RFeature feature, StoreKey baseKey, AttributeDescriptor descriptor, Identifier id ) {
        super( feature, baseKey, descriptor, id );
    }

    
    public ComplexType getType() {
        return (ComplexType)super.getType();
    }


    public Collection<? extends Property> getValue() {
        return getProperties();
    }


    public Collection<Property> getProperties() {
        if (value == null) {
            Collection<PropertyDescriptor> descriptors = getType().getDescriptors();
            List<Property> props = new ArrayList( descriptors.size() );
            for (PropertyDescriptor child : descriptors) {
                // complex (check more special first!)
                if (child.getType() instanceof ComplexType) {
                    props.add( new RComplexAttribute( feature, key, (AttributeDescriptor)child, null ) );                    
                }
                // attribute
                else if (child.getType() instanceof AttributeType) {
                    props.add( new RAttribute( feature, key, (AttributeDescriptor)child, null ) );
                }
                // unhandled
                else {
                    throw new RuntimeException( "Unhandled property type: " + child.getType() );
                }
            }
            value = Collections.unmodifiableList( props ); 
        }
        return value;
    }

    
    public Collection<Property> getProperties( final Name name ) {
        return Collections2.filter( getProperties(), new Predicate<Property>() {
            public boolean apply( Property input ) {
                return input.getName().equals( name );
            }
        });
    }


    public Collection<Property> getProperties( final String name ) {
        return Collections2.filter( getProperties(), new Predicate<Property>() {
            public boolean apply( Property input ) {
                return input.getName().getLocalPart().equals( name );
            }
        });
    }

    
    public Property getProperty( Name name ) {
        return Iterables.getFirst( getProperties( name ), null );
    }

    
    public Property getProperty( String name ) {
        return Iterables.getFirst( getProperties( name ), null );
    }


    public void setValue( Collection<Property> values ) {
        throw new RuntimeException( "Not yet implemented. Set single attributes separately." );
    }
    
}
