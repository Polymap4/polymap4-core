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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.refractions.udig.catalog.IService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.CoreException;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.feature.AddFeaturesRequest;
import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.FeatureRenderProcessor2;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
import org.polymap.core.data.feature.ModifyFeaturesRequest;
import org.polymap.core.data.feature.ModifyFeaturesResponse;
import org.polymap.core.data.feature.RemoveFeaturesRequest;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetLayerTypesRequest;
import org.polymap.core.data.image.GetLayerTypesResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageDecodeProcessor;
import org.polymap.core.data.image.ImageEncodeProcessor;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.image.RasterRenderProcessor;
import org.polymap.core.data.image.WmsRenderProcessor;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.project.PipelineProcessorConfiguration;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DefaultPipelineIncubator
        implements IPipelineIncubator {

    private static Log log = LogFactory.getLog( DefaultPipelineIncubator.class );

    private static List<Class<? extends PipelineProcessor>>          transformers = new ArrayList();

    private static List<Class<? extends ITerminalPipelineProcessor>> terminals = new ArrayList();


    static {
        // XXX get transfomers/terminals from extensions
        synchronized (transformers) {
            log.info( "Initializing standard transformers..." );
            transformers.add( ImageEncodeProcessor.class );
            transformers.add( ImageDecodeProcessor.class );
        }
        synchronized (terminals) {
            log.info( "Initializing standard terminal processors..." );
            terminals.add( WmsRenderProcessor.class );
            terminals.add( FeatureRenderProcessor2.class );
            terminals.add( DataSourceProcessor.class );
            terminals.add( RasterRenderProcessor.class );

            for (ProcessorExtension ext : ProcessorExtension.allExtensions()) {
                if (ext.isTerminal()) {
                    try {
                        log.info( "    Terminal processor type found: " + ext.getId() );
                        terminals.add( (Class<? extends ITerminalPipelineProcessor>)ext.newProcessor().getClass() );
                    }
                    catch (Exception e) {
                        log.warn( e.getMessage(), e );
                    }
                }
            }
        }
    }


    public Pipeline newPipeline( LayerUseCase usecase, IMap map, ILayer layer, IService service )
    throws PipelineIncubationException {
        log.info( "New pipeline for service: " + service );
//        IGeoResource geores = layer.getGeoResource();

        // terminal
        List<ProcessorDescription> terms = findTerminals( service, usecase );
        if (terms.isEmpty()) {
            throw new PipelineIncubationException( "No terminal for service: " + service.getClass().getName() );
        }

        // transformer chain
        LinkedList<ProcessorDescription> chain = null;
        for (ProcessorDescription term : terms) {
            chain = new LinkedList();
            ProcessorDescription start = new ProcessorDescription( signatureForUsecase( usecase ) );

            if (findTransformation( start, term, usecase, chain )) {
                break;
            }
            else {
                log.debug( "No transformer chain for terminal: " + term );
            }
        }
        if (chain == null) {
            throw new PipelineIncubationException( "No transformer chain for: layer=" + layer + ", usecase="  + usecase );
        }

        // add layer specific processors
        if (layer != null) {
            PipelineProcessorConfiguration[] procConfigs = layer.getProcessorConfigs();
            for (PipelineProcessorConfiguration procConfig : procConfigs) {
                ProcessorExtension ext = ProcessorExtension.forExtensionId( procConfig.getExtensionId() );

                try {
                    PipelineProcessor processor = ext.newProcessor();
                    ProcessorDescription candidate = new ProcessorDescription(
                            processor.getClass(), procConfig.getConfig(), usecase );
                    int i = 0;
                    for (ProcessorDescription chainElm : chain) {
                        if (candidate.getSignature().isCompatible( chainElm.getSignature() )) {
                            log.debug( "      Insert configured processor: " + candidate.toString() + " at: " + i );
                            chain.add( i, candidate );
                            break;
                        }
                        i++;
                    }
                }
                catch (CoreException e) {
                    log.warn( e );
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );

                }
            }
        }

        // create the pipeline
        Pipeline pipeline = new Pipeline( map, layer, service );
        for (ProcessorDescription desc : chain) {
            PipelineProcessor processor = desc.newProcessor();
            Properties props = desc.getProps() != null ? desc.getProps() : new Properties();
            if (layer != null) {
                props.put( "layer", layer );
            }
            if (map != null) {
                props.put( "map", map );
            }
            props.put( "service", service );
            processor.init( props );
            pipeline.addLast( processor );
        }
        return pipeline;
    }


    protected List<ProcessorDescription> findTerminals( IService service, LayerUseCase usecase ) {
        List<ProcessorDescription> result = new ArrayList();
        for (ProcessorDescription desc : allTerminals( usecase )) {
            if (desc.isCompatible( service )) {
                log.info( "Terminal for '" + service + "' -- " + usecase + " : " + desc );
                result.add( desc );
            }
        }
        return result;
    }

    protected List<ProcessorDescription> allTerminals( LayerUseCase usecase ) {
        List<ProcessorDescription> result = new ArrayList( terminals.size() );
        for (Class<? extends PipelineProcessor> cl : terminals) {
            result.add( new ProcessorDescription( cl, null, usecase ) );
        }
        return result;
    }


    protected boolean findTransformation( ProcessorDescription from,
            ProcessorDescription to, LayerUseCase usecase, Deque<ProcessorDescription> chain ) {
        log.info( StringUtils.repeat( "    ", chain.size() ) + "findTransformation: " + from + " => " + to + " -- " + usecase );

        // recursion break
        if (chain.size() > 16) {
            return false;
        }

        // recursion start
        if (from.getSignature().isCompatible( to.getSignature() )) {
            chain.addLast( to );
            log.info( StringUtils.repeat( "    ", chain.size() ) + "Transformation found: " + chain );
            return true;
        }

        // recursion step
        else {
            for (ProcessorDescription desc : allTransformers( usecase )) {
                if (from.getSignature().isCompatible( desc.getSignature() )
                        && !chain.contains( desc )) {
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


    protected List<ProcessorDescription> allTransformers( LayerUseCase usecase ) {
        List<ProcessorDescription> result = new ArrayList( transformers.size() );
        for (Class<? extends PipelineProcessor> cl : transformers) {
            result.add( new ProcessorDescription( cl, null, usecase ) );
        }
        return result;
    }


    /**
     * Create a new signature for the given usecase.
     * <p>
     * XXX Use case and its signature are closely related. It is not a that good
     * idea to have both separated. However, currently the {@link LayerUseCase}
     * is part of the project bundle, which does not know about pipelines.
     *
     * @param usecase
     * @return Newly created signature for the given usecase.
     * @throws RuntimeException If no signature is found.
     */
    public static ProcessorSignature signatureForUsecase( LayerUseCase usecase ) {
        // WMS request/response signature
        if (usecase.isCompatible( LayerUseCase.ENCODED_IMAGE )) {
            return new ProcessorSignature(
                    new Class[] {},
                    new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
                    new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class},
                    new Class[] {} );
        }
        // upstream WMS service
        else if (usecase.isCompatible( LayerUseCase.IMAGE )) {
            return new ProcessorSignature(
                    new Class[] {},
                    new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
                    new Class[] {ImageResponse.class, GetLayerTypesResponse.class},
                    new Class[] {} );
        }
        // WFS-T request/response signature (before WFS)
        else if (usecase.isCompatible( LayerUseCase.FEATURES_TRANSACTIONAL )) {
            return new ProcessorSignature(
                    new Class[] {},
                    new Class[] {ModifyFeaturesRequest.class, RemoveFeaturesRequest.class, AddFeaturesRequest.class, GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
                    new Class[] {ModifyFeaturesResponse.class, GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class},
                    new Class[] {} );
        }
        // WFS request/response signature
        else if (usecase.isCompatible( LayerUseCase.FEATURES )) {
            return new ProcessorSignature(
                    new Class[] {},
                    new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
                    new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class},
                    new Class[] {} );
        }
        else {
            throw new RuntimeException( "No signature specified yet for usecase: " + usecase );
        }
    }

}
