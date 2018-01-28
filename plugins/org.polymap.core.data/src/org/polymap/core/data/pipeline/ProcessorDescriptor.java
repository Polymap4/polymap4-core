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

import java.util.Collections;
import java.util.Map;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;

/**
 * Describes a processor in a {@link Pipeline}. Creates an actual processor instance
 * to be executed by an {@link PipelineExecutor}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProcessorDescriptor<P extends PipelineProcessor> {

    private Class<? extends PipelineProcessor> type;

    /** Lazily initialized by {@link #signature()}. */
    private ProcessorSignature          signature;
    
    private Map<String,String>          properties;
    
    private Lazy<P>                     processor;


    /**
     * 
     * @param type
     * @param properties The properties that are given to the
     *        {@link PipelineProcessor#init(PipelineProcessorSite)} method, or null.
     * @throws PipelineBuilderException 
     */
    public ProcessorDescriptor( Class<? extends PipelineProcessor> type, Map<String,String> properties )
            throws PipelineBuilderException {
        this.type = type;
        this.properties = properties != null ? properties : Collections.EMPTY_MAP;
        this.signature = new ProcessorSignature( type );

        this.processor = new LockedLazyInit( () -> {
            try {
                return (P)type.newInstance();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        });
    }


    /**
     * Constructs a descriptor for a use case.
     */
    public ProcessorDescriptor( ProcessorSignature signature ) {
        this.signature = signature;
    }


    public int hashCode() {
        int result = 1;
        result = 31 * result + ((type == null) ? 0 : type.getName().hashCode());
//        result = 31 * result + ((usecase == null) ? 0 : usecase.asString().hashCode());
        return result;
    }


    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        else if (obj instanceof ProcessorDescriptor) {
            ProcessorDescriptor rhs = (ProcessorDescriptor)obj;
            return type.equals( rhs.type );
        }
        return false;
    }


    public String toString() {
        return /*getClass().getSimpleName() + */ "[" 
                + (type != null ? type.getSimpleName() : "null")
                + "]";
    }


    public ProcessorSignature signature() throws PipelineBuilderException {
        return signature;
    }

    
    /** Immutable properties Map. */
    public Map<String,String> properties() {
        return properties;
    }


    public P processor() throws PipelineBuilderException { 
        return processor.get();
    }

    
    public Class<? extends PipelineProcessor> processorType() {
        return type;
    }


    /**
     * Checks if the {@link TerminalPipelineProcessor} is compatible with (aka can handle)
     * the given data source. 
     *
     * @param dsd The data source description to handle.
     * @throws PipelineBuilderException 
     */
    public boolean isCompatible( DataSourceDescriptor dsd ) throws PipelineBuilderException {
        return ((TerminalPipelineProcessor)processor()).isCompatible( dsd );
    }


    public void invoke( ProcessorProbe probe, ProcessorContext context ) throws Exception {
        signature().invoke( processor(), probe, context );
    }

}
