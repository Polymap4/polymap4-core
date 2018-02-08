/*
 * polymap.org
 * Copyright (C) 2009-2018, Polymap GmbH. All rights reserved.
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

/**
 * Provides the logic to create a {@link Pipeline} out of a usecase defined by a
 * {@link PipelineProcessor} interface.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface PipelineBuilder {

    /**
     * Attempts to create a new {@link Pipeline} for the given configuration. 
     *
     * @param usecase A processor interface that defines the interface of the pipeline. 
     * @param dsd
     * @param procs (Additional) processors to add to the pipeline.
     * @return Newly created {@link Pipeline} instance.
     * @throws PipelineBuilderException
     */
    public Pipeline createPipeline( Class<? extends PipelineProcessor> usecase, DataSourceDescriptor dsd ) 
            throws PipelineBuilderException;

}
