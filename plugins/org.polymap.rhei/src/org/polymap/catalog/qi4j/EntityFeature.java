/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 * $Id: $
 */

package org.polymap.catalog.qi4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.resources.Utilities;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;

/**
 * {@link Feature} facade for an {@link Entity}.
 * <p>
 * Designed to cache as less as possible values in order to consume as less as
 * possible memory (compared to building every feature using
 * {@link SimpleFeatureBuilder}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class EntityFeature
        implements SimpleFeature {

    private static final Log log = LogFactory.getLog( EntityFeature.class );

    private Entity                      entity;
    
    private SimpleFeatureType           featureType;
    
    private EntityType                  entityType;
    
    private Map<String,PropertyImpl>    properties = new HashMap();
    
    private Map<String,Object>          modifiedProperties;
    
    
    public EntityFeature( Entity entity, EntityType entityType, SimpleFeatureType featureType ) {
        this.entity = entity;
        this.entityType = entityType;
        this.featureType = featureType;
    }

    public Name getName() {
        return featureType.getName();
    }

    public AttributeDescriptor getDescriptor() {
        throw new RuntimeException( "not yet implemented." );
    }

    public FeatureId getIdentifier() {
        return new FeatureId() {

            public String getID() {
                return entity.id();
            }

            public boolean matches( Object rhs ) {
                if (rhs instanceof Feature) {
                    Feature feature = (Feature)rhs;
                    return feature != null && getID().equals( feature.getIdentifier().getID() );
                }   
                return false;
            }
        };
    }

    public SimpleFeatureType getType() {
        return featureType;
    }


    /**
     * 
     */
    class PropertyImpl 
            implements Property {

        protected PropertyDescriptor    descriptor;
        
        
        protected PropertyImpl( PropertyDescriptor descriptor ) {
            assert descriptor != null;
            this.descriptor = descriptor;
        }
        
        public Object getValue() {
            // as long as no value was set deliver actual value from the
            // entity in order to give access to changes
            try {
                String propName = descriptor.getName().getLocalPart();
                Object value = modifiedProperties != null ? modifiedProperties.get( propName ) : null;
                if (value != null) {
                    return value;
                }
                else {
                    return entityType.getProperty( propName ).getValue( entity );
                }
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        
        public void setValue( Object value ) {
            log.debug( "property= " + getName().getLocalPart() + ", value=" + value );
            if (modifiedProperties == null) {
                modifiedProperties = new HashMap();
            }
            modifiedProperties.put( descriptor.getName().getLocalPart(), value );

// Property must not write back directly, modify features command has to be used
//            try {
//                entityType.getProperty( getName().getLocalPart() ).setValue( entity, value );
//            }
//            catch (Exception e) {
//                throw new RuntimeException( e );
//            }
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
            throw new RuntimeException( "not yet implemented." );
        }
        
        public boolean equals( Object obj ) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PropertyImpl)) {
                return false;
            }
            PropertyImpl other = (PropertyImpl)obj;
            if (!Utilities.equals( descriptor, other.descriptor )) {
                return false;
            }
            if (!Utilities.deepEquals( getValue(), other.getValue() )) {
                return false;
            }
            return true;
        }

        
        public int hashCode() {
            return descriptor.hashCode();
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer( getClass().getSimpleName() ).append( ":" );
            sb.append( getDescriptor().getName().getLocalPart() );
            sb.append( "<" );
            sb.append( getDescriptor().getType().getName().getLocalPart() );
            sb.append( ">=" );
            sb.append( getValue() );
            return sb.toString();
        }
        
    }

    
    public Collection<? extends Property> getValue() {
        return getProperties();
    }

    public void setValue( Collection<Property> props ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public void setValue( Object props ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public Collection<Property> getProperties() {
        List<Property> result = new ArrayList();
        for (EntityType.Property entityProp : entityType.getProperties()) {
            result.add( getProperty( entityProp.getName() ) );
        }
        return result;
    }

    public Collection<Property> getProperties( Name name ) {
        return Collections.singletonList( getProperty( name ) );
    }

    public Collection<Property> getProperties( String name ) {
        return Collections.singletonList( getProperty( name ) );
    }

    public Property getProperty( Name name ) {
        PropertyImpl result = properties.get( name.getLocalPart() );
        if (result == null) {
            result = new PropertyImpl( featureType.getDescriptor( name ) );
        }
        return result;
    }

    public Property getProperty( String name ) {
        PropertyImpl result = properties.get( name );
        if (result == null) {
            result = new PropertyImpl( featureType.getDescriptor( name ) );
        }
        return result;
    }

    public void validate()
            throws IllegalAttributeException {
        throw new RuntimeException( "not yet implemented." );
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        return false;
    }


    public void setDefaultGeometryProperty( GeometryAttribute arg0 ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        throw new RuntimeException( "not yet implemented." );
    }

    public BoundingBox getBounds() {
        throw new RuntimeException( "not yet implemented." );
    }

    
    // SimpleFeature
    
    public Object getAttribute( int index ) 
    throws IndexOutOfBoundsException {
        throw new RuntimeException( "not yet implemented." );
    }
    
    public Object getAttribute( String name ) {
        return getProperty( name ).getValue();
    }

    public Object getAttribute( Name name ) {
        return getProperty( name ).getValue();
    }

    public int getAttributeCount() {
        return getProperties().size();
    }

    public List<Object> getAttributes() {
        List<Object> result = new ArrayList();
        for (Property prop : getProperties() ) {
            result.add( prop.getValue() );
        }
        return result;
    }

    public void setAttribute(int index, Object value)
    throws IndexOutOfBoundsException {
        throw new RuntimeException( "not yet implemented." );
//        // first do conversion
//        Object converted = Converters.convert(value, getFeatureType().getDescriptor(index).getType().getBinding());
//        // if necessary, validation too
//        if(validating)
//            Types.validate(featureType.getDescriptor(index), converted);
//        // finally set the value into the feature
//        values[index] = converted;
    }
    
    public void setAttribute(String name, Object value) {
        throw new RuntimeException( "not yet implemented." );
//        final Integer idx = index.get(name);
//        if(idx == null)
//            throw new IllegalAttributeException("Unknown attribute " + name);
//        setAttribute( idx.intValue(), value );
    }

    public void setAttribute(Name name, Object value) {
        setAttribute( name.getLocalPart(), value );
    }

    public void setAttributes(List<Object> values) {
        throw new RuntimeException( "not yet implemented." );
//        for (int i = 0; i < this.values.length; i++) {
//            this.values[i] = values.get(i);
//        }
    }

    public void setAttributes(Object[] values) {
        throw new RuntimeException( "not yet implemented." );
//        setAttributes( Arrays.asList( values ) );
    }

    public Object getDefaultGeometry() {
        return getDefaultGeometryProperty().getValue();
    }

    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    public String getID() {
        return getIdentifier().getID();
    }

    public void setDefaultGeometry( Object arg0 ) {
        throw new RuntimeException( "not yet implemented." );
    }
    
}
