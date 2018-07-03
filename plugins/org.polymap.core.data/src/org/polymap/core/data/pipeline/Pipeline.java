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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * A data processing pipeline is a chain of processors, represented by
 * {@link ProcessorDescriptor}s. A pipeline is created by a
 * {@link PipelineBuilder} and executed by a {@link PipelineExecutor}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Pipeline
        implements Iterable<ProcessorDescriptor> {

    private LinkedList<ProcessorDescriptor> chain = new LinkedList();
    
    private ProcessorSignature          usecase;
    
    private DataSourceDescriptor        dsd;


    public Pipeline( ProcessorSignature usecase, DataSourceDescriptor dsd ) {
        this.usecase = usecase;
        this.dsd = dsd;
    }

    
    public DataSourceDescriptor dataSourceDescription() {
        return dsd;
    }


    /**
     * Add the given procesor at the 'source' side of the pipeline.
     *
     * @param processor The processor to add.
     */
    public void addLast( ProcessorDescriptor procDesc ) {
        chain.add( chain.size(), procDesc );
    }


    /**
     * Add the given processor at the 'sink' side of the pipeline.
     *
     * @param processor The processor to add.
     */
    public void addFirst( ProcessorDescriptor procDesc ) {
        chain.add( 0, procDesc );
    }


    public void add( int i, ProcessorDescriptor procDesc ) {
        chain.add( i, procDesc );
    }


    public boolean remove( ProcessorDescriptor procDesc ) {
        return chain.remove( procDesc );
    }


    public ProcessorDescriptor get( int index ) {
        assert index < chain.size();
        return chain.get( index );
    }

    /**
     * Get the last processor at the 'source' side of the pipeline.
     */
    public ProcessorDescriptor getLast() {
        assert !chain.isEmpty();
        return chain.getLast();
    }


    public int length() {
        return chain.size();
    }


    /**
     * The iterator returns the processors in order from sink to source.
     */
    @Override
    public Iterator<ProcessorDescriptor> iterator() {
        return chain.iterator();
    }


    @Override
    public String toString() {
        return "Pipeline" + chain.stream().map( procDesc -> procDesc.processorType().getSimpleName() ).collect( Collectors.toList() );
    }
    
}
