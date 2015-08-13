/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.pipeline;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

/**
 * Describes a data source a {@link TerminalPipelineProcessor} can handle or not. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DataSourceDescription
        extends Configurable {

    @Mandatory
    @Immutable
    public Config<Object>       service;

    @Mandatory
    @Immutable
    public Config<String>       resourceName;
    
    
    public DataSourceDescription() {
    }

    public DataSourceDescription( DataSourceDescription other ) {
        service.set( other.service.get() );
        resourceName.set( other.resourceName.get() );    
    }
    
}
