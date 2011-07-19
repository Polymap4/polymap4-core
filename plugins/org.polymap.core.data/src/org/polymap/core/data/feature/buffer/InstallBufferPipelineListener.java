/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
 */
package org.polymap.core.data.feature.buffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.pipeline.IPipelineIncubationListener;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.project.ILayer;

/**
 * Installs a buffer processor in every feature pipeline using the
 * {@link LayerFeatureBufferManager}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class InstallBufferPipelineListener
        implements IPipelineIncubationListener {

    private static Log log = LogFactory.getLog( InstallBufferPipelineListener.class );


    public void pipelineCreated( Pipeline pipeline ) {
        PipelineProcessor source = pipeline.get( pipeline.length() - 1 );

        if (source instanceof DataSourceProcessor) {
            if (pipeline.getLayers().size() > 1) {
                log.warn( "Pipeline with more that one layer!" );
            }
            ILayer layer = pipeline.getLayers().iterator().next();
            LayerFeatureBufferManager bufferManager = LayerFeatureBufferManager.forLayer( layer, true );
            pipeline.add( pipeline.length() - 1, bufferManager.getProcessor() );
            log.debug( "pipelineCreated(): buffer processor added to pipeline = " + pipeline );
        }
    }

}
