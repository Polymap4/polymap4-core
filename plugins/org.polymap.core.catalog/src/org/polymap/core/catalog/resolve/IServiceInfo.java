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

import org.geotools.data.DataStore;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.runtime.StreamIterable;

/**
 * Information about a service which is coupled to an {@link IMetadata} entry.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IServiceInfo
        extends IResolvableInfo {
 
    /**
     * The metadata this service was created of.
     */
    public IMetadata getMetadata();
    
    /**
     * All resources of this service.
     * <p/>
     * This may <b>block</b> execution until backend service is available and/or
     * connected.
     *
     * @return  Usually a {@link StreamIterable}.
     */
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception;
    
    /**
     * 
     * <p/>
     * This may <b>block</b> execution until backend service is available and/or
     * connected.
     *
     * @return Newly created instance of the backend service (WebMapService,
     *         {@link DataStore}, etc.)
     */
    public <T> T createService( IProgressMonitor monitor ) throws Exception;
    
}
