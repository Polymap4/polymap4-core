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

import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class PipelineBuilderBase
        implements PipelineBuilder {

    //  /**
    //  * Session bound incubation listeners. 
    //  */
    // static class Session
    //         extends SessionSingleton {
    //
    //     private ListenerList<PipelineBuilderConcern> listeners = new ListenerList();
    //
    //     public static Session instance() {
    //         return instance( Session.class );
    //     }
    //
    //     protected Session() {
    //         for (PipelineBuilderConcernExtension ext : PipelineBuilderConcernExtension.allExtensions()) {
    //             try {
    //                 PipelineBuilderConcern listener = ext.newListener();
    //                 listeners.add( listener );
    //             }
    //             catch (CoreException e) {
    //                 log.error( "Unable to create a new PipelineBuilderConcern: " + ext.getId() );
    //             }
    //         }
    //     }        
    // }

    protected List<PipelineBuilderConcern> createConcerns() {
        return FluentIterable.from( PipelineBuilderConcernExtension.all() )
                .transform( ext -> ext.newInstance() )
                .toList();
    };

    
    @FunctionalInterface
    protected interface Task {
        public void perform( PipelineBuilderConcern concern ) throws Exception;
    }

    
    protected void forEachConcern( List<PipelineBuilderConcern> concerns, Task task ) {
        for (PipelineBuilderConcern concern : concerns) {
            try {
                task.perform( concern );
            }
            catch (Exception e) {
                throw Throwables.propagate( e );
            }
        }
    }

    
    protected Pipeline createPipeline( String layerId, ProcessorSignature usecase, 
            DataSourceDescriptor dsd, Iterable<ProcessorDescriptor> chain ) 
            throws PipelineBuilderException {
        
        Pipeline pipeline = new Pipeline( usecase, dsd );
        for (ProcessorDescriptor procDesc : chain) {
            try {
                PipelineProcessor processor = procDesc.processor();
                PipelineProcessorSite site = createProcessorSite( procDesc );
                site.layerId.set( layerId );
                site.usecase.set( usecase );
                site.dsd.set( dsd );
                site.builder.set( this );
                processor.init( site );
                pipeline.addLast( procDesc );
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new PipelineBuilderException( e.getMessage(), e );
            }
        }
        return pipeline;
    }
    
    
    protected PipelineProcessorSite createProcessorSite( ProcessorDescriptor procDesc ) {
        return new PipelineProcessorSite( procDesc.params() );
    }

}
