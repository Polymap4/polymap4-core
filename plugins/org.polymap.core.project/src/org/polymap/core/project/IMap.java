/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import org.polymap.core.data.util.Geometries;

import org.polymap.model2.BidiAssociationName;
import org.polymap.model2.Computed;
import org.polymap.model2.ComputedBidiManyAssocation;
import org.polymap.model2.Concerns;
import org.polymap.model2.DefaultValue;
import org.polymap.model2.ManyAssociation;
import org.polymap.model2.Mixins;
import org.polymap.model2.Property;

/**
 * A Map contains other maps and/or {@link ILayer}s. It holds information about the
 * rendering of the Services of the Layers.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns({
//    ACLCheckConcern.class
})
@Mixins({
//    ACL.class
})
public class IMap
        extends ProjectNode { 

    public static IMap                  TYPE;
    
    @Computed( ComputedBidiManyAssocation.class )
    @BidiAssociationName( "parentMap" )
    public ManyAssociation<ILayer>      layers;

    @DefaultValue( "EPSG:3857" )
    public Property<String>             srsCode;


    /**
     * The max extent of the map. 
     */
    public Property<EnvelopeComposite>  maxExtent;

    
    public boolean containsLayer( ILayer search ) {
        assert search != null;
        return layers.stream()
                .filter( l -> l.id().equals( search.id() ) )
                .findAny().isPresent();
    }


    public ReferencedEnvelope maxExtent() {
        try {
            return maxExtent.get().toReferencedEnvelope( Geometries.crs( srsCode.get() ) );
        }
        catch (Exception e) {
            throw new RuntimeException();
        }
    }


    public void setMaxExtent( ReferencedEnvelope extent ) 
            throws NoSuchAuthorityCodeException, TransformException, FactoryException {
        ReferencedEnvelope transformed = extent.transform( CRS.decode( srsCode.get() ), true );
        maxExtent.createValue( EnvelopeComposite.defaults( transformed ) );
    }

}
