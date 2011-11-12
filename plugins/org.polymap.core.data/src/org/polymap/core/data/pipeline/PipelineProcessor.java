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

import java.util.Properties;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.ILayer;

/**
 * The SPI of a data pipeline processor. A pipeline processor is part of a
 * {@link Pipeline} and executed inside of a {@link PipelineExecutor}. A
 * processor can process requests, reponses or both.
 * <p>
 * A processor has to be thread-safe and stateless. The state of the processor
 * can be stored in the {@link ProcessorContext}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 * @since 3.0
 */
public interface PipelineProcessor {
    
    /**
     *
     * @param props The persistent configuration properties. The following
     * properties are always set:
     * <ul>
     * <li>"layer" - the {@link ILayer} of this pipeline</li>
     * <li>"map" - the {@link IMap} of this pipeline</li>
     * <li>"service" - the {@link IService} of this pipeline</li>
     * </ul>
     */
    public void init( Properties props );
    
    public void processRequest( ProcessorRequest request, ProcessorContext context )
            throws Exception;

    public void processResponse( ProcessorResponse reponse, ProcessorContext context )
            throws Exception;

}
