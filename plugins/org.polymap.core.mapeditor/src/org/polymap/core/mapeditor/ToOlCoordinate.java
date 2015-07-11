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
package org.polymap.core.mapeditor;

import java.util.function.Function;

import org.polymap.rap.openlayers.types.Coordinate;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ToOlCoordinate
        implements Function<com.vividsolutions.jts.geom.Coordinate,Coordinate> {

    public static final Coordinate map( com.vividsolutions.jts.geom.Coordinate input ) {
        assert input != null;
        return new Coordinate( input.x, input.y );
    }
    
    @Override
    public Coordinate apply( com.vividsolutions.jts.geom.Coordinate input ) {
        return map( input );
    }

}
