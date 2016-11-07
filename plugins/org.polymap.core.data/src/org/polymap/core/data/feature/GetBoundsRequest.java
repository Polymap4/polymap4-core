/* 
 * polymap.org
 * Copyright (C) 2016, Polymap GmbH. All rights reserved.
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

import org.geotools.data.Query;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class GetBoundsRequest
        extends Configurable
        implements ProcessorRequest {

    /**
     * The optional feature query.
     */
    public Config2<GetBoundsRequest,Query>  query;

//    /**
//     * Mandatory target CRS.
//     */
//    @Mandatory
//    private Config2<GetBoundsRequest,CoordinateReferenceSystem> crs;

    
    public GetBoundsRequest() {
    }

    public GetBoundsRequest( Query query ) {
        this.query.set( query );
    }

}
