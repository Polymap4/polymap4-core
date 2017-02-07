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

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.geotools.data.ResourceInfo;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides an implementation that delegates to an GeoTools {@link ResourceInfo}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultResourceInfo
        extends DefaultResolveableInfo
        implements IResourceInfo {

    private static final Log log = LogFactory.getLog( DefaultResourceInfo.class );
    
    private ResourceInfo        delegate;


    public DefaultResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate ) {
        super( serviceInfo );
        this.delegate = delegate;
    }

//    @Override
//    public int hashCode() {
//        return delegate.getName().hashCode();
//    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        else if (obj instanceof DefaultResourceInfo) {
            DefaultResourceInfo rhs = (DefaultResourceInfo)obj;
            return Objects.equals( delegate.getName(), rhs.delegate.getName() )
                    && getServiceInfo().equals( rhs.getServiceInfo() );
        }
        return false;
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public Set<String> getKeywords() {
        Set<String> result = delegate.getKeywords();
        return result != null ? result : Collections.EMPTY_SET;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable( delegate.getDescription() );
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public ReferencedEnvelope getBounds() {
        return delegate.getBounds();
    }
    
}
