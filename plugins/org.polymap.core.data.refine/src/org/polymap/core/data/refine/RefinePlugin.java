package org.polymap.core.data.refine;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.polymap.core.CorePlugin;
import org.polymap.core.data.refine.impl.RefineServiceImpl;

/**
 * The activator class controls the plug-in life cycle
 */
public class RefinePlugin extends AbstractUIPlugin {

	private static Log log = LogFactory.getLog(RefinePlugin.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.core.data.refine"; //$NON-NLS-1$

	// The shared instance
	private static RefinePlugin plugin;

	private ServiceReference<RefineService> reference;

	private RefineServiceImpl service;

	/**
	 * The constructor
	 */
	public RefinePlugin() {
		log.info("Refine plugin initialized");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		File refineDir = new File(CorePlugin.getDataLocation(getBundle()), "refine");
		// ServiceReference configAdminReference =
		// context.getServiceReference(ConfigurationAdmin.class.getName());
		// ConfigurationAdmin configAdmin = (ConfigurationAdmin)
		// context.getService(configAdminReference);
		// Configuration config =
		// configAdmin.createFactoryConfiguration(PLUGIN_ID, null);
		// Hashtable<String, Object> properties = new Hashtable<String,
		// Object>();
		// properties.put(RefineServiceImpl.PARAM_BASEDIR, refineDir);
		// config.update(properties);
		service = RefineServiceImpl.INSTANCE(refineDir);
		reference = context.registerService(RefineService.class, service, null)
				.getReference();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		service.destroy();
		context.ungetService(reference);
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RefinePlugin getDefault() {
		return plugin;
	}

}
