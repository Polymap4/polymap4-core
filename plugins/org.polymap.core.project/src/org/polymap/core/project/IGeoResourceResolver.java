/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project;

import java.util.List;

import org.polymap.core.runtime.Callback;

/**
 * A resource resolver provides the logic to find the actual {@link IGeoResource} for
 * a given {@link ILayer}. Instances are created via
 * {@link ProjectPlugin#geoResourceResolver(ILayer)}.
 * <p/>
 * The interface is the bridge between {@link org.polymap.core.project} and
 * {@link org.refractions.udig.catalog}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public interface IGeoResourceResolver {

    /**
     * Find geo resources in the catalog and handle results asynchronously.
     *     
     * @see #resolve(String)
     * @param identifier
     * @param handler
     * @throws Exception
     */
    void resolve( String identifier, Callback<List<IGeoResource>> handler )
    throws Exception;
    
    
    /**
     * Used to find the associated service in the catalog.
     * <p>
     * On the off chance *no* services exist an empty list is returned. All this
     * means is that the service is down, or the user has not connected to it
     * yet (perhaps they are waiting on security permissions.
     * <p>
     * When the real service comes along we will find out based on catalog
     * events.
     * <p>
     * getGeoResource() is a blocking method but it must not block UI thread.
     * With this purpose the new imlementation is done to avoid UI thread
     * blocking because of synchronization.
     */
    public List<IGeoResource> resolve( String identifier )
    throws Exception;
    
    public String createIdentifier( IGeoResource res );

}
