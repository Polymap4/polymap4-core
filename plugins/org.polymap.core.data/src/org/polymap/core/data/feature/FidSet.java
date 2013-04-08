/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.feature;

import java.util.Collection;
import java.util.HashSet;

import org.opengis.filter.identity.FeatureId;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FidSet
        extends HashSet<FeatureId> {

    public FidSet() {
        super();
    }

    public FidSet( Collection<? extends FeatureId> c ) {
        super( c );
    }

    public FidSet( int initialCapacity ) {
        super( initialCapacity );
    }

    @Override
    public boolean add( FeatureId e ) {
        if (super.add( e ) != true) {
            throw new IllegalArgumentException( "Feature Id already< exists." );
        }
        return true;
    }
    
}
