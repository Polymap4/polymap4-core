/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.polymap.catalog.dxf;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeEvent;
import net.refractions.udig.catalog.IResolveDelta;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.URLUtils;
import net.refractions.udig.catalog.internal.CatalogImpl;
import net.refractions.udig.catalog.internal.ResolveChangeEvent;
import net.refractions.udig.catalog.internal.ResolveDelta;
import net.refractions.udig.ui.ErrorManager;
import net.refractions.udig.ui.UDIGDisplaySafeLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.geotools.data.dxf.DXFDataStore;
import org.geotools.data.dxf.DXFDataStoreFactory;

/**
 * Connect to a shapefile
 * 
 * @author David Zwiers, Refractions Research
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class DxfServiceImpl 
        extends IService {

    private URL                       url;

    private ID                        id;

    private Map<String, Serializable> params;

    private Throwable                 msg;

    /**
     * Volatile cache of dataStore if created.
     */
    volatile DXFDataStore             ds;

    protected final Lock              rLock = new UDIGDisplaySafeLock();

    private final static Lock         dsInstantiationLock = new UDIGDisplaySafeLock();

    private volatile List<DxfGeoResourceImpl> members = null;


	/**
	 * Construct <code>DxfServiceImpl</code>.
	 * 
	 * @param arg1
	 * @param arg2
	 */
    public DxfServiceImpl( URL url, Map<String, Serializable> params ) {
        this.url = url;
        try {
            id = new ID( url );
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        this.params = params;
    }


	/*
	 * Required adaptions: <ul> <li>IServiceInfo.class <li>List.class
	 * <IGeoResource> </ul>
	 * 
	 * @see net.refractions.udig.catalog.IService#resolve(java.lang.Class,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public <T> T resolve(Class<T> adaptee, IProgressMonitor monitor)
			throws IOException {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (adaptee == null) {
            throw new NullPointerException( "No adaptor specified" ); //$NON-NLS-1$
        }
        if (adaptee.isAssignableFrom( DXFDataStore.class )) {
            return adaptee.cast( getDS( monitor ) );
        }
        if (adaptee.isAssignableFrom( File.class )) {
            return adaptee.cast( toFile() );
        }
        return super.resolve( adaptee, monitor );
	}

	/*
	 * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
	 */
	public <T> boolean canResolve(Class<T> adaptee) {
        if (adaptee == null) {
            return false;
        }
        else {
            return adaptee.isAssignableFrom( DXFDataStore.class ) || super.canResolve( adaptee );
        }
	}


    public void dispose( IProgressMonitor monitor ) {
        if (members == null) {
            return;
        }
        int steps = (int)((double)99 / (double)members.size());
        for (IResolve resolve : members) {
            try {
                SubProgressMonitor subProgressMonitor = new SubProgressMonitor( monitor, steps );
                resolve.dispose( subProgressMonitor );
                subProgressMonitor.done();
            }
            catch (Throwable e) {
                ErrorManager.get().displayException( e,
                        "Error disposing members of service: " + getIdentifier(), CatalogPlugin.ID ); //$NON-NLS-1$
            }
        }
    }

    
	/*
	 * @see net.refractions.udig.catalog.IResolve#members(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public List<DxfGeoResourceImpl> resources(IProgressMonitor monitor)
			throws IOException {

		if (members == null) {
            getDS(monitor); // slap it to load datastore
			rLock.lock();
            try{
				if (members == null) {
					members = new LinkedList<DxfGeoResourceImpl>();
					String[] typenames = ds.getTypeNames();
					if (typenames != null)
						for (int i = 0; i < typenames.length; i++) {
							members.add(new DxfGeoResourceImpl(this,
									typenames[i]));
						}
				}
			}finally{
			    rLock.unlock();
            }
		}
		return members;
	}

	/*
	 * @see net.refractions.udig.catalog.IService#getInfo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IServiceInfo createInfo(IProgressMonitor monitor) throws IOException {
		getDS(monitor); // load ds
		if (info == null && ds != null) {
			rLock.lock();
            try{
				if (info == null) {
					info = new DxfServiceInfo(this);
				}
			}finally{
			    rLock.unlock();
            }
		}
		return info;
	}

	/*
	 * @see net.refractions.udig.catalog.IService#getConnectionParams()
	 */
	public Map<String, Serializable> getConnectionParams() {
		return params;
	}

	DXFDataStore getDS(IProgressMonitor monitor) 
	throws IOException {
		if (ds == null) {
			dsInstantiationLock.lock();
            try {
                if (ds == null) {
                    DXFDataStoreFactory dsf = new DXFDataStoreFactory();
                    if (dsf.canProcess( params )) {

                        ds = (DXFDataStore)dsf.createDataStore( params );
                        // hit it lightly to make sure it exists.
                        //ds.getFeatureSource();

                    }
                }
            }
            finally {
                dsInstantiationLock.unlock();
            }
            IResolveDelta delta = new ResolveDelta( this, IResolveDelta.Kind.CHANGED );
            ResolveChangeEvent event = new ResolveChangeEvent( this,
                    IResolveChangeEvent.Type.POST_CHANGE, delta );
            fire( event );
        }
        return ds;
	}


    private void fire( ResolveChangeEvent event ) {
        ICatalog catalog = parent( new NullProgressMonitor() );
        if (catalog instanceof CatalogImpl) {
            ((CatalogImpl)catalog).fire( event );
        }
    }


    public Status getStatus() {
        return msg != null ? Status.BROKEN : ds == null ? Status.NOTCONNECTED : Status.CONNECTED;
    }


	public Throwable getMessage() {
		return msg;
	}

	public URL getIdentifier() {
		return url;
	}

	@Override
	public ID getID() {
	    return id;
	}


    /**
     * The File as indicated in the connection parameters, may be null if we are
     * representing a web resource.
     * 
     * @return file as indicated in the connection parameters, may be null if we are
     *         reprsenting a web resource
     */
    public File toFile() {
        Map<String, Serializable> parametersMap = getConnectionParams();
        URL _url = (URL)parametersMap.get( DXFDataStoreFactory.PARAM_URL.key );
        return URLUtils.urlToFile( _url );
    }
    
}
