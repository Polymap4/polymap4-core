/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.data.pipeline;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.refractions.udig.catalog.IService;

//import org.polymap.core.data.feature.buffer.FeatureBufferProcessor;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

/**
 * Provides the API and implementation of a data processing pipeline.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class Pipeline
        implements Iterable<PipelineProcessor> {

//    private AtomicBoolean       isWorking = new AtomicBoolean( false );

    private IMap                        map;

    private Set<ILayer>                 layers = new HashSet();

    private IService                    service;

    private List<PipelineProcessor>     chain = new LinkedList();


    /**
     *
     * @param map The map we are working for.
     * @param layer
     * @param service The terminal service of this pipeline.
     */
    public Pipeline( IMap map, ILayer layer, IService service ) {
        super();
        this.map = map;
        this.service = service;
        addLayer( layer );
    }


    public IMap getMap() {
        return map;
    }

    public Set<ILayer> getLayers() {
        return layers;
    }

    public boolean addLayer( ILayer layer ) {
       return layers.add( layer );
    }

    /**
     * The terminal service of this pipeline.
     */
    public IService getService() {
        return service;
    }


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


    public void process( ProcessorRequest request, ResponseHandler handler )
            throws Exception {
        // XXX make this a preference and/or give it an API
        //new SerialPipelineExecutor().execute( this, request, handler );
        new DepthFirstStackExecutor().execute( this, request, handler );
    }

}
