/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.project;

import java.util.List;

import net.refractions.udig.catalog.IGeoResource;

/**
 * A resource resolver provides the logic to find the actual
 * {@link IGeoResource} for a given {@link ILayer}. Instances are created via
 * {@link ProjectPlugin#geoResourceResolver(ILayer)}.
 * <p>
 * The interface is the bridge between the packages
 * {@link org.polymap.core.project} and {@link org.refractions.udig.catalog}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface IGeoResourceResolver {

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
