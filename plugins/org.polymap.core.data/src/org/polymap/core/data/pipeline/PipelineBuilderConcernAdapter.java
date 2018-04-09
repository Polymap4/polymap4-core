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
package org.polymap.core.data.pipeline;

import java.util.List;

/**
 * No-op implementations of all interface methods. 
 *
 * @author Falko Bräutigam
 */
public class PipelineBuilderConcernAdapter
        implements PipelineBuilderConcern {

    @Override
    public void preBuild( PipelineBuilder builder, List<Class<? extends TerminalPipelineProcessor>> terminals,
            List<Class<? extends PipelineProcessor>> transformers ) {
    }

    @Override
    public void startBuild( PipelineBuilder builder, String layerId, DataSourceDescriptor dsd,
            Class<? extends PipelineProcessor> usecase, ProcessorDescriptor start ) {
    }

    @Override
    public void terminals( PipelineBuilder builder, List<ProcessorDescriptor<TerminalPipelineProcessor>> terms ) {
    }

    @Override
    public void transformations( PipelineBuilder Builder, List<ProcessorDescriptor> chain ) {
    }

    @Override
    public void additionals( PipelineBuilder Builder, List<ProcessorDescriptor> chain ) {
    }

    @Override
    public void postBuild( PipelineBuilder builder, Pipeline pipeline ) {
    }
}
