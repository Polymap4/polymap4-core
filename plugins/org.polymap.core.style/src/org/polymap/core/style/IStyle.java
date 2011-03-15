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
import java.util.List;

import java.io.IOException;

import net.refractions.udig.catalog.IResolve;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Provides a handle of a style resource in a {@link IStyleCatalog}. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class IStyle
        implements IResolve {

    private IStyleInfo          info;
    
    
    public synchronized IStyleInfo getInfo( IProgressMonitor monitor ) {
        if (info == null) {
            try {
                info = resolve( IStyleInfo.class, monitor );
            }
            catch (IOException e) {
                throw new RuntimeException( e.getLocalizedMessage(), e );
            }
        }
        return info;
    }
    
    public String getTitle() {
        return info != null ? info.getTitle() : null;
    }

    /**
     * Will attempt to morph into the adaptee, and return that object.
     * This provides access the the underlying style representation. Concrete
     * implementation provide their typical API to access the style info.
     *
     * @see IResolve#resolve(java.lang.Class, org.eclipse.core.runtime.IProgressMonitor)
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor )
    throws IOException {
        assert adaptee != null : "No adaptor specified";
        monitor = monitor != null ? monitor : new NullProgressMonitor();

        if (adaptee.isAssignableFrom( IStyleInfo.class )) {
            return adaptee.cast( createInfo( monitor ) );
        }
//        ...
//        IResolveManager rm = CatalogPlugin.getDefault().getResolveManager();
//        if (rm.canResolve(this, adaptee)) {
//            return rm.resolve(this, adaptee, monitor);
//        }
        return null; // could not find adapter    
    } 

    
    public <T> boolean canResolve( Class<T> adaptee ) {
        try {
            Object value = resolve( adaptee, null );
            return value != null;
        }
        catch (IOException e) {
            throw new RuntimeException( e.getLocalizedMessage(), e );
        }
    }


    protected abstract Object createInfo( IProgressMonitor monitor );

    public abstract String createSLD( IProgressMonitor monitor );


    /**
     * Attempts to store the style resource represented by this object to its
     * underlying catalog.
     * 
     * @param monitor
     * @throws IOException
     * @throws {@link UnsupportedOperationException} If the underlying catalog
     *         does not support storing/updating its styles.
     */
    public abstract void store( IProgressMonitor monitor )
    throws UnsupportedOperationException, IOException;
    
    
    public void dispose( IProgressMonitor monitor ) {
    }

    public Status getStatus() {
        return Status.CONNECTED;
    }

    public Throwable getMessage() {
        return null;
    }

    public List<IResolve> members( IProgressMonitor monitor )
            throws IOException {
        return Collections.EMPTY_LIST;
    }

    public IResolve parent( IProgressMonitor monitor )
            throws IOException {
        return null;
    }

}
