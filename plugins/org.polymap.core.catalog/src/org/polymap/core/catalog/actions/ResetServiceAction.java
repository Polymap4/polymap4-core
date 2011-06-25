package org.polymap.core.catalog.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceFactory;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;

import org.polymap.core.workbench.PolymapWorkbench;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Resets a selection of services
 * 
 * @author Jody Garnett
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class ResetServiceAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( ResetServiceAction.class );

    private IStructuredSelection    selection;
    
    
    public void run( IAction action ) {
        if (selection == null) {
            return;
        }
        PlatformGIS.run( new ISafeRunnable() {

            public void handleException( Throwable e ) {
                PolymapWorkbench.handleError( CatalogPlugin.ID, this, "Unable to reset service: " + selection, e );
            }

            public void run() throws Exception {
                List<IService> servers = new ArrayList<IService>();
                for (Object elm : selection.toList()) {
                    if (elm instanceof IService) {
                        servers.add( (IService)elm );
                    } 
                    else {
                        log.warn( "Not a service: " + elm );
                    }
                }
                reset( servers, null );
            }
        });
    }
    
    
    /**
     * Allows a list of services to be reset.
     * <p>
     * In each case a replacement service is made using the same connection
     * parameters; the old service is disposed; and the replacement placed into the
     * catalog.
     * <p>
     * Client code listing to catalog change events will see the event fired off any
     * client code that has tried to cache the IService (to avoid doing a look up
     * each time) will be in trouble.
     * 
     * @param servers List of IService handles to reset
     * @param monitor Progress Monitor used to interrupt the command if needed
     */
    public static void reset( List<IService> servers, IProgressMonitor monitor ) {
        IServiceFactory serviceFactory = CatalogPlugin.getDefault().getServiceFactory();
        ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();

        for (IService original : servers) {
            try {
                final ID id = original.getID();
                log.debug( "Resetting service: " + original.getIdentifier() ); //$NON-NLS-1$

                Map<java.lang.String, java.io.Serializable> connectionParams = original.getConnectionParams();

                IService replacement = null;
                for (IService candidate : serviceFactory.createService( connectionParams )) {
                    try {
                        log.debug( id + " : connecting" ); //$NON-NLS-1$
                        IServiceInfo info = candidate.getInfo( monitor );

                        log.debug( id + " : found " + info.getTitle() ); //$NON-NLS-1$
                        replacement = candidate;

                        break;
                    }
                    catch (Throwable t) {
                        log.debug( id + " : ... " + t.getLocalizedMessage() ); //$NON-NLS-1$
                    }
                }
                if (replacement == null) {
                    log.warn( "Could not reset " + id + " - as we could not connect!", null );
                    continue; // skip - too bad we cannot update status the original
                }
                catalog.replace( id, replacement );
            }
            catch (Throwable failed) {
                log.warn( "Reset failed", failed );
            }
        }
    }


    public void selectionChanged( IAction action, ISelection _selection ) {
        if (!_selection.isEmpty() && _selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection)_selection;
        }
    }

    
    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

}