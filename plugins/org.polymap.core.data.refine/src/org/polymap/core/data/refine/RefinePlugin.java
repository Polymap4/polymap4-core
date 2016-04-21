package org.polymap.core.data.refine;

import java.io.File;
import java.nio.file.Files;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.data.refine.impl.RefineServiceImpl;

/**
 * The activator class controls the plug-in life cycle
 */
public class RefinePlugin
        extends AbstractUIPlugin {

    private static Log                      log = LogFactory.getLog( RefinePlugin.class );

    // The plug-in ID
    public static final String              ID  = "org.polymap.core.data.refine";         //$NON-NLS-1$

    // The shared instance
    private static RefinePlugin             plugin;

    private ServiceReference<RefineService> reference;

    private RefineServiceImpl               service;

    private File                            baseTempDir;


    /**
     * The constructor
     */
    public RefinePlugin() {
        log.info( "Refine plugin initialized" );
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
     * BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;

        baseTempDir = Files.createTempDirectory( ID ).toFile();
        baseTempDir.mkdirs();
        baseTempDir.deleteOnExit();
        FileUtils.cleanDirectory( baseTempDir );
        log.info( "temp dir: " + baseTempDir );

        service = RefineServiceImpl
                .INSTANCE( Files.createTempDirectory( baseTempDir.toPath(), null ) );
        reference = context.registerService( RefineService.class, service, null )
                .getReference();
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext)
     */
    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        service.destroy();
        context.ungetService( reference );
        if (baseTempDir != null) {
            FileUtils.cleanDirectory( baseTempDir );
        }
        super.stop( context );
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
