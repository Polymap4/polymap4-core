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
 * {@link #newPipeline(Class, DataSourceDescription, PipelineProcessorConfiguration[])}
 * will produce pipeline that may contain the same terminal or transformer instances!
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultPipelineIncubator
        implements PipelineIncubator {

    private static Log log = LogFactory.getLog( DefaultPipelineIncubator.class );

    private List<ProcessorDescription<PipelineProcessor>>         transformers = new ArrayList();

    private List<ProcessorDescription<TerminalPipelineProcessor>> terminals = new ArrayList();


    public DefaultPipelineIncubator( ProcessorExtension... extensions ) {
        throw new RuntimeException( "not yet..." );
    }


    public DefaultPipelineIncubator( Class<? extends PipelineProcessor>... procTypes ) {
        for (Class<? extends PipelineProcessor> procType : procTypes) {
            ProcessorDescription procDesc = new ProcessorDescription( procType, null );
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
            DataSourceDescription dsd,
            PipelineProcessorConfiguration[] procConfigs) 
            throws PipelineIncubationException {
        ProcessorSignature usecase = new ProcessorSignature( usecaseType );
        
        assert !usecase.requestIn.isEmpty()
                && usecase.requestOut.isEmpty()
                && !usecase.responseOut.isEmpty()
                && usecase.responseIn.isEmpty() : "PipelineUsecase must have requestIn and reponseOut only.";
                
        // swap requestIn/Out and reposnseIn/Out to make an emitter signature for the start processor
        usecase.requestOut = usecase.requestIn;
        usecase.requestIn = Collections.EMPTY_SET;
        usecase.responseIn = usecase.responseOut;
        usecase.responseOut = Collections.EMPTY_SET;
        
        ProcessorDescription start = new ProcessorDescription( usecase );

        // terminal
        Iterable<ProcessorDescription<TerminalPipelineProcessor>> terms = findTerminals( usecase, dsd );

        // transformer chain
        LinkedList<ProcessorDescription> chain = null;
        int termCount = 0;
        for (ProcessorDescription term : terms) {
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
            throw new PipelineIncubationException( "No terminal for data source: " + dsd );
        }
        else if (chain == null) {
            throw new PipelineIncubationException( "No transformer chain for: data source=" + dsd + ", usecase="  + usecase );
        }

//        // add layer specific processors
//        for (PipelineProcessorConfiguration procConfig : procConfigs) {
//            ProcessorExtension ext = ProcessorExtension.forExtensionId( procConfig.getExtensionId() );
//
//            if (ext == null) {
//                log.warn( "No processor extension found for: " + procConfig.getExtensionId() + "!!!" );
//                break;
//            }
//            try {
//                PipelineProcessor processor = ext.newProcessor();
//                ProcessorDescription candidate = new ProcessorDescription(
//                        processor.getClass(), procConfig.getConfig(), usecase );
//                int i = 0;
//                for (ProcessorDescription chainElm : chain) {
//                    if (candidate.signature().isCompatible( chainElm.signature() )) {
//                        log.debug( "      Insert configured processor: " + candidate.toString() + " at: " + i );
//                        chain.add( i, candidate );
//                        break;
//                    }
//                    i++;
//                }
//            }
//            catch (CoreException e) {
//                log.warn( e );
//                PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
//
//            }
//        }

        // create the pipeline
        Pipeline pipeline = new Pipeline( usecase, dsd );
        for (ProcessorDescription procDesc : chain) {
            try {
                PipelineProcessor processor = procDesc.processor();
                PipelineProcessorSite procSite = createProcessorSite( procDesc );
                procSite.usecase.set( usecase );
                procSite.dsd.set( dsd );
                procSite.incubator.set( this );
                processor.init( procSite );
                pipeline.addLast( procDesc );
            }
            catch (Exception e) {
                throw new PipelineIncubationException( e.getMessage(), e );
            }
        }
        
        // call listeners
        log.info( "FIXME: Session bound Pipeline listener COMMENTED OUT!" );
//        for (IPipelineIncubationListener listener : Session.instance().listeners) {
//            listener.pipelineCreated( pipeline );
//        }

        return pipeline;
    }


    protected PipelineProcessorSite createProcessorSite( ProcessorDescription procDesc ) {
        return new PipelineProcessorSite( procDesc.getProps() );
    }


    protected Iterable<ProcessorDescription<TerminalPipelineProcessor>> findTerminals( 
            ProcessorSignature usecase, DataSourceDescription dsd ) 
            throws PipelineIncubationException {
        
        try (
            ExceptionCollector<PipelineIncubationException> excs = Streams.exceptions()
        ){
            return Streams.iterable( terminals.stream()
                    .filter( desc -> excs.check( () -> desc.isCompatible( dsd ) ) ) );
        }
    }
    

    protected boolean findTransformation( 
            ProcessorDescription from, ProcessorDescription to, 
            ProcessorSignature usecase, Deque<ProcessorDescription> chain ) 
            throws PipelineIncubationException {
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
            for (ProcessorDescription desc : transformers) {
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


//    /**
//     * Create a new signature for the given usecase.
//     * <p>
//     * XXX Use case and its signature are closely related. It is not a that good
//     * idea to have both separated. However, currently the {@link LayerUseCase}
//     * is part of the project bundle, which does not know about pipelines.
//     *
//     * @param usecase
//     * @return Newly created signature for the given usecase.
//     * @throws RuntimeException If no signature is found.
//     */
//    public static ProcessorSignature signatureForUsecase( LayerUseCase usecase ) {
//        // WMS request/response signature
//        if (usecase.isCompatible( LayerUseCase.ENCODED_IMAGE )) {
//            return new ProcessorSignature(
//                    new Class[] {},
//                    new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
//                    new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class},
//                    new Class[] {} );
//        }
//        // upstream WMS service
//        else if (usecase.isCompatible( LayerUseCase.IMAGE )) {
//            return new ProcessorSignature(
//                    new Class[] {},
//                    new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
//                    new Class[] {ImageResponse.class, GetLayerTypesResponse.class},
//                    new Class[] {} );
//        }
//        // WFS-T request/response signature (before WFS)
//        else if (usecase.isCompatible( LayerUseCase.FEATURES_TRANSACTIONAL )) {
//            return new ProcessorSignature(
//                    new Class[] {},
//                    new Class[] {ModifyFeaturesRequest.class, RemoveFeaturesRequest.class, AddFeaturesRequest.class, GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
//                    new Class[] {ModifyFeaturesResponse.class, GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class},
//                    new Class[] {} );
//        }
//        // WFS request/response signature
//        else if (usecase.isCompatible( LayerUseCase.FEATURES )) {
//            return new ProcessorSignature(
//                    new Class[] {},
//                    new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
//                    new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class},
//                    new Class[] {} );
//        }
//        else {
//            throw new RuntimeException( "No signature specified yet for usecase: " + usecase );
//        }
//    }

}
