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

/**
 * Tagging interface for processors that produce data. A terminal processor is the
 * start point of a {@link Pipeline}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface TerminalPipelineProcessor
        extends PipelineProcessor {

    public boolean isCompatible( DataSourceDescription dsd );
    
}
