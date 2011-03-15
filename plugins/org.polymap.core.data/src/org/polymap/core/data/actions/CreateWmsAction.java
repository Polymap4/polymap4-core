package org.polymap.core.data.actions;

import java.util.Map;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.internal.wms.WMSServiceExtension;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class CreateWmsAction 
        implements IWorkbenchWindowActionDelegate {
	
    private static Log log = LogFactory.getLog( CreateWmsAction.class );

    private IWorkbenchWindow        window;
	
	/**
	 * The constructor.
	 */
	public CreateWmsAction() {
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
            createService( new URL( "http://www.polymap.de/geoserver/wms?" ) );
            // forsten.sachsen
            createService( new URL( "http://www.forsten.sachsen.de/kartendienst/wald?" ) );
        }
        catch (Exception e) {
            log.warn( "unexpected: ", e );
        }
	}

    private void createService( URL location )
            throws MalformedURLException, IOException {
        WMSServiceExtension creator = new WMSServiceExtension();

        Map<String, Serializable> params = creator.createParams( location );
        IService service = creator.createService( location, params );
        IServiceInfo info = service.getInfo( new NullProgressMonitor() ); // load

        CatalogPlugin.getDefault().getLocalCatalog().add( service );
        
        MessageDialog.openInformation( window.getShell(), 
                "polymap3.core Plug-in", "Created WMS service: " + info.getTitle() );
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
}