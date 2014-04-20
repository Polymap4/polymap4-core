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
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class RComplexAttribute
        extends RAttribute
        implements ComplexAttribute {

    private static Log log = LogFactory.getLog( RComplexAttribute.class );

    private static CacheBuilder             valueCacheBuilder = CacheBuilder.newBuilder()
            .initialCapacity( 32 ).concurrencyLevel( 1 );
            
    private IRecordState                    state;
    
    /** Lazily initialized cache of {@link Property} or <code>Collection<Property></code>. */
    private LoadingCache<String,Optional<Object>> value; 
    
    
    public RComplexAttribute( final RFeature feature, StoreKey baseKey, AttributeDescriptor descriptor, Identifier id ) {
        super( feature, baseKey, descriptor, id );
        
        value = valueCacheBuilder.build( new CacheLoader<String,Optional<Object>>() {
            public Optional<Object> load( String name ) {
                PropertyDescriptor child = getType().getDescriptor( name );
                assert child == null || child.getMaxOccurs() == 1: "Collections of properties are not yet again implemented. (" + name + ")";
                
                if (child == null) {
                    return Optional.absent();
                }
                // complex (check more special first!)
                if (child.getType() instanceof ComplexType) {
                    return Optional.of( (Object)new RComplexAttribute( 
                            RComplexAttribute.this.feature, key, (AttributeDescriptor)child, null ) );                    
                }
                // geometry
                else if (child.getType() instanceof GeometryType) {
                    return Optional.of( (Object)new RGeometryAttribute( 
                            RComplexAttribute.this.feature, key, (GeometryDescriptor)child, null ) );
                }
                // attribute
                else if (child.getType() instanceof AttributeType) {
                    return Optional.of( (Object)new RAttribute( 
                            RComplexAttribute.this.feature, key, (AttributeDescriptor)child, null ) );
                }
                // unhandled
                else {
                    throw new RuntimeException( "Unknown property type: " + child.getType() );
                }
        }});
    }

    
    @Override
    public ComplexType getType() {
        return (ComplexType)super.getType();
    }


    @Override
    public Collection<? extends Property> getValue() {
        return getProperties();
    }


//    private Function<PropertyDescriptor,Iterator<Property>> propTransform = new Function<PropertyDescriptor,Iterator<Property>>() {
//        public Iterator<Property> apply( PropertyDescriptor input ) {
//            return getProperties( input.getName().getLocalPart() ).iterator();
//        }
//    };
    
    @Override
    public Collection<Property> getProperties() {
//        // Collection view
//        return new AbstractCollection<Property>() {
//            private Lazy<Integer>       size = new LockedLazyInit();
//            @Override
//            public Iterator<Property> iterator() {
//                Iterator<PropertyDescriptor> descriptors = getType().getDescriptors().iterator();
//                return concat( transform( descriptors, propTransform ) );
//            }
//            @Override
//            public int size() {
//                return size.get( new Supplier<Integer>() {
//                    public Integer get() { return Iterators.size( iterator() ); }
//                });
//            }
//        };
        
        Collection<PropertyDescriptor> descriptors = getType().getDescriptors();
        List<Property> result = new ArrayList( descriptors.size() * 2 );
        for (PropertyDescriptor _descriptor : descriptors) {
            Optional<Object> prop = value.getUnchecked( _descriptor.getName().getLocalPart() );
            if (!prop.isPresent()) {
            }
            else if (prop.get() instanceof Collection) {
                result.addAll( (Collection<Property>)prop.get() );
            }
            else {
                result.add( (Property)prop.get() );
            }
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
            throw new RuntimeException( "Collections of properties are not yet again implemented. (" + name + ")" );
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
            throw new RuntimeException( "Collections of properties are not yet again implemented. (" + name + ")" );
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
