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

import java.util.HashMap;
import java.util.Map;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PipelineProcessorSite
        extends Configurable {

    @Mandatory
    @Immutable
    public Config<ProcessorSignature>       usecase;
    
    @Mandatory
    @Immutable
    public Config<DataSourceDescription>    dsd;

    @Immutable
    public Config<PipelineIncubator>        incubator;

    private Map<String,Object>              props = new HashMap();
    
    
    public PipelineProcessorSite( Map<String,Object> props ) {
        if (props != null) {
            this.props.putAll( props );
        }
    }

    public <T> T getProperty( String key ) {
        return (T)props.get( key );
    }
    
    public Iterable<String> propertyKeys() {
        throw new RuntimeException( "" );
    }
    
}
