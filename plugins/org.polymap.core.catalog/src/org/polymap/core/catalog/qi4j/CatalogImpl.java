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
package org.polymap.core.catalog.qi4j;

import java.util.Collections;
import java.util.List;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;

import com.vividsolutions.jts.geom.Envelope;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ICatalogInfo;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeEvent;
import net.refractions.udig.catalog.IResolveChangeListener;
import net.refractions.udig.catalog.IResolveDelta;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.ResolveChangeEvent;
import net.refractions.udig.catalog.internal.ResolveDelta;

import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.catalog.CatalogPlugin;
import org.polymap.core.catalog.CatalogRepository;
import org.polymap.core.model.ACL;
import org.polymap.core.model.AclPermission;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * A facade of {@link CatalogComposite} providing the {@link ICatalog}
 * API to the user. This is necessary because ICatalog is an abstract class
 * (instead of an interface), which an entity composite cannot directly
 * extend.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class CatalogImpl
        extends ICatalog
        implements ACL, IAdaptable {

    private CatalogComposite        delegate;
    
    private final ListenerList      catalogListeners;


    public CatalogImpl() {
        delegate = Polymap.getSessionDisplay() != null
                ? CatalogRepository.instance().getCatalog()    
                : CatalogRepository.globalInstance().getCatalog();    
        delegate.setFacade( this );
        
        catalogListeners = new ListenerList( ListenerList.IDENTITY );
        //catalogListeners = Collections.synchronizedSet( new WeakHashSet<IResolveChangeListener>() );
    }
    

    public Object getAdapter( Class adapter ) {
        if (ACL.class.isAssignableFrom( adapter )) {
            return delegate;
        }
        // WorkbenchAdapter to support labeling
        if (IWorkbenchAdapter.class.isAssignableFrom( adapter )) {
            return new WorkbenchAdapter() {
                public String getLabel( Object object ) {
                    return getTitle();
                }
            };
        }
        // try AdapterManager
        return Platform.getAdapterManager().getAdapter( this, adapter );
    }

    
    // ACL ************************************************
    
    public boolean addPermission( String principal, AclPermission... permissions ) {
        return delegate.addPermission( principal, permissions );
    }

    public boolean checkPermission( Principal principal, AclPermission permission ) {
        return delegate.checkPermission( principal, permission );
    }

    public boolean removePermission( String principal, AclPermission... permissions ) {
        return delegate.removePermission( principal, permissions );
    }

    public Iterable<Entry> entries() {
        return delegate.entries();
    }


    // events *********************************************
    
    public void addCatalogListener( IResolveChangeListener listener ) {
        catalogListeners.add( listener );
    }

    public void addListener( IResolveChangeListener listener ) {
        catalogListeners.add( listener );
    }

    public void removeCatalogListener( IResolveChangeListener listener ) {
        catalogListeners.remove( listener );
    }

    public void removeListener( IResolveChangeListener listener ) {
        catalogListeners.remove( listener );
    }

    /**
     * Fire a resource changed event, these may be batched into one delta for
     * performance.
     */
    public void fire( IResolveChangeEvent event ) {
        for (Object listener : catalogListeners.getListeners()) {
            try {
                ((IResolveChangeListener)listener).changed( event );
            }
            catch (Throwable e) {
                PolymapWorkbench.handleError( CatalogPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
    }

    
    // add / remove / replace *****************************
    
    public void add( IService service )
            throws UnsupportedOperationException {
        delegate.add( service );
        
        IResolveDelta deltaAdded = new ResolveDelta( service, IResolveDelta.Kind.ADDED );
        IResolveDelta deltaChanged = new ResolveDelta( this, Collections.singletonList( deltaAdded ) );
        fire( new ResolveChangeEvent( CatalogImpl.this, IResolveChangeEvent.Type.POST_CHANGE,
                deltaChanged ) );

    }

    public void remove( IService service )
            throws UnsupportedOperationException {
        IResolveDelta deltaRemoved = new ResolveDelta( service, IResolveDelta.Kind.REMOVED );
        IResolveDelta deltaChanged = new ResolveDelta( this, Collections.singletonList( deltaRemoved ) );
        fire( new ResolveChangeEvent( CatalogImpl.this, IResolveChangeEvent.Type.PRE_DELETE,
                deltaChanged ) );

        delegate.remove( service );

        fire( new ResolveChangeEvent( CatalogImpl.this, IResolveChangeEvent.Type.POST_CHANGE,
                deltaRemoved ) );
    }

    public void replace( ID id, IService replacement )
            throws UnsupportedOperationException {
        delegate.replace( id, replacement );
    }


    public List<IResolve> find( URL resourceId, IProgressMonitor monitor ) {
        return delegate.find( resourceId, monitor );
    }

    @Override
    public <T> boolean canResolve( Class<T> adaptee ) {
        return delegate.resolve( adaptee, null ) != null;
    }


    @Override
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public IGeoResource createTemporaryResource( Object descriptor )
            throws IllegalArgumentException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public String[] getTemporaryDescriptorClasses() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public <T extends IResolve> T getById( Class<T> type, ID id, IProgressMonitor monitor ) {
        return delegate.getById( type, id, monitor );
    }


    @Override
    public List<IResolve> search( String pattern, Envelope bbox, IProgressMonitor monitor )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public List<IResolve> find( ID resourceId, IProgressMonitor monitor ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public ID getID() {
        return new ID( getIdentifier() );
    }


    public URL getIdentifier() {
        return delegate.getIdentifier();
    }


    public ICatalogInfo getInfo( IProgressMonitor monitor )
            throws IOException {
        return delegate.getInfo( monitor );
    }


    public Throwable getMessage() {
        return null;
    }


    public Status getStatus() {
        return Status.CONNECTED;
    }


    public String getTitle() {
        return delegate.getTitle();
    }


    public List<IResolve> members( IProgressMonitor monitor )
            throws IOException {
        return delegate.getMembers( monitor );
    }


    public IResolve parent( IProgressMonitor monitor ) {
        return null;
    }

}
