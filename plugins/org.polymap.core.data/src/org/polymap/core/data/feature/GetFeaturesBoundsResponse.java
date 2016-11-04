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

/**
 * The response of the {@link GetFeatureTypeRequest}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class GetFeaturesBoundsResponse
        implements ProcessorResponse {

    private ReferencedEnvelope      bounds;
    
    
    public GetFeaturesBoundsResponse( ReferencedEnvelope bounds ) {
        this.bounds = bounds;
    }

    public ReferencedEnvelope getBounds() {
        return bounds;
    }
    
}
