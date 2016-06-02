/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.project;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.polymap.model2.Composite;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EnvelopeComposite
        extends Composite {

    public static ValueInitializer<EnvelopeComposite> defaults( Envelope extent ) {
        return (EnvelopeComposite proto) -> {
            proto.minX.set( extent.getMinimum( 0 ) );
            proto.maxX.set( extent.getMaximum( 0 ) );
            proto.minY.set( extent.getMinimum( 1 ) );
            proto.maxY.set( extent.getMaximum( 1 ) );
            return proto;
        };        
    }
    
    // instance *******************************************
    
    public Property<Double>     minX;
    
    public Property<Double>     minY;
    
    public Property<Double>     maxX;

    public Property<Double>     maxY;

    
    public ReferencedEnvelope toReferencedEnvelope( CoordinateReferenceSystem crs ) {
        assert crs != null;
        return new ReferencedEnvelope( minX.get(), maxX.get(), minY.get(), maxY.get(), crs );
    }

}
