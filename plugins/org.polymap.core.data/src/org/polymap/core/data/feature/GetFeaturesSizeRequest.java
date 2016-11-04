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

import org.geotools.data.Query;

import org.polymap.core.data.pipeline.ProcessorRequest;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GetFeaturesSizeRequest
        implements ProcessorRequest {
    
    private Query           query;

    public GetFeaturesSizeRequest( Query query ) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

}
