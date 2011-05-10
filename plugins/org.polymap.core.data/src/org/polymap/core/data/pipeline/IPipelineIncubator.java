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

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.LayerUseCase;

/**
 * Provides the logic to create a {@link Pipeline} out of:
 * <ul>
 * <li>{@link IGeoResource}s (of an {@link ILayer} or {@link IMap})</li>
 * <li>processor descriptions (of an {@link ILayer} or {@link IMap})</li>
 * <li>a given {@link LayerUseCase}</li>
 * </ul>
 * The interface is the bridge between the packages
 * <code>org.polymap.core.project</code> and <code>org.polymap.core.data</code>.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface IPipelineIncubator {

    static IPipelineIncubator       instance = new DefaultPipelineIncubator();


    public Pipeline newPipeline( LayerUseCase usecase, IMap map, ILayer layer,
            IService service)
            throws PipelineIncubationException;

}
