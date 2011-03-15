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
package org.polymap.core.style;

import java.util.Collections;
import java.util.Set;

import org.geotools.data.FeatureSource;
import org.geotools.util.WeakHashSet;

import org.eclipse.core.runtime.IProgressMonitor;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeListener;
import net.refractions.udig.catalog.ISearch;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class IStyleCatalog
        extends ISearch {

    private Set<IResolveChangeListener> listeners = Collections.synchronizedSet( new WeakHashSet( IResolveChangeListener.class ) );

    /**
     * Check for an exact match with provided id.
     * 
     * @param id id used for lookup
     * @param monitor
     * @return Resolve or null if not found
     */
    public abstract IStyle getById( ID id, IProgressMonitor monitor);
    
    public <T extends IResolve> T getById( Class<T> type, ID id, IProgressMonitor monitor ) {
        if (type.isAssignableFrom( IStyle.class )) {
            return (T)getById( id, monitor );
        }
        return null;
    }
   
    
    /**
     * Adds the specified entry to this catalog.
     * <p>
     * In some cases the catalog will be backed onto an
     * external server, which may not allow for additions.
     * <p>
     * An IService may belong to more than one Catalog.
     * </p>
     * 
     * @param entry
     * @throws UnsupportedOperationException
     */
    public abstract void add( IStyle style ) 
    throws UnsupportedOperationException;

    /**
     * Removes the specified entry to this catalog. In some cases the catalog will be backed onto a
     * server, which may not allow for deletions.
     * 
     * @param service
     * @throws UnsupportedOperationException
     */
    public abstract void remove( IStyle style ) 
    throws UnsupportedOperationException;

    /**
     * Replaces the specified entry in this catalog.
     * <p>
     * In some cases the catalog will be backed onto a server, which may not
     * allow for deletions.
     * <p>
     * This method can be used for two things:
     * <ul>
     * <li>ResetService (Action): calls this method with id == service.getID() in order
     * to "reset" the IService handle with a fresh one. This can be used to replace a
     * catalog entry that has locked up.
     * <li>This method can also be used to *move* an existing service (the one with the
     * indicated ID) with a new replacement). 
     * </ul>
     * <p>
     * This replace method has two differences from a simple *remove( id )* and *add( replacement)* 
     * <ul>
     * <li>A difference series of events is generated; letting client code know that they
     * should update the ID they were using to track this resource.
     * <li>An new IForward( ID, replacement.getID()) is left in the catalog as a place holder to
     * order to let any client that was off-line know what happened next time they come to call.
     * </ul>
     * @param id ID of the service to replace, the service with this ID will be removed
     * @param replacement Replacement IService handle; indicating where the service has moved to
     * @throws UnsupportedOperationException
     */
    public abstract void replace( ID id, IStyle replacement ) 
    throws UnsupportedOperationException;
    
    
    /**
     * Generate a default style for the given geores.
     * 
     * @param fs
     * @return The newly created Style.
     */
    public abstract IStyle createDefaultStyle( FeatureSource fs  );

    
    /**
     * Add a listener to notice when the a resource changes.
     * 
     * @param listener
     */
    public void addCatalogListener( IResolveChangeListener listener ) {
        listeners.add( listener );
    }

    /**
     * Remove a listener that was watching for resource changes.
     * 
     * @param listener
     */
    public void removeListener( IResolveChangeListener listener ) {
        listeners.remove( listener );
    }
    
}
