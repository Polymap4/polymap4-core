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
package org.polymap.core.catalog.resolve;

import java.util.Optional;
import java.util.Set;

import org.geotools.data.ServiceInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.catalog.IMetadata;


/**
 * Provides an implementation that delegates to GeoTools {@link ServiceInfo}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultServiceInfo
        implements IServiceInfo {

    private static Log log = LogFactory.getLog( DefaultServiceInfo.class );

    protected IMetadata             metadata;
    
    protected ServiceInfo           delegate;
    

    public DefaultServiceInfo( IMetadata metadata, ServiceInfo delegate ) {
        this.metadata = metadata;
        this.delegate = delegate;
    }

    @Override
    public IMetadata getMetadata() {
        return metadata;
    }

    @Override
    public IServiceInfo getServiceInfo() {
        return this;
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public Set<String> getKeywords() {
        return delegate.getKeywords();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable( delegate.getDescription() );
    }

}
