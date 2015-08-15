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

/**
 * Provides the logic to create a {@link Pipeline} out of a usecase defined by a
 * {@link PipelineProcessor} interface.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface PipelineIncubator {

    /**
     * Attempts to create a new {@link Pipeline} for the given configuration. 
     *
     * @param usecaseType A processor interface that defines the interface of the pipeline. 
     * @param dsd
     * @param procConfigs
     * @return Newly created {@link Pipeline} instance.
     * @throws PipelineIncubationException
     */
    public Pipeline newPipeline( 
            Class<? extends PipelineProcessor> usecaseType,
            DataSourceDescription dsd,
            PipelineProcessorConfiguration[] procConfigs ) 
            throws PipelineIncubationException;

}
