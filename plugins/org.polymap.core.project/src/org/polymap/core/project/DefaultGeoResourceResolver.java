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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolve.Status;
import net.refractions.udig.core.internal.CorePlugin;
import net.refractions.udig.ui.PlatformJobs;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.operation.JobMonitors;

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

    public List<IGeoResource> resolve( final String identifier )
    throws Exception {
        final List<IGeoResource> results = new ArrayList<IGeoResource>();
        final ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();

        IProgressMonitor monitor = JobMonitors.get() != null
                ? JobMonitors.get() : new NullProgressMonitor();

        IRunnableWithProgress runnable = new IRunnableWithProgress(){
            public void run( IProgressMonitor _monitor ) 
            throws InvocationTargetException {
                try {
                    URL url  = new URL( null, identifier, CorePlugin.RELAXED_HANDLER );
                    List<IResolve> canditates = catalog.find( url, _monitor );
                    for (IResolve resolve : canditates) {
                        if (resolve.getStatus() == Status.BROKEN) {
                            continue;
                        }
                        if (resolve instanceof IGeoResource) {
                            results.add( (IGeoResource)resolve );
                        }
                    }
                } 
                catch (Exception e) {
                    //e.printStackTrace();
                    throw new InvocationTargetException( e );
                }
            }
        };
        if (Display.getCurrent() != null) {
            PlatformJobs.runSync( runnable, monitor );
        }
        else {
            runnable.run( monitor );
        }

        return results;
    }


    public String createIdentifier( IGeoResource geores ) {
        // XXX check if geores exists
        return geores.getIdentifier().toExternalForm();
    }

}
