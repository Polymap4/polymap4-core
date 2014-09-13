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
package org.polymap.core.model2.store.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.feature.AttributeImpl;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import com.google.common.collect.Lists;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreCollectionProperty;
import org.polymap.core.model2.store.StoreProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FeatureCompositeState
        implements CompositeState {

    private Feature                 feature;
    
    private ComplexAttribute        state;
    
    private FeatureStoreUnitOfWork  suow;
    
    
    protected FeatureCompositeState( Feature feature, FeatureStoreUnitOfWork suow ) {
        this( feature, feature, suow );
    }

    
    protected FeatureCompositeState( Feature feature, ComplexAttribute state, FeatureStoreUnitOfWork suow ) {
        this.feature = feature;
        this.state = state;
        this.suow = suow;
    }

    
    @Override
    public Object id() {
        if (state instanceof Feature) {
            return ((Feature)state).getIdentifier().getID();
        }
        else {
            throw new IllegalStateException( "Composite property does not have an id." );            
        }
    }
    
    
    @Override
    public Object getUnderlying() {
        return feature;
    }

    
    @Override
    public StoreProperty loadProperty( PropertyInfo info ) {
        // Collection
        if (info.getMaxOccurs() > 1) {
            return new CollectionPropertyImpl( info );
        }
        // Composite
        else if (Composite.class.isAssignableFrom( info.getType() )) {
            return new CompositePropertyImpl( info );            
        }
        // primitive
        else {
            return new PropertyImpl( info );
        }
    }

    
    /**
     * 
     */
    protected class PropertyImpl
            implements StoreProperty {
    
        protected final PropertyInfo            info;
        
        /** Cache used by {@link #delegate()}. No LazyInit in order to safe memory. */
        private org.opengis.feature.Property    delegate;
    
    
        protected PropertyImpl( PropertyInfo info ) {
            this.info = info;
        }
    
        protected org.opengis.feature.Property delegate() {
            if (delegate == null) {
                // not synchronized: concurrent init is ok
                delegate = state.getProperty( info.getNameInStore() );
                assert delegate != null : "No such Feature property: " + info.getNameInStore();
            }
            return delegate;
        }

        @Override
        public Object get() {
            Object value = delegate().getValue();
            if (value != null && info.getType().isEnum()) {
                value = Enum.valueOf( info.getType(), (String)value );
            }
            return value;
        }
    
        @Override
        public void set( Object value ) {
            if (value instanceof Enum) {
                value = ((Enum)value).toString();
            }
            delegate().setValue( value );
            
            suow.markPropertyModified( feature, (AttributeDescriptor)delegate().getDescriptor(), value );
        }
        
        @Override
        public Object createValue() {
            return getInfo().getDefaultValue();
        }

        @Override
        public PropertyInfo getInfo() {
            return info;
        }
    }
    
    
    /**
     * 
     */
    protected class CompositePropertyImpl
            extends PropertyImpl {

        protected CompositePropertyImpl( PropertyInfo info ) {
            super( info );
        }

        @Override
        public CompositeState get() {
            ComplexAttribute propState = (ComplexAttribute)delegate();

            // FIXME getProperties() initializes all the properties which makes the
            // cache in RComplexAttribute pretty useless
            boolean isNull = true;
            ArrayList<Property> stack = Lists.newArrayList( (Property)propState );
            while (!stack.isEmpty() && isNull) {
                Property prop = stack.remove( stack.size()-1 );
                if (prop instanceof ComplexAttribute) {
                    stack.addAll( ((ComplexAttribute)prop).getProperties() );
                }
                else if (prop instanceof Attribute) {
                    isNull = isNull && ((Attribute)prop).getValue() == null;
                }
                else if (prop instanceof Association) {
                    isNull = isNull && ((Association)prop).getValue() == null;
                }
                else {
                    throw new IllegalStateException( "Unhandled Property type: " + prop.getClass().getName() );
                }
            }
            return !isNull ? new FeatureCompositeState( feature, propState, suow ) : null;
        }

        @Override
        public void set( Object value ) {
            throw new UnsupportedOperationException( "Setting composite property is not yet supported." );
        }

        @Override
        public CompositeState createValue() {
            ComplexAttribute propState = (ComplexAttribute)delegate();
            return new FeatureCompositeState( feature, propState, suow );
        }
    }

    
    /**
     * 
     */
    protected class CollectionPropertyImpl
            extends PropertyImpl
            implements StoreCollectionProperty {

        private Collection<org.opengis.feature.Property>    featureProps;
        
        protected CollectionPropertyImpl( PropertyInfo info ) {
            super( info );
        }
        
        @Override
        public Object createValue() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        /**
         * The value of the {@link #delegate()} property cast to {@link Collection}.
         */
        protected Collection<org.opengis.feature.Property> featureProps() {
            if (featureProps == null) {
                featureProps = state.getProperties( info.getNameInStore() );
            }
            return featureProps;
        }

        @Override
        public int size() {
            return featureProps().size();
        }

        @Override
        public Iterator iterator() {
            return new Iterator() {
                private Iterator<org.opengis.feature.Property> it = featureProps().iterator(); 
                
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Object next() {
                    if (Composite.class.isAssignableFrom( getInfo().getType() )) {
                        return new FeatureCompositeState( feature, (ComplexAttribute)it.next(), suow );
                    }
                    else {
                        return it.next().getValue();
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException( "not yet implemented." );
                }
            };
        }

        @Override
        public boolean add( Object value ) {
            PropertyDescriptor desc = state.getType().getDescriptor( info.getNameInStore() );
            AttributeImpl prop = new AttributeImpl( value, (AttributeDescriptor)desc, null );
            return featureProps().add( prop );
        }

    }
    
}
