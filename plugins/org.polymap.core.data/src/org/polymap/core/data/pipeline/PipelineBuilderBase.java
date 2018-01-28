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

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class PipelineBuilderBase
        implements PipelineBuilder {

    protected Pipeline createPipeline( ProcessorSignature usecase, DataSourceDescriptor dsd, Iterable<ProcessorDescriptor> chain ) 
            throws PipelineBuilderException {
        Pipeline pipeline = new Pipeline( usecase, dsd );
        for (ProcessorDescriptor procDesc : chain) {
            try {
                PipelineProcessor processor = procDesc.processor();
                PipelineProcessorSite procSite = createProcessorSite( procDesc );
                procSite.usecase.set( usecase );
                procSite.dsd.set( dsd );
                procSite.builder.set( this );
                processor.init( procSite );
                pipeline.addLast( procDesc );
            }
            catch (Exception e) {
                throw new PipelineBuilderException( e.getMessage(), e );
            }
        }
        return pipeline;
    }
    
    
    protected PipelineProcessorSite createProcessorSite( ProcessorDescriptor procDesc ) {
        return new PipelineProcessorSite( procDesc.properties() );
    }

}
