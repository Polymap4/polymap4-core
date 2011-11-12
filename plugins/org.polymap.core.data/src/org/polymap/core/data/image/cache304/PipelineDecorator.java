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
package org.polymap.core.data.image.cache304;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.polymap.core.data.pipeline.IPipelineIncubationListener;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineProcessor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PipelineDecorator
        implements IPipelineIncubationListener {

    private static Log log = LogFactory.getLog( PipelineDecorator.class );


    public void pipelineCreated( Pipeline pipeline ) {
        if (pipeline.length() == 0) {
            return;
        }
        PipelineProcessor firstProc = pipeline.get( 0 );
        
        // XXX check compatibility instead of direct class

// explicitly done
//        if (firstProc instanceof ImageEncodeProcessor) {
//            log.debug( "Inserting before: " + firstProc );
//            pipeline.addFirst( new ImageCacheProcessor() );
//        }

// modifications are checked in ImageCacheProcessor
//        if (firstProc instanceof DataSourceProcessor
//                || firstProc instanceof FeatureTypeEditorProcessor
//                || firstProc instanceof FeatureBufferProcessor) {
//            log.debug( "Inserting before: " + firstProc );
//            pipeline.addFirst( new FeatureModificationProcessor() );
//        }
    }
    
}
