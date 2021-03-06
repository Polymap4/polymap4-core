/*
 * polymap.org
 * Copyright 2009-2018, Polymap GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;

/**
 * Once constructed this builder stores and re-uses terminal and transformation
 * processors. Subsequent invocations of
 * {@link #newPipeline(Class, DataSourceDescriptor, PipelineProcessor.Configuration[])}
 * will produce pipeline that may contain the same terminal or transformer instances!
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class AutoWirePipelineBuilder
        extends PipelineBuilderBase {

    private static final Log log = LogFactory.getLog( AutoWirePipelineBuilder.class );

    // hold types instead of ProcessorDescriptors to make sure
    // that new processor instances are created for each pipeline
    
    private List<Class<? extends PipelineProcessor>>    transformers = new ArrayList();

    private List<Class<? extends TerminalPipelineProcessor>> terminals = new ArrayList();


    public AutoWirePipelineBuilder( Class<? extends PipelineProcessor>... types ) {
        for (Class<? extends PipelineProcessor> type : types) {
            if (TerminalPipelineProcessor.class.isAssignableFrom( type )) {
                terminals.add( (Class<? extends TerminalPipelineProcessor>)type );
            }
            else {
                transformers.add( type );
            }
        }
    }


    @Override
    public Optional<Pipeline> createPipeline( String layerId, Class<? extends PipelineProcessor> usecase, 
            DataSourceDescriptor dsd ) throws PipelineBuilderException {
        return Optional.ofNullable( createPipeline( layerId, usecase, dsd, Collections.EMPTY_LIST ) );
    }


    protected Pipeline createPipeline(
            String layerId,
            Class<? extends PipelineProcessor> usecase, 
            DataSourceDescriptor dsd,
            List<ProcessorDescriptor> procs)
            throws PipelineBuilderException {

        List<PipelineBuilderConcern> concerns = createConcerns();
        forEachConcern( concerns, concern -> concern.preBuild( this, terminals, transformers ) );

        ProcessorSignature usecaseSig = new ProcessorSignature( usecase );
        assert !usecaseSig.requestIn.isEmpty()
                && usecaseSig.requestOut.isEmpty()
                && !usecaseSig.responseOut.isEmpty()
                && usecaseSig.responseIn.isEmpty() : "PipelineUsecase must have requestIn and reponseOut only.";
                
        // swap requestIn/Out and reponseIn/Out to make an emitter signature for the start processor
        usecaseSig.requestOut = usecaseSig.requestIn;
        usecaseSig.requestIn = Collections.EMPTY_SET;
        usecaseSig.responseIn = usecaseSig.responseOut;
        usecaseSig.responseOut = Collections.EMPTY_SET;
        
        ProcessorDescriptor start = new ProcessorDescriptor( usecaseSig );
        forEachConcern( concerns, concern -> concern.startBuild( this, layerId, dsd, usecase, start ) );
        
        // terminal
        List<ProcessorDescriptor<TerminalPipelineProcessor>> terms = findTerminals( usecaseSig, dsd );
        forEachConcern( concerns, concern -> concern.terminals( this, terms ) );

        // transformer chain
        LinkedList<ProcessorDescriptor> chain = new LinkedList();
        int termCount = 0;
        for (ProcessorDescriptor term : terms) {
            termCount ++;
            if (findTransformation( start, term, usecaseSig, chain )) {
                break;
            }
            else {
                log.debug( "No transformer chain for terminal: " + term );
            }
        }
        forEachConcern( concerns, concern -> concern.transformations( this, chain ) );
        if (termCount == 0) {
            //throw new PipelineBuilderException( "No terminal for data source: " + dsd );
            return null;
        }
        else if (chain.isEmpty()) {
            //throw new PipelineBuilderException( "No transformer chain for: data source=" + dsd + ", usecase="  + usecaseSig );
            return null;
        }
        
        // additional processors
        for (ProcessorDescriptor candidate : procs) {
            if (usecaseSig.isCompatible( candidate.signature() )) {
                chain.add( 0, candidate );                
            }
            int index = 1;
            for (ProcessorDescriptor cursor : chain) {
                if (cursor.signature().isCompatible( candidate.signature() )) {
                    chain.add( index, candidate );
                    break;
                }
                index ++;
            }
//            if (index > chain.size()) {
//                throw new RuntimeException( "No compatible parent found for: " + candidate );
//            }
        }
        forEachConcern( concerns, concern -> concern.additionals( this, chain ) );
        
        Pipeline pipeline = createPipeline( layerId, usecaseSig, dsd, chain );
        forEachConcern( concerns, concern -> concern.postBuild( this, pipeline ) );
        return pipeline;
    }


    protected List<ProcessorDescriptor<TerminalPipelineProcessor>> findTerminals( 
            ProcessorSignature usecase, DataSourceDescriptor dsd ) 
            throws PipelineBuilderException {
        // f*ck streaming has no Exception handling
        //return FluentIterable.from( terminals ).filter( desc -> desc.isCompatible( dsd ) );
        List<ProcessorDescriptor<TerminalPipelineProcessor>> result = new ArrayList( terminals.size() );
        for (Class<? extends TerminalPipelineProcessor> type : terminals) {
            ProcessorDescriptor candidate = new ProcessorDescriptor( type, Params.EMPTY );
            if (candidate.isCompatible( dsd )) {
                result.add( candidate );
            }
        }
        return result;
    }
    

    protected boolean findTransformation( 
            ProcessorDescriptor from, ProcessorDescriptor to, 
            ProcessorSignature usecase, Deque<ProcessorDescriptor> chain ) 
            throws PipelineBuilderException {
        log.debug( StringUtils.repeat( "    ", chain.size() ) + "findTransformation: " + from + " => " + to + " -- " + usecase );

        // recursion break
        if (chain.size() > 16) {
            return false;
        }

        // recursion start
        if (from.signature().isCompatible( to.signature() )) {
            chain.addLast( to );
            log.debug( StringUtils.repeat( "    ", chain.size() ) + "Transformation found: " + chain );
            return true;
        }

        // recursion step
        else {
            for (Class<? extends PipelineProcessor> type : transformers) {
                ProcessorDescriptor candidate = new ProcessorDescriptor( type, Params.EMPTY );
                if (from.signature().isCompatible( candidate.signature() ) && !chain.contains( candidate )) {
                    chain.addLast( candidate );
                    if (findTransformation( candidate, to, usecase, chain )) {
                        //log.debug( "      transformation found: " + desc );
                        return true;
                    }
                    chain.removeLast();
                }
            }
            return false;
        }
    }

}
