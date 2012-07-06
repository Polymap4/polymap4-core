package org.polymap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.polymap.core.runtime.DefaultSessionContextProvider;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.Stringer;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;

import org.polymap.service.ui.GeneralPreferencePage;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ServicesPlugin 
        extends AbstractUIPlugin {
        //implements IModelStoreListener {

    private static Log log = LogFactory.getLog( ServicesPlugin.class );

	// The plug-in ID
	public static final String      PLUGIN_ID = "org.polymap.service";
	
	/** The general base pathSpec for all services. */
	public static final String      SERVICES_PATHSPEC = "/services";

	public static final String      PREF_PROXY_URL = "_proxyUrl_";
    
    public static final String      SERVICE_TYPE_WMS = "org.polymap.service.http.WmsService";
    public static final String      SERVICE_TYPE_WFS = "org.polymap.service.http.WfsService";

    // The shared instance
    private static ServicesPlugin   plugin;
    

    public static ServicesPlugin getDefault() {
        return plugin;
    }


    /**
     * Replace invalid chars to form a valid servlet pathSpec.
     * <p/>
     * Also ensures that the given pathSpec The pathSpec must begin with slash ('/')
     * and must not end with slash ('/'), with the exception that an alias of the
     * form &quot;/&quot; is used to denote the root alias. See the specification
     * text for details on how HTTP requests are mapped to servlet and resource
     * registrations.
     * 
     * @param s The name or pathSpec to check.
     * @return The modified pathSpec.
     */
    public static String validPathSpec( String s ) {
        //s = s.startsWith( "/" ) ? s.substring( 1 ) : s;
        Stringer result = Stringer.on( s ).replaceUmlauts().toURIPath( "_" );
        if (!result.startsWith( "/" )) {
            result.insert( 0, '/' );
        }
        if (result.endsWith( "/" )) {
            result.deleteCharAt( result.length() - 1 );
        }
        return result.toString();
    }

    /**
     * 
     * @param pathSpec The simple name or pathSpec to use in the URL.
     * @return The complete URL including protocol, host, port, services base URL and
     *         tteh validated pathSpec.
     */
    public static String createServiceUrl( String pathSpec ) {
        return getDefault().getServicesBaseUrl() + validPathSpec( pathSpec );
    }
    
    /**
     * 
     * @param pathSpec The simple name or pathSpec to use in the URL.
     * @return The complete URL including protocol, host, port, services base URL and
     *         tteh validated pathSpec.
     */
    public static String createServicePath( String pathSpec ) {
        return SERVICES_PATHSPEC + validPathSpec( pathSpec );
    }
    
    
    // instance *******************************************
    
    private Map<String,ServiceContext> serviceContexts = new HashMap();
    
    /** The base URL on the local machine (without proxy). */
    private String                  localBaseUrl;
    
    /** The base URL explicitly set by the user via {@link GeneralPreferencePage}. */
    private String                  proxyBaseUrl;
    
    private DefaultSessionContextProvider contextProvider;

    private HttpService             httpService;

    private ServiceTracker          httpServiceTracker;
    
    
    public ServicesPlugin() {
    }

    
    public String getBaseUrl() {
        return proxyBaseUrl != null && proxyBaseUrl.length() > 0
            ? proxyBaseUrl : localBaseUrl;
    }

    public String getServicesBaseUrl() {
        return getBaseUrl() + SERVICES_PATHSPEC;
    }
    
    
    public void start( final BundleContext context )
    throws Exception {
        super.start( context );
        plugin = this;
        
        // sessionContext
        contextProvider = new DefaultSessionContextProvider();
        SessionContext.addProvider( contextProvider );
        
        // legacy: delete services without a map
        contextProvider.mapContext( "legacyDeleteServices", true );
        Polymap.instance().addPrincipal( new AdminPrincipal() );
        ServiceRepository repo = ServiceRepository.instance();
        try {
            repo.legacyRemoveServices();
        }
        finally {
            repo.commitChanges();
            contextProvider.unmapContext();
        }

        // register resource
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {
            public Object addingService( ServiceReference reference ) {
                httpService = (HttpService)super.addingService( reference );                
                if (httpService != null) {
                    String protocol = "http";
                    String port = context.getProperty( "org.osgi.service.http.port" );
                    String hostname = "localhost";
                    try {
                        InetAddress.getLocalHost().getHostAddress();
                    }
                    catch (UnknownHostException e) {
                        // ignore; use "localhost" then
                    }

                    // get baseUrl
                    localBaseUrl = protocol + "://" + hostname + ":" + port;
                    log.info( "HTTP service found on: " + localBaseUrl );

                    ScopedPreferenceStore prefStore = new ScopedPreferenceStore( new InstanceScope(), getBundle().getSymbolicName() );
                    proxyBaseUrl = prefStore.getString( ServicesPlugin.PREF_PROXY_URL );
                    log.info( "Proxy URL set to: " + proxyBaseUrl );

                    // delayed starting services in separate thread
                    new Job( "ServiceStarter" ) {
                        protected IStatus run( IProgressMonitor monitor ) {
                            log.info( "Starting services..." );
                            initServices();
                            return Status.OK_STATUS;
                        }
                    }.schedule( 5000 );
                }
                return httpService;
            }
        };
        httpServiceTracker.open();
    }


    public void stop( BundleContext context )
    throws Exception {
        httpServiceTracker.close();
        httpServiceTracker = null;
        
        plugin = null;
        super.stop( context );
        
        SessionContext.removeProvider( contextProvider );
        contextProvider = null;
    }

    
    /**
     * Start all global services and register model change listener
     * and preference listener.
     */
    protected void initServices() {
        try {
            contextProvider.mapContext( "services", true );
            Polymap.instance().addPrincipal( new AdminPrincipal() );
            
            // create/start ServiceContexts
            ServiceRepository repo = ServiceRepository.instance();
            List<IProvidedService> services = new ArrayList( repo.allServices() );
            
            // ServiceContext maps its own context and DefaultSessionContextProvider
            // uses a static ThreadLocal to store contexts -> unmap now
            contextProvider.unmapContext();

            for (IProvidedService service : services) {
                initServiceContext( service );
            }
        }
        catch (Exception e) {
            log.warn( "Error while starting services.", e );
        }
        finally {
            // a ServiceContext may have set another context, so we have to check first
            SessionContext context = contextProvider.currentContext();
            if (context != null && context.getSessionKey().equals( "services" )) {
                contextProvider.unmapContext();
            }
        }
    }

    
    public ServiceContext initServiceContext( IProvidedService service ) {
        ServiceContext context = serviceContexts.get( service.id() );
        if (context == null) {
            context = new ServiceContext( service.id(), httpService );
            serviceContexts.put( service.id(), context );
        }
        return context;
    }
    
    
    public boolean isValid() {
        return true;
    }

    
    /*
     * 
     */
    static class AdminPrincipal
            extends UserPrincipal {

        public AdminPrincipal() {
            super( SecurityUtils.ADMIN_USER );
        }

        public String getPassword() {
            throw new RuntimeException( "not yet implemented." );
        }
        
    }
    
}
