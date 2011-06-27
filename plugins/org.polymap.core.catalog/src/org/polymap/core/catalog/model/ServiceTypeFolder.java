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
package org.polymap.core.catalog.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A folder for all service of a type. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class ServiceTypeFolder 
        implements IResolveFolder, IResolve {

    protected ICatalog          catalog;
    
    protected List<IResolve>    services = new ArrayList();
    
    protected Class<? extends IResolve> type;
    
    protected URL               id;
    
    
    public ServiceTypeFolder( ICatalog catalog, IResolve service ) {
        this.catalog = catalog;
        this.services.add( service );
        this.type = service.getClass();
        try {
            id = new URL( "http://localhost/" + type.getSimpleName() ); //$NON-NLS-1$
        } 
        catch (MalformedURLException e) {
            // do nothing
        }
    }

    public ImageDescriptor getIcon( IProgressMonitor monitor ) {
        // XXX falko: give the folder the icon of the services; does not work;
        // does not seem to be called anyway
        if (!services.isEmpty()) {
            IResolve service = services.get( 0 );
            if (service instanceof IService) {
                try {
                    IServiceInfo info = ((IService)service).getInfo( monitor );
                    return info.getImageDescriptor();
                }
                catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public IService getService( IProgressMonitor monitor ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public String getTitle() {
        String typeName = type.getSimpleName();
        if (StringUtils.contains( typeName, "Post" )) {
            return "PostGIS";
        }
        else if (StringUtils.contains( typeName, "Shp" )) {
            return "Shapefile";
        }
        else if (StringUtils.contains( typeName, "WMS" )) {
            return "WMS";
        }
        else if (StringUtils.contains( typeName, "WFS" )) {
            return "WFS";
        }
        else if (StringUtils.contains( typeName, "Oracle" )) {
            return "Oracle";
        }
        else if (StringUtils.contains( typeName, "MySQL" )) {
            return "MySQL";
        }
        else if (StringUtils.containsIgnoreCase( typeName, "GeoTiff" )) {
            return "GeoTIFF";
        }
        else if (StringUtils.containsIgnoreCase( typeName, "DXF" )) {
            return "DXF";
        }
        else {
            String result = StringUtils.remove( typeName, "Impl" );
            result = StringUtils.remove( result, "Service" );
            return result;
        }
    }

    public void dispose( IProgressMonitor monitor ) {
    }

    public ID getID() {
        return new ID( getIdentifier() );
    }

    public URL getIdentifier() {
        return id;
    }

    public Throwable getMessage() {
        return null;
    }

    public Status getStatus() {
        return Status.CONNECTED;
    }

    public List<IResolve> members( IProgressMonitor monitor )
            throws IOException {
        return Collections.unmodifiableList( services );
    }

    public IResolve parent( IProgressMonitor monitor )
            throws IOException {
        return catalog;
    }

    public <T> boolean canResolve( Class<T> adaptee ) {
        return false;
    }

    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor )
            throws IOException {
        return null;
    }
    
}
