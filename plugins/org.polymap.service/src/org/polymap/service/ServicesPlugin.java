package org.polymap.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.core.runtime.preferences.InstanceScope;

import org.polymap.core.CorePlugin;
import org.polymap.core.model.event.ModelStoreEvent;
import org.polymap.core.model.event.IModelStoreListener;
import org.polymap.core.model.event.ModelStoreEvent.EventType;
import org.polymap.core.runtime.DefaultSessionContext;
import org.polymap.core.runtime.DefaultSessionContextProvider;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.workbench.PolymapWorkbench;

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

    private static Log log = LogFactory.getLog( ServicesPlugin.class );

	// The plug-in ID
	public static final String      PLUGIN_ID = "org.polymap.service";
	
	/** The general base pathSpec for all services. */
	public static final String      SERVICES_PATHSPEC = "/services";

	public static final String      PREF_PROXY_URL = "_proxyUrl_";
    

    // The shared instance
    private static ServicesPlugin   plugin;
    
    private static boolean          started = false;


    public static ServicesPlugin getDefault() {
        return plugin;
    }

    /**
     * Static helper function that builds names that can be used as part
     * of an URL out of label Strings.
     * <p>
     * Currently german umlauts are replaced. Afterwards all chars other than
     * [a-zA-Z\\-_] are removed.
     */
    public static String simpleName( String s ) {
        String result = s;
        result = result.replaceAll("[Üü]", "ue").
                replaceAll( "[Ää]", "ae").
                replaceAll( "[Öö]", "oe").
                replaceAll( "[ß]", "ss");
        result = result.replaceAll( "[^a-zA-Z0-9\\-_]", "" );
        return result;
    }
    


    // instance *******************************************
    
    private ServiceRepository       repo;
    
    private IModelStoreListener modelChangeListener;
    
    /** The base URL on the local machine (without proxy). */
    private String                  localBaseUrl;
    
    /** The base URL explicitly set by the user via {@link GeneralPreferencePage}. */
    private String                  proxyBaseUrl;
    
    /** The session context shared by all services. */
    private DefaultSessionContext   sessionContext;
    
    public DefaultSessionContextProvider contextProvider;

    
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
        sessionContext = new DefaultSessionContext( "services" );
        contextProvider = new DefaultSessionContextProvider() {
            protected DefaultSessionContext newContext( String sessionKey ) {
                return sessionContext;
            }
        };
        SessionContext.addProvider( contextProvider );
        
        // start HttpServiceRegistry
        context.addBundleListener( new BundleListener() {
            public void bundleChanged( BundleEvent ev ) {
                
                if (!started && (HttpService.class != null)) {
                    HttpService httpService = null;
                    ServiceReference[] httpReferences = null;
                    try {
                        httpReferences = context.getServiceReferences( HttpService.class.getName(), null );
                    }
                    catch (InvalidSyntaxException e) {
                        // FIXME Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    if (httpReferences != null) {
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

                        httpService = (HttpService) context.getService( httpReferences[0] );
                        startServices( httpService );                            
                        started = true;
                    } 
                    else {
                        // No http service yet available - waiting for next BundleEvent
                    }
                }
                // stop
                else if (ev.getType() == BundleEvent.STOPPED && ev.getBundle().equals( getBundle() )) {

                }
            }
        });
    }


    public void stop( BundleContext context )
            throws Exception {
        plugin = null;
        super.stop( context );
        
        SessionContext.removeProvider( contextProvider );
        contextProvider = null;
    }

    
    public void mapContext( String sessionKey ) {
        contextProvider.mapContext( sessionContext.getSessionKey(), false );    
    }

    
    public void unmapContext() {
        contextProvider.unmapContext();
    }
    
    
    /**
     * Start all global services and register model change listener
     * and preference listener.
     */
    protected void startServices( HttpService httpService ) {
        try {
            contextProvider.mapContext( sessionContext.getSessionKey(), true );
            Polymap.instance().addPrincipal( new AdminPrincipal() );
            
            //
            repo = ServiceRepository.instance();
            for (IProvidedService service : repo.allServices()) {
                if (service.isEnabled()) {
                    try {
                        service.start();
                    }
                    catch (Exception e) {
                        CorePlugin.logError( "Error while starting services: " + service.getPathSpec(), log, e );
                    }
                }
            }
            
            // listen to global change events of the maps
            modelChangeListener = new IModelStoreListener() {
                public void modelChanged( ModelStoreEvent ev ) {
                    // XXX avoid restart on *every* global entity event
                    log.debug( "Global entity event: source= " + ev.getSource() );
                    if (ev.getEventType() == EventType.COMMIT
                            && ServiceRepository.class.isAssignableFrom( ev.getSource().getClass() )) {
                        restartServices();
                    }
                }
                public boolean isValid() {
                    return true;
                }
            };
            repo.addModelStoreListener( modelChangeListener );

            // listen to preference changes
            final ScopedPreferenceStore prefStore = new ScopedPreferenceStore( new InstanceScope(), getBundle().getSymbolicName() );
            prefStore.addPropertyChangeListener( new IPropertyChangeListener() {
                
                public void propertyChange( PropertyChangeEvent ev ) {
                    log.debug( "Preferences changed: " + ev.getProperty() );
                    
                    if (ev.getProperty().equals( PREF_PROXY_URL )) {
                        proxyBaseUrl = prefStore.getString( ServicesPlugin.PREF_PROXY_URL );
                        log.info( "Proxy URL set to: " + proxyBaseUrl );
                        restartServices();
                    }
                }
            });

        }
        catch (Exception e) {
            CorePlugin.logError( "Error while starting services.", log, e );
        }
        finally {
            contextProvider.unmapContext();
        }
    }


    protected void restartServices() {
        // stop running services from current repository
        for (IProvidedService service : repo.allServices()) {
            if (service.isEnabled()) {
                try {
                    contextProvider.mapContext( sessionContext.getSessionKey(), false );
                    service.stop();
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( ServicesPlugin.PLUGIN_ID, this, "Fehler beim Anhalten des Dienstes.", e );
                }
                finally {
                    contextProvider.unmapContext();
                }
            }
        }
        
        // destroy current session
        contextProvider.destroyContext( sessionContext.getSessionKey() );
        sessionContext = new DefaultSessionContext( "services" );

        // get new repository and (re)start new services
        try {
            contextProvider.mapContext( sessionContext.getSessionKey(), true );
            Polymap.instance().addPrincipal( new AdminPrincipal() );

            repo = ServiceRepository.instance();
            for (IProvidedService service : repo.allServices()) {
                if (service.isEnabled()) {
                    try {
                        service.start();
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( ServicesPlugin.PLUGIN_ID, this, "Fehler beim (Re)Start des Dienstes.", e );
                    }
                }
            }
        }
        finally {
            contextProvider.unmapContext();
        }
    }
    
    
    /*
     * 
     */
    class AdminPrincipal
            extends UserPrincipal {

        public AdminPrincipal() {
            super( SecurityUtils.ADMIN_USER );
        }

        public String getPassword() {
            throw new RuntimeException( "not yet implemented." );
        }
        
    }
    
}
