/* 
 * polymap.org
 * Copyright (C) 2015-2018, Falko Bräutigam. All rights reserved.
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

import java.util.Collections;
import java.util.Map;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

/**
 * The runtime environment of a {@link PipelineProcessor}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PipelineProcessorSite
        extends Configurable {

    @Mandatory
    @Immutable
    public Config<ProcessorSignature>   usecase;
    
    @Mandatory
    @Immutable
    public Config<DataSourceDescriptor> dsd;

    @Immutable
    public Config<PipelineBuilder>      builder;

    protected Map<String,Object>        properties;
    
    
    public PipelineProcessorSite( Map<String,Object> properties ) {
        this.properties = properties != null ? properties : Collections.EMPTY_MAP;
    }

    public <T> T getProperty( String key ) {
        return (T)properties.get( key );
    }
    
    public Iterable<String> propertyKeys() {
        throw new RuntimeException( "..." );
    }
    
}
