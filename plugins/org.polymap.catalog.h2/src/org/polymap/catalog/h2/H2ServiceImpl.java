package org.polymap.catalog.h2;

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

import org.geotools.data.h2.H2DialectBasic;
import org.geotools.jdbc.JDBCDataStore;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.catalog.h2.data.H2DataStoreFactory;

/**
 * Provides an ISerivce so that H2 can show up in service lists.
 * 
 * @since 3.1
 */
public class H2ServiceImpl 
        extends IService
        implements IDeletingSchemaService {

    private static Log log = LogFactory.getLog( H2ServiceImpl.class );

    private URL                       url;

    private Map<String, Serializable> params;

    protected Lock                    rLock = new UDIGDisplaySafeLock();

    private volatile List<H2GeoResource> members = null;
    
    private Throwable                 msg;
    
    private volatile JDBCDataStore    ds;
    
    private Lock                      dsInstantiationLock = new UDIGDisplaySafeLock();

    
    public H2ServiceImpl( URL url, Map<String, Serializable> params ) {
        assert url != null;
        this.url = url;
        this.params = params;
    }

    
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (adaptee == null) {
            throw new NullPointerException("No adaptor specified");
        }
        if (adaptee.isAssignableFrom( JDBCDataStore.class )) {
            return adaptee.cast( getDS() );
        }
        return super.resolve(adaptee, monitor);
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


    public List<H2GeoResource> resources( IProgressMonitor monitor ) 
    throws IOException {
        @SuppressWarnings("hiding")
        JDBCDataStore ds = getDS();
        rLock.lock();
        try {
            if (members == null) {
                members = new LinkedList<H2GeoResource>();
                String[] typenames = ds.getTypeNames();
                if (typenames != null) {
                    for (int i = 0; i < typenames.length; i++) {
                        members.add( new H2GeoResource( this, typenames[i] ) );
                    }
                }
            }
        }
        finally {
            rLock.unlock();
        }
        return members;
    }


    protected H2ServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        JDBCDataStore dataStore = getDS(); // load DataStore
        if (dataStore == null) {
            return null; // could not connect to provide info
        }
        rLock.lock();
        try {
            return new H2ServiceInfo(dataStore);
        } 
        finally {
            rLock.unlock();
        }
    }

    
    public Map<String, Serializable> getConnectionParams() {
        return params;
    }

    
    JDBCDataStore getDS() throws IOException {
        boolean changed = false;
        dsInstantiationLock.lock();
        try {
            if (ds == null) {
                changed = true;
                H2DataStoreFactory dsf = H2ServiceExtension.getFactory();
                if (dsf.canProcess( params )) {
                    try {
                        ds = dsf.createDataStore( params );
                    } 
                    catch (IOException e) {
                        msg = e;
                        throw e;
                    }
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
            throw new IllegalStateException( "Geores is not H2" );
        }
        new JDBCHelper( getDS(), new H2DialectBasic( getDS() ) )
                .deleteSchema( geores, monitor );
    }


    /*
     * 
     */
    private class H2ServiceInfo 
            extends IServiceInfo {

        H2ServiceInfo( JDBCDataStore resource ) {
            String[] tns = new String[0];
            try {
                tns = resource.getTypeNames();
            } 
            catch (IOException e) {
                log.warn( "Unable to read typenames", e ); //$NON-NLS-1$
            }
            keywords = new String[tns.length + 1];

            System.arraycopy( tns, 0, keywords, 1, tns.length );
            keywords[0] = "h2";

            try {
                schema = new URI("jdbc://h2/gml"); //$NON-NLS-1$
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

        public String getTitle() {
            return ("H2 " + StringUtils.substringAfterLast( getIdentifier().toExternalForm(), ":" ) );
            //return "MySQL " + getIdentifier(); //$NON-NLS-1$
            //return "MySQL " + getIdentifier().getHost() + URLUtils.urlToFile(getIdentifier()).getAbsolutePath(); //$NON-NLS-1$
        }

    }
}
