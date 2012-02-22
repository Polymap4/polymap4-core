package org.polymap.openlayers.rap.widget;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class OpenlayersPlugin 
        extends Plugin {

	// The plug-in ID
	public static final String         PLUGIN_ID = "org.polymap.openlayers.rap.widget";

	private static OpenlayersPlugin    plugin;

    private ServiceTracker             httpServiceTracker;

	
	public OpenlayersPlugin() {
	}


    public void start( final BundleContext context )
    throws Exception {
        super.start( context );

        // register HTTP resource
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {
            public Object addingService( ServiceReference reference ) {
                HttpService httpService = (HttpService)super.addingService( reference );                
                if (httpService != null) {
                    try {
                        httpService.registerResources( "/openlayers", "/openlayers", null );
                    }
                    catch (NamespaceException e) {
                        throw new RuntimeException( e );
                    }
                }
                return httpService;
            }
        };
        httpServiceTracker.open();

		plugin = this;
	}


    public void stop( BundleContext context )
    throws Exception {
        httpServiceTracker.close();
        httpServiceTracker = null;
        
        plugin = null;
        super.stop( context );
    }

    
	public static OpenlayersPlugin getDefault() {
		return plugin;
	}
	
}
