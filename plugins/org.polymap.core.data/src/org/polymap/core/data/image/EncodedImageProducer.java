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
package org.polymap.core.data.image;

import org.polymap.core.data.pipeline.EndOfProcessing;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.Produces;

/**
 * Defines the WMS pipeline usecase.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface EncodedImageProducer
        extends PipelineProcessor {
    
    @Produces({EncodedImageResponse.class, EndOfProcessing.class})
    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception;

    @Produces({EncodedImageResponse.class, EndOfProcessing.class})
    public void getLegendGraphicRequest( GetLegendGraphicRequest request, ProcessorContext context ) throws Exception;
    
    @Produces(GetLayerTypesResponse.class)
    public void getLayerTypesRequest( GetLayerTypesRequest request, ProcessorContext context ) throws Exception;
    
}
