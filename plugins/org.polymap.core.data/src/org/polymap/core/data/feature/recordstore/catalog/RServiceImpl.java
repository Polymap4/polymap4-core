package org.polymap.core.data.feature.recordstore.catalog;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IDeletingSchemaService;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.ui.ErrorManager;
import net.refractions.udig.ui.UDIGDisplaySafeLock;

import org.geotools.data.DataAccess;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.type.Name;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.data.Messages;
import org.polymap.core.data.feature.recordstore.RDataStore;

/**
 * Provides an ISerivce so that {@link RDataStore} can show up in service lists.
 * 
 * @since 3.1
 */
public class RServiceImpl 
        extends IService
        implements IDeletingSchemaService {

    private static Log log = LogFactory.getLog( RServiceImpl.class );

    private URL                       url;

    private Map<String, Serializable> params;

    protected Lock                    rLock = new UDIGDisplaySafeLock();

    private volatile List<RGeoResource> members = null;
    
    private Throwable                 msg;
    
    private volatile RDataStore       ds;
    
    private Lock                      dsInstantiationLock = new UDIGDisplaySafeLock();

    
    public RServiceImpl( URL url, Map<String, Serializable> params ) {
        assert url != null;
        this.url = url;
        this.params = params;
    }

    
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (adaptee == null) {
            throw new NullPointerException( "No adaptor specified" );
        }
        if (adaptee.isAssignableFrom( DataAccess.class )) {
            return adaptee.cast( getDS() );
        }
        return super.resolve( adaptee, monitor );
    }
    
    
    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee == null) {
            return false;
        }
        return adaptee.isAssignableFrom( JDBCDataStore.class )
                || adaptee.isAssignableFrom( Connection.class ) 
                || super.canResolve( adaptee );
    }

    
    public void dispose( IProgressMonitor monitor ) {
        if (members != null) {
            int steps = (int) (99 / (double) members.size());
            for (IResolve resolve : members) {
                try {
                    SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, steps);
                    resolve.dispose( subProgressMonitor );
                    subProgressMonitor.done();
                } 
                catch (Throwable e) {
                    ErrorManager.get().displayException(e,
                            "Error disposing members of service: " + getIdentifier(), CatalogPlugin.ID );
                }
            }
            members = null;
        }
    }


    public List<RGeoResource> resources( IProgressMonitor monitor ) 
    throws IOException {
        @SuppressWarnings("hiding")
        RDataStore ds = getDS();
        rLock.lock();
        try {
            if (members == null) {
                members = new LinkedList<RGeoResource>();
                List<Name> typenames = ds.getNames();
                if (typenames != null) {
                    for (Name name : typenames) {
                        members.add( new RGeoResource( this, name ) );
                    }
                }
            }
        }
        finally {
            rLock.unlock();
        }
        return members;
    }


    protected RServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        RDataStore dataStore = getDS();
        if (dataStore == null) {
            return null; // could not connect to provide info
        }
        rLock.lock();
        try {
            return new RServiceInfo( dataStore );
        } 
        finally {
            rLock.unlock();
        }
    }

    
    public Map<String, Serializable> getConnectionParams() {
        return params;
    }

    
    protected RDataStore getDS() throws IOException {
        boolean changed = false;
        dsInstantiationLock.lock();
        try {
            if (ds == null) {
                changed = true;
                RDataStoreFactory factory = RServiceExtension.factory();
                try {
                    ds = factory.createDataStore( params );
                } 
                catch (IOException e) {
                    msg = e;
                    throw e;
                }
                catch (Exception e) {
                    msg = e;
                    throw new IOException( e );
                }
            }
        } 
        finally {
            dsInstantiationLock.unlock();
        }
        // FIXME: cast gioes wrong because of different catalog impl
//        if (changed) {
//            IResolveDelta delta = new ResolveDelta(this, IResolveDelta.Kind.CHANGED);
//            ((CatalogImpl) CatalogPlugin.getDefault().getLocalCatalog())
//                    .fire(new ResolveChangeEvent(this, IResolveChangeEvent.Type.POST_CHANGE, delta));
//        }
        
//        if (ds != null) {
//            MySQLPlugin.addDataStore(ds);
//        }
        return ds;
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

    
    public void deleteSchema( IGeoResource geores, IProgressMonitor monitor )
    throws IOException {
        if (geores.service( monitor ) != this) {
            throw new IllegalStateException( "Geo resource is not RecordStore" );
        }
        getDS().deleteSchema( geores, monitor );
    }


    /*
     * 
     */
    private class RServiceInfo 
            extends IServiceInfo {

        RServiceInfo( RDataStore resource ) {
//            List<Name> tns = Collections.EMPTY_LIST;
//            try {
//                tns = resource.getNames();
//            } 
//            catch (IOException e) {
//                log.warn( "Unable to read typenames", e );
//            }
//            keywords = new String[tns.length + 1];

//            System.arraycopy( tns, 0, keywords, 1, tns.length );
            keywords = new String[] {"RDataStore"};

            try {
                schema = new URI( "recordstore://lucene/gml" );
            } 
            catch (URISyntaxException e) {
                log.warn( null, e );
            }
        }

        public String getDescription() {
            return getIdentifier().toString();
        }

        public URI getSource() {
            try {
                return getIdentifier().toURI();
            } 
            catch (URISyntaxException e) {
                throw (RuntimeException) new RuntimeException().initCause(e);
            }
        }

        @Override
        public String getShortTitle() {
            return Messages.get( "RDataStoreFactory_displayName" );
        }

        public String getTitle() {
            return StringUtils.substringAfterLast( getIdentifier().toExternalForm(), ":" );
        }

    }
    
}
