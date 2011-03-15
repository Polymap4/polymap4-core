package org.polymap.openlayers.rap.lib_provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class Activator extends Plugin {
	private static final Log log = LogFactory.getLog(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.openlayers.rap.lib_provider";

	// The shared instance
	private static Activator plugin;

	public boolean started=false;
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		super.start(context);

		
		  // start HttpServiceRegistry
        context.addBundleListener( new BundleListener() {
            public void bundleChanged( BundleEvent ev ) {
               
            	if (!started&& (HttpService.class!=null)) {
            	           	
            		//log.info("bundle event" + ev.getType() + " " + ev.getBundle() );
               // if (ev.getType() == BundleEvent.STARTED && ev.getBundle().equals( getBundle() )) {
                   

            		HttpService httpService;
            		// BundleContext context=
            		// CorePlugin.getDefault().getBundle().getBundleContext();
            		ServiceReference[] httpReferences = null;
            		try {
            			httpReferences = context.getServiceReferences(HttpService.class
            					.getName(), null);
            		} catch (InvalidSyntaxException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            		if (httpReferences != null) {

            			String port = context.getProperty("org.osgi.service.http.port");
            			String hostname = context
            					.getProperty("org.osgi.service.http.hostname");

            			log.info("found http service on hostname:" + hostname + "/ port:"
            					+ port);

            			httpService = (HttpService) context.getService(httpReferences[0]);

            			try {
            				httpService.registerResources("/openlayers", "/openlayers", null);
            				started=true;
            			
            			}  catch (NamespaceException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}

            		} else {
            			log.debug("No http service yet available - waiting for next BundleEvent");
            		}
            		
                }
                // stop
                else if (ev.getType() == BundleEvent.STOPPED && ev.getBundle().equals( getBundle() )) {

                }
            }
        });

		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
