package org.polymap.openlayers.rap.widget;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * The activator class controls the plug-in life cycle
 */
public class OpenlayersPlugin 
        extends Plugin {

	// The plug-in ID
	public static final String         PLUGIN_ID = "org.polymap.openlayers.rap.widget";

	private static OpenlayersPlugin    plugin;

	public boolean                     started = false;

	
	public OpenlayersPlugin() {
	}


    public void start( final BundleContext context )
    throws Exception {
        super.start( context );

        // start HttpServiceRegistry
        if (HttpService.class != null) {
            startService( context );
        }
        else {
            context.addBundleListener( new BundleListener() {
                public void bundleChanged( BundleEvent ev ) {
                    if (ev.getType() == BundleEvent.STOPPED
                            && !started && (HttpService.class != null)) {
                        startService( context );
                    }
                }
            });
        }

		plugin = this;
	}


    public void stop( BundleContext context )
    throws Exception {
        plugin = null;
        super.stop( context );
    }

    
    protected void startService( BundleContext context ) {
        HttpService httpService;
        ServiceReference[] httpReferences = null;
        try {
            httpReferences = context.getServiceReferences( HttpService.class.getName(), null );
        }
        catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

        if (httpReferences != null) {
            String port = context.getProperty( "org.osgi.service.http.port" );
            String hostname = context.getProperty( "org.osgi.service.http.hostname" );

            httpService = (HttpService)context.getService( httpReferences[0] );

            try {
                httpService.registerResources( "/openlayers", "/openlayers", null );
                started = true;
            }
            catch (NamespaceException e) {
                e.printStackTrace();
            }
        }
    }

    
	public static OpenlayersPlugin getDefault() {
		return plugin;
	}
	
}
