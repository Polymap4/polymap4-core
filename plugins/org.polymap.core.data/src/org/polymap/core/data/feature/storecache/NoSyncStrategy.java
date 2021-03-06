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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.ProcessorProbe;

/**
 * No synchronization at all.
 *
 * @author Falko Br�utigam
 */
public class NoSyncStrategy
        extends SyncStrategy {

    private static final Log log = LogFactory.getLog( NoSyncStrategy.class );

    @Override
    public void beforeProbe( StoreCacheProcessor processor, ProcessorProbe probe, ProcessorContext context ) {
        log.debug( "beforeProbe(): " + probe.getClass().getSimpleName() );
    }

    @Override
    public void afterProbe( StoreCacheProcessor processor, ProcessorProbe probe, ProcessorContext context ) {
        log.debug( "afterProbe(): " + probe.getClass().getSimpleName() );
    }
    
}
