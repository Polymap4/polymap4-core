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

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.Identifier;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class RGeometryAttribute
        extends RAttribute
        implements GeometryAttribute {

    public RGeometryAttribute( RFeature feature, StoreKey baseKey, GeometryType type, Identifier id ) {
        super( feature, baseKey, type, id );
    }

    
    public RGeometryAttribute( RFeature feature, StoreKey baseKey, GeometryDescriptor descriptor, Identifier id ) {
        super( feature, baseKey, descriptor, id );
    }


    public GeometryType getType() {
        return (GeometryType)super.getType();
    }


    public GeometryDescriptor getDescriptor() {
        return (GeometryDescriptor)super.getDescriptor();
    }


    public BoundingBox getBounds() {
        Geometry geom = (Geometry)getValue();
        ReferencedEnvelope result = new ReferencedEnvelope( getType().getCoordinateReferenceSystem() );
        if (geom != null) {
            result.expandToInclude( geom.getEnvelopeInternal() );
        }
        else {
            result.setToNull();
        }
        return result;
    }


    public void setBounds( BoundingBox bounds ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}
