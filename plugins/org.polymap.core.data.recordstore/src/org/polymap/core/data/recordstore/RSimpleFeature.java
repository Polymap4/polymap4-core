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

import java.util.Arrays;
import java.util.List;

import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.polymap.recordstore.IRecordState;

/**
 * Provides {@link SimpleFeature} API methods on top of an {@link RFeature}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class RSimpleFeature
        extends RFeature
        implements SimpleFeature {

    private static Log log = LogFactory.getLog( RSimpleFeature.class );

    public RSimpleFeature( IRecordState state, FeatureType type ) {
        super( state, type );
    }

    @Override
    public String getID() {
        return getIdentifier().getID();
    }

    @Override
    public SimpleFeatureType getType() {
        return (SimpleFeatureType)super.getType();
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return getType();
    }

    @Override
    public Object getAttribute( String name ) {
        Property prop = getProperty( name );
        return prop != null ? prop.getValue() : null;
    }

    @Override
    public Object getAttribute( Name name ) {
        Property prop = getProperty( name );
        return prop != null ? prop.getValue() : null;
    }

    @Override
    public Object getAttribute( int index ) throws IndexOutOfBoundsException {
        return ((List<Property>)getProperties()).get( index ).getValue();
    }

    @Override
    public int getAttributeCount() {
        return getProperties().size();
    }

    @Override
    public List<Object> getAttributes() {
        return Lists.transform( (List<Property>)getProperties(), new Function<Property,Object>() {
            public Object apply( Property input ) {
                return input.getValue();
            }
        });
    }

    @Override
    public Object getDefaultGeometry() {
        return getDefaultGeometryProperty().getValue();
        //return getAttribute( getFeatureType().getGeometryDescriptor().getName() );
    }

    @Override
    public void setAttribute( String name, Object value ) {
        getProperty( name ).setValue( value );
    }

    @Override
    public void setAttribute( Name name, Object value ) {
        getProperty( name ).setValue( value );
    }

    @Override
    public void setAttribute( int index, Object value ) throws IndexOutOfBoundsException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setAttributes( List<Object> values ) {
        int index = 0;
        for (Property prop : getProperties()) {
            prop.setValue( values.get( index++ ) );
        }
    }

    @Override
    public void setAttributes( Object[] values ) {
        setAttributes( Arrays.asList( values ) );
    }

    @Override
    public void setDefaultGeometry( Object geometry ) {
        getDefaultGeometryProperty().setValue( geometry );
    }
}
