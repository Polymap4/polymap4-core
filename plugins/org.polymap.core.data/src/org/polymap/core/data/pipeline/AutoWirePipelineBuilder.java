/*
 * polymap.org
 * Copyright 2009, Polymap GmbH. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.CoreException;

import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.Streams;
import org.polymap.core.runtime.Streams.ExceptionCollector;
import org.polymap.core.runtime.session.SessionSingleton;

/**
 * Once constructed this incubator stores and re-uses terminal and transformation
 * processors. Subsequent invocations
 * {@link #newPipeline(Class, DataSourceDescriptor, PipelineProcessorConfiguration[])}
 * will produce pipeline that may contain the same terminal or transformer instances!
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AutoWirePipelineBuilder
        extends PipelineBuilderBase {

    private static final Log log = LogFactory.getLog( AutoWirePipelineBuilder.class );

    private List<ProcessorDescriptor<PipelineProcessor>>         transformers = new ArrayList();

    private List<ProcessorDescriptor<TerminalPipelineProcessor>> terminals = new ArrayList();


    public AutoWirePipelineBuilder( ProcessorExtension... extensions ) {
        throw new RuntimeException( "not yet..." );
    }


    public AutoWirePipelineBuilder( Class<? extends PipelineProcessor>... procTypes ) {
        for (Class<? extends PipelineProcessor> procType : procTypes) {
            ProcessorDescriptor procDesc = new ProcessorDescriptor( procType, null );
            if (TerminalPipelineProcessor.class.isAssignableFrom( procType )) {
                terminals.add( procDesc );
            }
            else {
                transformers.add( procDesc );
            }
        }
    }


    /**
     * Session bound incubation listeners. 
     */
    static class Session
            extends SessionSingleton {

        private ListenerList<IPipelineIncubationListener> listeners = new ListenerList();

        public static Session instance() {
            return instance( Session.class );
        }

        protected Session() {
            for (PipelineListenerExtension ext : PipelineListenerExtension.allExtensions()) {
                try {
                    IPipelineIncubationListener listener = ext.newListener();
                    listeners.add( listener );
                }
                catch (CoreException e) {
                    log.error( "Unable to create a new IPipelineIncubationListener: " + ext.getId() );
                }
            }
        }
        
    }


    @Override
    public Pipeline newPipeline( 
            Class<? extends PipelineProcessor> usecaseType, 
            DataSourceDescriptor dsd,
            PipelineProcessorConfiguration[] procConfigs) 
            throws PipelineBuilderException {
        ProcessorSignature usecase = new ProcessorSignature( usecaseType );
        
        assert !usecase.requestIn.isEmpty()
                && usecase.requestOut.isEmpty()
                && !usecase.responseOut.isEmpty()
                && usecase.responseIn.isEmpty() : "PipelineUsecase must have requestIn and reponseOut only.";
                
        // swap requestIn/Out and reponseIn/Out to make an emitter signature for the start processor
        usecase.requestOut = usecase.requestIn;
        usecase.requestIn = Collections.EMPTY_SET;
        usecase.responseIn = usecase.responseOut;
        usecase.responseOut = Collections.EMPTY_SET;
        
        ProcessorDescriptor start = new ProcessorDescriptor( usecase );

        // terminal
        Iterable<ProcessorDescriptor<TerminalPipelineProcessor>> terms = findTerminals( usecase, dsd );

        // transformer chain
        LinkedList<ProcessorDescriptor> chain = null;
        int termCount = 0;
        for (ProcessorDescriptor term : terms) {
            termCount ++;
            chain = new LinkedList();
            if (findTransformation( start, term, usecase, chain )) {
                break;
            }
            else {
                log.debug( "No transformer chain for terminal: " + term );
            }
        }
        if (termCount == 0) {
            throw new PipelineBuilderException( "No terminal for data source: " + dsd );
        }
        else if (chain == null) {
            throw new PipelineBuilderException( "No transformer chain for: data source=" + dsd + ", usecase="  + usecase );
        }
        return createPipeline( usecase, dsd, chain );
    }


    protected Iterable<ProcessorDescriptor<TerminalPipelineProcessor>> findTerminals( 
            ProcessorSignature usecase, DataSourceDescriptor dsd ) 
            throws PipelineBuilderException {
        try (
            ExceptionCollector<PipelineBuilderException> excs = Streams.exceptions()
        ){
            return Streams.iterable( terminals.stream()
                    .filter( desc -> excs.check( () -> desc.isCompatible( dsd ) ) ) );
        }
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
            for (ProcessorDescriptor desc : transformers) {
                if (from.signature().isCompatible( desc.signature() ) && !chain.contains( desc )) {
                    chain.addLast( desc );
                    if (findTransformation( desc, to, usecase, chain )) {
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
