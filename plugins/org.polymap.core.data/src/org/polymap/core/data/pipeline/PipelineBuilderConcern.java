/* 
 * polymap.org
 * Copyright 2011-2018, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.pipeline;

import java.util.List;

/**
 * Intercepts and decorates {@link PipelineBuilder}.
 * <p/>
 * Instances might be ...
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface PipelineBuilderConcern {

    /**
     * 
     *
     * @param builder The builder we are working for.
     * @param dsd 
     * @param layerId 
     * @param start The start processor with the usecase signature.
     */
    public void preBuild( PipelineBuilder builder, 
            List<Class<? extends TerminalPipelineProcessor>> terminals,
            List<Class<? extends PipelineProcessor>> transformers );

    /**
     * 
     *
     * @param builder The builder we are working for.
     * @param dsd 
     * @param layerId 
     * @param usecase 
     * @param start The start processor with the usecase signature.
     */
    public void startBuild( PipelineBuilder builder, String layerId, DataSourceDescriptor dsd, 
            Class<? extends PipelineProcessor> usecase, ProcessorDescriptor start );

    /**
     * 
     *
     * @param builder The builder we are working for.
     * @param terms The list of possible terminal processors.
     */
    public void terminals( PipelineBuilder builder, List<ProcessorDescriptor<TerminalPipelineProcessor>> terms );

    /**
     * 
     *
     * @param builder The builder we are working for.
     * @param chain The generated chain, might be empty.
     */
    public void transformations( PipelineBuilder Builder, List<ProcessorDescriptor> chain );

    /**
     * Additional processors typically are configured by the user. They don't
     * transform data.
     *
     * @param builder The builder we are working for.
     * @param chain The generated chain, with addional processors.
     */
    public void additionals( PipelineBuilder Builder, List<ProcessorDescriptor> chain );


//    public void processorAdded( Pipeline pipeline, PipelineProcessor processor );
    
    public void postBuild( PipelineBuilder builder, Pipeline pipeline );

}
