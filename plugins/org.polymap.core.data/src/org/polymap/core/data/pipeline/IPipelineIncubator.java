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
 * Provides the logic to create a {@link Pipeline} out of:
 * <ul>
 * <li>{@link IGeoResource}s (of an {@link ILayer} or {@link IMap})</li>
 * <li>processor descriptions (of an {@link ILayer} or {@link IMap})</li>
 * <li>a given {@link LayerUseCase}</li>
 * </ul>
 * The interface is the bridge between the packages
 * <code>org.polymap.core.project</code> and <code>org.polymap.core.data</code>.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPipelineIncubator {

    static IPipelineIncubator       instance = new DefaultPipelineIncubator();

    /**
     * 
     */
    public class PipelineUseCase {
        
    }

    
    public Pipeline newPipeline( PipelineUseCase usecase )
            throws PipelineIncubationException;

}
