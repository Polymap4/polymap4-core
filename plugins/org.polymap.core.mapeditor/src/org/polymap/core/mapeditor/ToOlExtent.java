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

import com.vividsolutions.jts.geom.Envelope;

import org.polymap.rap.openlayers.types.Extent;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ToOlExtent
        implements Function<Envelope,Extent> {

    public static Extent map( Envelope input ) {
        return new Extent( 
                input.getMinX(), 
                input.getMinY(), 
                input.getMaxX(), 
                input.getMaxY() );        
    }
    
    @Override
    public Extent apply( Envelope input ) {
        return map( input );
    }

}
