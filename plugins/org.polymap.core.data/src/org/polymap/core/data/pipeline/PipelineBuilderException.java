/* 
 * polymap.org
 * Copyright 2009-2018, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.pipeline;

/**
 * Thrown if an {@link PipelineBuilder} cannot find and/or instantiate
 * all processors of a pipeline. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class PipelineBuilderException
        extends RuntimeException {

    public PipelineBuilderException( String msg ) {
        super( msg );
    }

    public PipelineBuilderException( String msg, Throwable cause ) {
        super( msg, cause );
    }

}
