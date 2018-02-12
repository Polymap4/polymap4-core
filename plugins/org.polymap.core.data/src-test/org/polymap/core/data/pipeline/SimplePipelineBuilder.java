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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.geotools.data.FeatureSource;

import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.pipeline.Param.ParamsHolder;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class SimplePipelineBuilder
        extends PipelineBuilderBase
        implements ParamsHolder {

    private Params          params = new Params();
    

    public Pipeline newFeaturePipeline( FeatureSource fs, Class<? extends PipelineProcessor>... procTypes ) 
            throws PipelineBuilderException {
        ProcessorSignature usecase = new ProcessorSignature( FeaturesProducer.class );
        DataSourceDescriptor dsd = new DataSourceDescriptor( fs.getDataStore(), fs.getName().getLocalPart() );
    
        List<ProcessorDescriptor> procs = Arrays.stream( procTypes )
                .map( procType -> new ProcessorDescriptor( procType, params ) )
                .collect( Collectors.toList() );
        
        return createPipeline( usecase, dsd, procs );
    }
    
    
    @Override
    public Optional<Pipeline> createPipeline( Class<? extends PipelineProcessor> usecaseType, DataSourceDescriptor dsd ) 
            throws PipelineBuilderException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    @Override
    public Params params() {
        return params;
    }

}
