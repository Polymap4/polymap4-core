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

import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.store.CompositeState;
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
            throw new UnsupportedOperationException( "Collection properties are not yet supported." );
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
    
        private final PropertyInfo              info;
        
        /** Cache used by {@link #underlying()}. No LazyInit in order to safe memory. */
        private org.opengis.feature.Property    underlying;
    
    
        protected PropertyImpl( PropertyInfo info ) {
            this.info = info;
        }
    
    
        protected org.opengis.feature.Property underlying() {
            if (underlying == null) {
                // not synchronized: concurrent init is ok
                underlying = state.getProperty( info.getNameInStore() );
                assert underlying != null : "No such Feature property: " + info.getNameInStore();
            }
            return underlying;
        }


        @Override
        public Object get() {
            return underlying().getValue();
        }
    
        @Override
        public void set( Object value ) {
            underlying().setValue( value );
            
            suow.markPropertyModified( feature, (AttributeDescriptor)underlying().getDescriptor(), value );
        }
        
        @Override
        public Object newValue() {
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
    class CompositePropertyImpl
            extends PropertyImpl {

        protected CompositePropertyImpl( PropertyInfo info ) {
            super( info );
        }

        @Override
        public CompositeState get() {
            ComplexAttribute propState = (ComplexAttribute)state.getProperty( getInfo().getName() ).getValue();
            return new FeatureCompositeState( feature, propState, suow );
        }

        @Override
        public void set( Object value ) {
            throw new IllegalStateException( "Setting composite property is not allowed." );
        }

        @Override
        public CompositeState newValue() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

    }
    
}
