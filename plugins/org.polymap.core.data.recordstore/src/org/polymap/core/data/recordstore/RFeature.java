/* 
 * polymap.org
 * Copyright (C) 2012-2013, Falko Bräutigam. All rights reserved.
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

import java.util.Collection;

import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.geometry.BoundingBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.LazyInit;

import org.polymap.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RFeature
        extends RComplexAttribute
        implements Feature {

    private static Log log = LogFactory.getLog( RFeature.class );

    public static final String          TYPE_KEY = "_featureTypeName_";
    
    protected IRecordState              state;
    
    /** 
     * Lazily initialized cache of {@link #getBounds()}. 
     * Don't use {@link LazyInit} as multi-threaded access is very unlikely for
     * a single Feature (?) and for memory performace reasons.
     */
    private BoundingBox                 bounds;
    
    /** Lazily initialized cache of {@link #getDefaultGeometryProperty()}. */
    private GeometryAttribute           geom;
    
    
    public RFeature( IRecordState state, FeatureType type ) {
        super( null, new StoreKey(), 
                new AttributeDescriptorImpl( type, type.getName(), 0, 1, false, null), 
                new FeatureIdImpl( (String)state.id() ) );
        this.feature = this;
        this.state = state;
        this.key = new StoreKey();
        
        // just created? -> set TYPE field
        if (state.get( TYPE_KEY ) == null) {
            state.put( TYPE_KEY, type.getName().getLocalPart() );
        }
    }

    
    public FeatureId getIdentifier() {
        Identifier result = super.getIdentifier();
//        // check if the feature was newly created and has been stored
//        if (result.getID() == NOT_YET_STORED_FID
//                && state.id() != null) {
//            result = new FeatureIdImpl( (String)state.id() );
//        }
        return (FeatureId)result;
    }

    
    public FeatureType getType() {
        return (FeatureType)super.getType();
    }

    
    /**
     * Get the total bounds of this feature which is calculated by doing a union
     * of the bounds of each geometry this feature is associated with.
     * 
     * @return An Envelope containing the total bounds of this Feature.
     */
    public BoundingBox getBounds() {
        if (bounds == null) {
            bounds = new ReferencedEnvelope( getType().getCoordinateReferenceSystem() );        
            for (Property prop : getValue()) {
                if (prop instanceof GeometryAttribute) {
                    bounds.include( ((GeometryAttribute)prop).getBounds() );
                }
            }
        }
        return bounds;
    }

    
    public GeometryAttribute getDefaultGeometryProperty() {
        if (geom == null) {
            GeometryDescriptor geomDescriptor = getType().getGeometryDescriptor();
            if (geomDescriptor != null) {
                geom = (GeometryAttribute)getProperty( geomDescriptor.getName() );
            }
        }
        return geom;
    }

    
//    public GeometryAttribute getDefaultGeometryProperty() {
//        if (geom == null) {
//            //look it up from the type
//            if (getType().getGeometryDescriptor() == null ) {
//                return null;
//            }
//
//            GeometryType geomType = 
//                (GeometryType)getType().getGeometryDescriptor().getType();
//
//            if (geomType != null) {
//                for (Property prop : getValue()) {
//                    if (prop instanceof GeometryAttribute) {
//                        if (prop.getType().equals( geomType )) {
//                            geom = (GeometryAttribute)prop;  
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        return geom;
//    }


    public void setDefaultGeometryProperty( GeometryAttribute geometryAttribute ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void setValue( Collection<Property> values ) {
        super.setValue( values );
    }
    
}
