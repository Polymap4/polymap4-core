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
package org.polymap.core.data.image;

import java.util.ArrayList;
import java.util.List;

import org.polymap.core.data.pipeline.ProcessorResponse;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GetLayerTypesResponse
        implements ProcessorResponse {

    private List<LayerType>         types = new ArrayList();
    
    public GetLayerTypesResponse( List<LayerType> types ) {
        this.types = types;
    }

    public List<LayerType> getTypes() {
        return types;
    }

}
