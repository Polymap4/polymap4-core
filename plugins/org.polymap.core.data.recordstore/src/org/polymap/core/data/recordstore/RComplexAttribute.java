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
package org.polymap.core.data.recordstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;

import org.polymap.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class RComplexAttribute
        extends RAttribute
        implements ComplexAttribute {

    private static Log log = LogFactory.getLog( RComplexAttribute.class );

    private static CacheBuilder             valueCacheBuilder = CacheBuilder.newBuilder().initialCapacity( 32 ).concurrencyLevel( 1 );
            
    private IRecordState                    state;
    
    /** Lazily initialized cache of {@link Property} or <code>Collection<Property></code>. */
    private LoadingCache<String,Optional<Object>> value; 
    
    
    public RComplexAttribute( final RFeature feature, StoreKey baseKey, AttributeDescriptor descriptor, Identifier id ) {
        super( feature, baseKey, descriptor, id );
        
        value = valueCacheBuilder.build( new CacheLoader<String,Optional<Object>>() {
            public Optional<Object> load( String name ) {
                Object result = null;
                final PropertyDescriptor child = getType().getDescriptor( name );
                // empty
                if (child == null) {
                    return Optional.absent();
                }
                // Collection
                else if (child.getMaxOccurs() > 1) {
                    StoreKey propKey = key.appendProperty( child.getName().getLocalPart() );
                    result = new PropertyCollection( child, RComplexAttribute.this.feature.state, propKey ) {
                        protected Property valueAt( StoreKey storeKey ) {
                            return buildProperty( child, storeKey );
                        }
                    };
                }
                // single Property
                else {
                    result = buildProperty( child, key );
                }
                return result != null ? Optional.of( result ) : Optional.absent();
        }});
    }

    
    protected <T extends RProperty> T buildProperty( PropertyDescriptor desc, StoreKey baseKey ) {
        PropertyType propType = desc.getType();
        // complex (check more special first!)
        if (propType instanceof ComplexType) {
            return (T)new RComplexAttribute( feature, baseKey, (AttributeDescriptor)desc, null );                    
        }
        // geometry
        else if (propType instanceof GeometryType) {
            return (T)new RGeometryAttribute( feature, baseKey, (GeometryDescriptor)desc, null );
        }
        // attribute
        else if (propType instanceof AttributeType) {
            return (T)new RAttribute( feature, baseKey, (AttributeDescriptor)desc, null );
        }
        // unhandled
        else {
            throw new RuntimeException( "Unknown property type: " + propType );
        }
    }

    
    @Override
    public ComplexType getType() {
        return (ComplexType)super.getType();
    }


    @Override
    public Collection<? extends Property> getValue() {
        return getProperties();
//        Collection<Property> props = getProperties();
//        return !props.isEmpty() ? props : null;
    }


    @Override
    public Collection<Property> getProperties() {
        Collection<PropertyDescriptor> descriptors = getType().getDescriptors();
        List<Property> result = new ArrayList( descriptors.size() * 2 );
        for (PropertyDescriptor _descriptor : descriptors) {
            result.addAll( getProperties( _descriptor.getName() ) );
        }
        return result;
    }

    
    @Override
    public Collection<Property> getProperties( final Name name ) {
        return getProperties( name.getLocalPart() );
    }


    @Override
    public Collection<Property> getProperties( final String name ) {        
        Optional<Object> result = value.getUnchecked( name );
        if (!result.isPresent()) {
            return Collections.EMPTY_LIST;
        }
        else if (result.get() instanceof Collection) {
            return (Collection<Property>)result.get();
        }
        else {
            return Collections.singleton( (Property)result.get() ); //ImmutableList.of( result );
        }
    }

    
    @Override
    public Property getProperty( Name name ) {
        return getProperty( name.getLocalPart() );
    }

    
    @Override
    public Property getProperty( String name ) {
        Optional<Object> result = value.getUnchecked( name );
        if (!result.isPresent()) {
            return null;
        }
        else if (result.get() instanceof Collection) {
            return Iterables.getFirst( (Collection<Property>)result.get(), null );
        }
        else {
            return (Property)result.get();
        }
    }


    @Override
    public void setValue( Collection<Property> values ) {
        throw new RuntimeException( "Not yet implemented. Set single attributes separatelly." );
    }
    
}
