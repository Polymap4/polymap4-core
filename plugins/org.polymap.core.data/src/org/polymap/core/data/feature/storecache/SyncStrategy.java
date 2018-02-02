/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.data.feature.storecache;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorProbe;

/**
 * Provides sync strategy SPI and default no-op implementation.
 *
 * @author Falko Bräutigam
 */
public abstract class SyncStrategy {

    public void beforeProbe( StoreCacheProcessor processor, ProcessorProbe probe, ProcessorContext context ) throws Exception {
    }

    public void afterProbe( StoreCacheProcessor processor, ProcessorProbe probe, ProcessorContext context ) {
    }

    public void beforeInit( StoreCacheProcessor processor, PipelineProcessorSite site ) throws Exception {
    }

    public void afterInit( StoreCacheProcessor processor, PipelineProcessorSite site ) throws Exception {
    }

}
