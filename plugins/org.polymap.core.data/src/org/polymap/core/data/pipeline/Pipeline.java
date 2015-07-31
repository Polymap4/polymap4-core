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
import java.util.List;

/**
 * Provides the API and implementation of a data processing pipeline.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Pipeline
        implements Iterable<PipelineProcessor> {

    private List<PipelineProcessor>     chain = new LinkedList();


    /**
     * Add the given procesor at the 'source' side of the pipeline.
     *
     * @param processor The processor to add.
     */
    public void addLast( PipelineProcessor processor ) {
        chain.add( chain.size(), processor );
    }


    /**
     * Add the given processor at the 'sink' side of the pipeline.
     *
     * @param processor The processor to add.
     */
    public void addFirst( PipelineProcessor processor ) {
        chain.add( 0, processor );
    }


    public void add( int i, PipelineProcessor processor ) {
        chain.add( i, processor );
    }


    public PipelineProcessor get( int index ) {
        assert index < chain.size();
        return chain.get( index );
    }


    public int length() {
        return chain.size();
    }


    /**
     * The iterator returns the processors in order from sink to source.
     */
    public Iterator<PipelineProcessor> iterator() {
        return chain.iterator();
    }


    public void process( ProcessorRequest request, ResponseHandler handler ) throws Exception {
        // XXX make this a preference and/or give it an API
        //new SerialPipelineExecutor().execute( this, request, handler );
        new DepthFirstStackExecutor().execute( this, request, handler );
    }

}
