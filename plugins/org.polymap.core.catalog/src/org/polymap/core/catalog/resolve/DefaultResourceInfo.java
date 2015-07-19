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

import java.util.Set;

import org.geotools.data.ResourceInfo;

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

    private static Log log = LogFactory.getLog( DefaultResourceInfo.class );
    
    private ResourceInfo        delegate;


    public DefaultResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate ) {
        super( serviceInfo );
        this.delegate = delegate;
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
    public String getDescription() {
        return delegate.getDescription();
    }


    @Override
    public String getName() {
        return delegate.getName();
    }

}
