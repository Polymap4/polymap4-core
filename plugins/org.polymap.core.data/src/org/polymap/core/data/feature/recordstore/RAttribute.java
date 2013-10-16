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

import java.util.Collection;

import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.Types;
import org.geotools.resources.Utilities;
import org.opengis.feature.Attribute;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.identity.Identifier;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * Simple, mutable class to store attributes.
 * <p/>
 * Initially taken from GeoTools source tree in order to fix few performance
 * issues and to implement direct access to {@link IRecordState} in the given
 * {@link RFeature}.
 * 
 * @author Rob Hranac, VFNY
 * @author Chris Holmes, TOPP
 * @author Ian Schneider
 * @author Jody Garnett
 * @author Gabriel Roldan
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("deprecation")
class RAttribute 
        extends RProperty 
        implements Attribute {

    protected Identifier            id;
    
    
    public RAttribute( RFeature feature, StoreKey baseKey, AttributeDescriptor descriptor,
            Identifier id) {
        super( feature, baseKey, descriptor );
        this.id = id;
        
        // FIXME
        //Types.validate(this, getValue());
    }

    
    public RAttribute( RFeature feature, StoreKey baseKey, AttributeType type, Identifier id ) {
        this( feature, baseKey, new AttributeDescriptorImpl( type, type.getName(), 1, 1, true, null), id );
    }

    
    public Identifier getIdentifier() {
        return id;
    }
    
    
    public AttributeDescriptor getDescriptor() {
        return (AttributeDescriptor)super.getDescriptor();
    }
    
    
    public AttributeType getType() {
        return (AttributeType)super.getType();
    }
    
    
    public Object getValue() {
        // collection
        if (descriptor.getMaxOccurs() > 1) {
            return new PropertyCollection( this ) {
                protected Object valueAt( int index ) {
                    return feature.state.get( key.appendCollectionIndex( index ).toString() );
                }
            };
        }
        // single value
        else {
            return feature.state.get( key.toString() );
        }
    }

    
    public void setValue( Object newValue ) 
    throws IllegalArgumentException, IllegalStateException {
        // null
        if (newValue == null) {
            if (!getDescriptor().isNillable()) {
                throw new IllegalAttributeException( getDescriptor(), null, "Attribute is not nillable." );
            }
            feature.state.remove( key.toString() );
        }
        // collection
        else if (newValue instanceof Collection) {
            PropertyCollection coll = (PropertyCollection)getValue();
            coll.clear();
            coll.addAll( (Collection)newValue );
        }
        // single value
        else {
            feature.state.put( key.toString(), newValue );
        }

//        newValue = parse(newValue);
//
//        //TODO: remove this validation
//        Types.validate(getType(), this, newValue);
//        super.setValue( newValue );
    }

    
    public int hashCode() {
        return super.hashCode() + ( 37 * (id == null ? 0 : id.hashCode()) );
    }

    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RAttribute) {
            if (!super.equals( obj )) {
                return false;
            }
            RAttribute att = (RAttribute)obj;
            return Utilities.equals( id, att.id );
        }
        return false;
    }

    
    public void validate() {
        Types.validate(this, this.getValue() );
    }
    
    
    public String toString() {
        StringBuilder sb = new StringBuilder( getClass().getSimpleName() ).append( ":" );
        sb.append( getDescriptor().getName().getLocalPart() );
        if (!getDescriptor().getName().getLocalPart().equals( getDescriptor().getType().getName().getLocalPart() )
                || id != null) {
            sb.append( "<" );
            sb.append( getDescriptor().getType().getName().getLocalPart() );
            if (id != null) {
                sb.append( " id=" );
                sb.append( id );
            }
            sb.append( ">" );
        }
        sb.append( "=" );
        sb.append( getValue() );
        return sb.toString();
    }
    
    
//    /**
//     * Allows this Attribute to convert an argument to its prefered storage
//     * type. If no parsing is possible, returns the original value. If a parse
//     * is attempted, yet fails (i.e. a poor decimal format) throw the Exception.
//     * This is mostly for use internally in Features, but implementors should
//     * simply follow the rules to be safe.
//     * 
//     * @param value
//     *            the object to attempt parsing of.
//     * 
//     * @return <code>value</code> converted to the preferred storage of this
//     *         <code>AttributeType</code>. If no parsing was possible then
//     *         the same object is returned.
//     * 
//     * @throws IllegalArgumentException
//     *             if parsing is attempted and is unsuccessful.
//     */
//    protected Object parse(Object value) throws IllegalArgumentException {
//        if ( value != null ) {
//            Class target = getType().getBinding(); 
//            if ( !target.isAssignableFrom( value.getClass() ) ) {
//                // attempt to convert
//                Object converted = Converters.convert(value,target);
//                if ( converted != null ) {
//                    value = converted;
//                }
//            }
//        }
//        return value;
//    }
    
}
