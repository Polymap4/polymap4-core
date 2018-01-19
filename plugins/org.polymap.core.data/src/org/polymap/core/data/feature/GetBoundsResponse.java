/* 
 * polymap.org
 * Copyright (C) 2009-2016, Polymap GmbH. All rights reserved.
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

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;

/**
 * The response of the {@link GetBoundsRequest}.
 *
 * @author Falko Bräutigam
 */
public class GetBoundsResponse
        extends Configurable
        implements ProcessorResponse {

    //@Mandatory null result is allowed
    @Immutable
    public Config2<GetBoundsResponse,ReferencedEnvelope>    bounds;
    
    
    public GetBoundsResponse( ReferencedEnvelope bounds ) {
        this.bounds.set( bounds );
    }

}
