/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH. All rights reserved.
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
package org.polymap.core.catalog.model;

import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.Principal;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.AclPermission;

/**
 * A facade of {@link ServiceComposite} providing the {@link ICatalog} and the
 * {@link ACL} API to the user. This facade is necessary because IService is an
 * abstract class (instead of an interface), which an entity composite cannot
 * extend directly.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class ServiceImpl
        extends IService
        implements ACL, IAdaptable {

    private ServiceComposite        entity;
    
    private IService                delegate;


    public ServiceImpl( ServiceComposite entity ) {
        this.entity = entity;
        this.delegate = entity.getService();
    }
    

    public Object getAdapter( Class adapter ) {
        if (ACL.class.isAssignableFrom( adapter )) {
            return this;
        }
        return null;
    }

    
    // ACL ************************************************
    
    public boolean addPermission( String principal, AclPermission... permissions ) {
        return entity.addPermission( principal, permissions );
    }

    public boolean checkPermission( Principal principal, AclPermission permission ) {
        return entity.checkPermission( principal, permission );
    }

    public boolean removePermission( String principal, AclPermission... permissions ) {
        return entity.removePermission( principal, permissions );
    }

    public Iterable<Entry> entries() {
        return entity.entries();
    }


    // IService *******************************************
    
    @Override
    protected IServiceInfo createInfo( IProgressMonitor monitor )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public <T> boolean canResolve( Class<T> adaptee ) {
        return delegate.canResolve( adaptee );
    }


    public void dispose( IProgressMonitor monitor ) {
        delegate.dispose( monitor );
    }


    public Map<String, Serializable> getConnectionParams() {
        return delegate.getConnectionParams();
    }


    public ID getID() {
        return delegate.getID();
    }


    public URL getIdentifier() {
        return delegate.getIdentifier();
    }


    public Throwable getMessage() {
        return delegate.getMessage();
    }


    public Map<String, Serializable> getPersistentProperties() {
        return delegate.getPersistentProperties();
    }


    public Status getStatus() {
        return delegate.getStatus();
    }


    public String getTitle() {
        return delegate.getTitle();
    }


    public List<IResolve> members( IProgressMonitor monitor )
            throws IOException {
        return delegate.members( monitor );
    }


    public ICatalog parent( IProgressMonitor monitor ) {
        return delegate.parent( monitor );
    }


    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor )
            throws IOException {
        return delegate.resolve( adaptee, monitor );
    }


    public List<? extends IGeoResource> resources( IProgressMonitor monitor )
            throws IOException {
        return delegate.resources( monitor );
    }


    public String toString() {
        return delegate.toString();
    }

}
