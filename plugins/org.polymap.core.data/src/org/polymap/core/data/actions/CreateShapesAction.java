package org.polymap.core.data.actions;

import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.internal.shp.ShpServiceExtension;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class CreateShapesAction 
        implements IWorkbenchWindowActionDelegate {
	
    private static Log log = LogFactory.getLog( CreateShapesAction.class );

    private IWorkbenchWindow        window;
	

    /**
	 * The constructor.
	 */
	public CreateShapesAction() {
	}

    /**
     * We can use this method to dispose of any system
     * resources we previously allocated.
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose() {
    }

    /**
     * We will cache window object in order to
     * be able to provide parent shell for the message dialog.
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow _window) {
        this.window = _window;
    }
    
    /**
     * The action has been activated. The argument of the method represents the
     * 'real' action sitting in the workbench UI.
     * 
     * @see IWorkbenchWindowActionDelegate#run
     */
	public void run( IAction action ) {
        try {
            // geoserver lokal
            //createService( new File( "/home/falko/jboss-4.0.3/polymap2.freiberg/shape/altleisnig", "buff_1000_1.shp" ).toURI().toURL() );
            createService( new File( "/home/falko/uDigWorkspace", "topp_states.shp" ).toURI().toURL() );
        }
        catch (Exception e) {
            log.warn( "unexpected: ", e );
        }
	}

    private void createService( URL location )
            throws MalformedURLException, IOException {
        ShpServiceExtension creator = new ShpServiceExtension();

        Map<String, Serializable> params = creator.createParams( location );
        IService service = creator.createService( location, params );
        IServiceInfo info = service.getInfo( new NullProgressMonitor() ); // load

        CatalogPlugin.getDefault().getLocalCatalog().add( service );
        
        MessageDialog.openInformation( window.getShell(), 
                "Info", "Created Shapefile service: " + info.getTitle() );
    }

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}