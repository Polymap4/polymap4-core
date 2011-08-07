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

import java.util.ArrayList;
import java.util.List;

import java.net.URL;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolve.Status;
import net.refractions.udig.core.internal.CorePlugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.UIJob;

/**
 * Provides the default implementation that finds geo resources in the
 * {@link CatalogPlugin}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DefaultGeoResourceResolver
        implements IGeoResourceResolver {

    private static Log log = LogFactory.getLog( DefaultGeoResourceResolver.class );

    
    public List<IGeoResource> resolve( final String identifier )
    throws Exception {
        log.debug( "resolving: " + identifier );
        
        final List<IGeoResource> results = new ArrayList<IGeoResource>();
        final ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();

        UIJob job = new UIJob( Messages.get( "GeoResResolver_jobTitle" ) ) {
            public void runWithException( IProgressMonitor monitor ) 
            throws Exception {
                monitor.beginTask( Messages.get( "GeoResResolver_beginTask" ) + identifier, 5 );
                monitor.worked( 1 );
                
                URL url  = new URL( null, identifier, CorePlugin.RELAXED_HANDLER );
                List<IResolve> canditates = catalog.find( url, monitor );
                for (IResolve resolve : canditates) {
                    monitor.worked( 1 );
                    if (resolve.getStatus() == Status.BROKEN) {
                        continue;
                    }
                    if (resolve instanceof IGeoResource) {
                        results.add( (IGeoResource)resolve );
                    }
                }
                monitor.done();
            }
        };
        job.setShowProgressDialog( null, true );
        job.schedule();
        
        boolean success = job.joinAndDispatch( 15000 );
        
        if (!success) {
            job.cancelAndInterrupt();
        }
        
        return results;
    }


    public String createIdentifier( IGeoResource geores ) {
        // XXX check if geores exists
        return geores.getIdentifier().toExternalForm();
    }

}
