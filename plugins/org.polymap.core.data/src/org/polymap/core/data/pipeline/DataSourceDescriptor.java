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

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

/**
 * Describes a data source so that a {@link TerminalPipelineProcessor} can decide if
 * it is able to handle it.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DataSourceDescriptor
        extends Configurable {

    @Mandatory
    @Immutable
    public Config2<DataSourceDescriptor,Object>   service;

    @Mandatory
    @Immutable
    public Config2<DataSourceDescriptor,String>   resourceName;
    
    
    public DataSourceDescriptor() {
    }

    public DataSourceDescriptor( Object service, String resourceName ) {
        this.service.set( service );
        this.resourceName.set( resourceName );
    }

    public DataSourceDescriptor( DataSourceDescriptor other ) {
        service.set( other.service.get() );
        resourceName.set( other.resourceName.get() );    
    }

    @Override
    public String toString() {
        return "DataSourceDescription [service=" + service.get() + ", resourceName=" + resourceName.get() + "]";
    }
    
}
