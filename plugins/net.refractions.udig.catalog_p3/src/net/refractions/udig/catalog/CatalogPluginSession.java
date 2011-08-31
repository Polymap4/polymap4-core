package net.refractions.udig.catalog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.internal.CatalogImpl;
import net.refractions.udig.catalog.internal.ResolveManager;
import net.refractions.udig.catalog.internal.ServiceFactoryImpl;
import net.refractions.udig.core.internal.ExtensionPointProcessor;
import net.refractions.udig.core.internal.ExtensionPointUtil;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.polymap.core.runtime.SessionSingleton;

/**
 * The session dependent part of the {@link CatalogPlugin} API and
 * implementation.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 *         <li>12.10.2009: created; taken from CatalogPlugin</li>
 * @since 3.0
 */
public class CatalogPluginSession {

    private static Log log = LogFactory.getLog( CatalogPluginSession.class );

    // static factory *************************************
    
    /**
     * Gets or creates the Platform instance for the application session of the
     * current thread.
     */
    public static CatalogPluginSession instance() {
        return SessionSingleton.instance( CatalogPluginSession.class );
    }
    

    // instance *******************************************
    
    private List<ISearch>           catalogs;

    private IPreferenceStore        preferenceStore;

    private volatile IResolveManager resolveManager;
  
    private IServiceFactory         serviceFactory;

    
    /**
     * The constructor.
     */
    protected CatalogPluginSession() {
        super();

        loadCatalogs();
        serviceFactory = new ServiceFactoryImpl();

        resolveManager = new ResolveManager();
        preferenceStore = new ScopedPreferenceStore( new InstanceScope(), 
                CatalogPlugin.getDefault().getBundle().getSymbolicName() );
        
        try {
            restoreFromPreferences();
        }
        catch (BackingStoreException e) {
            CatalogPlugin.log( null, e );
            handlerLoadingError( e );
        }
        catch (MalformedURLException e) {
            CatalogPlugin.log( null, e );
            handlerLoadingError( e );
        }
    }

    
//    private void addSaveLocalCatalogShutdownHook() {
//        ShutdownTaskList.instance().addPreShutdownTask(new PreShutdownTask(){
//
//            public int getProgressMonitorSteps() {
//                try {
//                    return getLocalCatalog().members( ProgressManager.instance().get() ).size();
//                }
//                catch (IOException e) {
//                    return 0;
//                }
//            }
//
//            public boolean handlePreShutdownException( Throwable t, boolean forced ) {
//                CatalogPlugin.log( "Error storing local catalog", t ); //$NON-NLS-1$
//                return true;
//            }
//
//
//            public boolean preShutdown( IProgressMonitor monitor, IWorkbench workbench,
//                    boolean forced )
//                    throws Exception {
//                log.warn( "_p3: XXX save catalog on shutdown commented out." );
////                ISearch[] toDispose = getCatalogs();
////                monitor.beginTask( Messages.CatalogPlugin_SavingCatalog,
////                        4 + (4 * toDispose.length) );
////                SubProgressMonitor subProgressMonitor = new SubProgressMonitor( monitor, 4 );
////                storeToPreferences( subProgressMonitor );
////                subProgressMonitor.done();
////                for (ISearch catalog : toDispose) {
////                    subProgressMonitor = new SubProgressMonitor( monitor, 4 );
////                    catalog.dispose( subProgressMonitor );
////                    subProgressMonitor.done();
////                }
//                return true;
//            }
//        });
//    }

    
    /**
     * Opens a dialog warning the user that an error occurred while loading the local catalog
     * 
     * @param e the exception that occurred
     */
    private void handlerLoadingError( Exception e ) {
        try {
            File backup = new File( getLocalCatalogFile().getParentFile(), "corruptedLocalCatalog" ); //$NON-NLS-1$
            copy( getLocalCatalogFile(), backup );
        }
        catch (IOException ioe) {
            CatalogPlugin.log( "Could not make a back up of the corrupted local catalog.", ioe ); //$NON-NLS-1$
        }
//        boolean addShutdownHook = MessageDialog.openQuestion(
//                Display.getDefault().getActiveShell(), Messages.CatalogPlugin_ErrorLoading,
//                Messages.CatalogPlugin__ErrorLoadingMessage );
//        if (addShutdownHook) {
//            addSaveLocalCatalogShutdownHook();
//        }
    }


    private void copy( File file, File backup ) throws IOException {
        FileChannel in = new FileInputStream( file ).getChannel();
        FileChannel out = new FileOutputStream( backup ).getChannel();
        final int BSIZE = 1024;
        ByteBuffer buffer = ByteBuffer.allocate( BSIZE );
        while (in.read( buffer ) != -1) {
            buffer.flip(); // Prepare for writing
            out.write( buffer );
            buffer.clear(); // Prepare for reading
        }
    }

    
    /** 
     * Load the getLocalCatalogFile() into the local catalog(). 
     */
    public void restoreFromPreferences()
            throws BackingStoreException, MalformedURLException {
        log.info( "instance: " + this );
        try {
            if (getLocalCatalog() instanceof CatalogImpl) {
                ((CatalogImpl)getLocalCatalog()).loadFromFile( getLocalCatalogFile(), getServiceFactory() );
            }
            loadCatalogs();
        }
        catch (Throwable t) {
            CatalogPlugin.log( null, new Exception( t ) );
        }
    }
    
    
    /**
     * Initialize catalogs. Checks extension point. Loads default implementation
     * into slot[0] if no extension is found.
     */
    private void loadCatalogs() {
        catalogs = new CopyOnWriteArrayList();

        // check extension point
        ExtensionPointUtil.process( CatalogPlugin.getDefault(),
                "net.refractions.udig.catalog.ICatalog", 
                
                new ExtensionPointProcessor() {
                    public void process( IExtension extension, IConfigurationElement element )
                    throws Exception {
                        catalogs.add( (ISearch)element.createExecutableExtension( "class" ) ); //$NON-NLS-1$                 
                    }
        } );

        // default impl
        if (catalogs.isEmpty()) {
            catalogs.add( new CatalogImpl() );
        }
    }

    
    public void storeToPreferences( IProgressMonitor monitor ) 
            throws BackingStoreException, IOException {
        ICatalog localCatalog = getLocalCatalog();
        if (localCatalog instanceof CatalogImpl) {
            ((CatalogImpl)localCatalog).saveToFile(getLocalCatalogFile(), getServiceFactory(), monitor);
        }
    }

    
    /**
     * File used to load/save the local catalog.
     *
     * @return
     * @throws IOException
     */
    private File getLocalCatalogFile() throws IOException {
//        // working directory for the application as a file
//        File userLocation = new File( FileLocator.toFileURL(
//                Platform.getInstanceLocation().getURL() ).getFile() );
//
//        // create the file if needed
//        if (!userLocation.exists()) {
//            userLocation.mkdirs();
//        }
        
//        IWorkspace workspace = ResourcesPlugin.getWorkspace();
//        IWorkspaceRoot root = workspace.getRoot();
//        IPath path = root.getLocation();
//        System.out.println( "Workspace root: " + path.toString() );

        // local catalog saved in working directory/.localCatalog
        //File catalogLocation = new File(userLocation, ".localCatalog"); //$NON-NLS-1$
        File catalogLocation = new File( "/tmp" /*path.toFile()*/, "localCatalog.props" ); //$NON-NLS-1$
        System.out.println( "*** *** Loading Catalog from: " + catalogLocation );
        return catalogLocation;
    }


    /**
     * Add a catalog listener for changed to this catalog.
     * 
     * @param listener
     */
    public void addListener( IResolveChangeListener listener ) {
        getLocalCatalog().addCatalogListener( listener );
    }


    /**
     * Remove a catalog listener that was interested in this catalog.
     * 
     * @param listener
     */
    public void removeListener( IResolveChangeListener listener ) {
        getLocalCatalog().removeCatalogListener( listener );
    }


    /**
     * @return Returns All catalogs of the system. Local default impl is is
     *         slot[0] if no other catalogs were found.
     */
    public ISearch[] getCatalogs() {
        if (catalogs == null) {
            loadCatalogs();
        }
        return catalogs.toArray( new ISearch[catalogs.size()] );
    }

    
    /**
     * @return the local catalog. Equivalent to getCatalogs()[0]
     */
    public ICatalog getLocalCatalog() {
        //log.info( "instance: " + this );
        return (ICatalog)getCatalogs()[0];
    }

    
    /**
     * @return Returns the serviceFactory.
     */
    public IServiceFactory getServiceFactory() {
        return serviceFactory;
    }


    /**
     * Returns the preference store for this UI plug-in.
     * This preference store is used to hold persistent settings for this plug-in in
     * the context of a workbench. Some of these settings will be user controlled, 
     * whereas others may be internal setting that are never exposed to the user.
     * <p>
     * If an error occurs reading the preference store, an empty preference store is
     * quietly created, initialized with defaults, and returned.
     * </p>
     * <p>
     * <strong>NOTE:</strong> As of Eclipse 3.1 this method is
     * no longer referring to the core runtime compatibility layer and so
     * plug-ins relying on Plugin#initializeDefaultPreferences
     * will have to access the compatibility layer themselves.
     * </p>
     *
     * @return the preference store 
     */
    public IPreferenceStore getPreferenceStore() {
        return preferenceStore;
    }

    
    public IResolveManager getResolveManager() {
        return resolveManager;
    }

}
