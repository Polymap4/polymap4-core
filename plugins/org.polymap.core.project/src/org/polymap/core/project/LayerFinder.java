/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerFinder
        extends LayerVisitor<ILayer> {
    
    private Predicate<ILayer>       predicate;

    
    public LayerFinder( final String... layerLabels ) {
        this.predicate = new Predicate<ILayer>() {
            Set<String> labels = Sets.newHashSet( layerLabels ); 
            public boolean apply( ILayer input ) { return labels.contains( input.getLabel() ); }
        };
    }

    
    public LayerFinder( Predicate<ILayer> predicate ) {
        this.predicate = predicate;
    }


    @Override
    public boolean visit( ILayer layer ) {
        if (predicate.apply( layer )) {
            result = layer;
            return false;
        }
        return true;
    }

}
