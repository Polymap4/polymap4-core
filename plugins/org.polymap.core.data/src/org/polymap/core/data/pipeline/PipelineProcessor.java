/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.pipeline;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;

/**
 * The SPI of a data pipeline processor. A pipeline processor is part of a
 * {@link Pipeline} and executed inside of a {@link PipelineExecutor}. A
 * processor can process requests, reponses or both.
 * <p/>
 * A processor has to be thread-safe and stateless. The state of the processor
 * can be stored in its {@link ProcessorContext}.
 * 
 * @see Produces
 * @see Consumes
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a> 
 */
public interface PipelineProcessor {
    
    /**
     *
     * @param props The persistent configuration properties.
     */
    public void init( PipelineProcessorSite site );
    
}
