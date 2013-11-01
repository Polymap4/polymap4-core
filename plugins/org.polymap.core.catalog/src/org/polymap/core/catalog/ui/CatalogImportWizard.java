/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.catalog.ui;

import java.util.Collection;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.ui.CatalogView;
import net.refractions.udig.catalog.ui.CatalogTreeViewer;
import net.refractions.udig.catalog.ui.workflow.EndConnectionState;
import net.refractions.udig.catalog.ui.workflow.State;
import net.refractions.udig.catalog.ui.workflow.Workflow;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizard;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardPageProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.Messages;
import org.polymap.core.catalog.operations.AddServiceOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 * 
 * <p/>
 * Taken from {@link net.refractions.udig.catalog.internal.ui.CatalogImport} and adapted
 * for the Polymap3 way of handling adding resources to the catalog.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CatalogImportWizard
        extends WorkflowWizard {
    
    
	public CatalogImportWizard( Workflow workflow,
            Map<Class< ? extends State>, WorkflowWizardPageProvider> map ) {
        super( workflow, map );
        setWindowTitle( i18n( "title" ) );
    }
	
	
	@Override
	protected boolean performFinish(IProgressMonitor monitor) {
		//get the connection state from the pipe
		EndConnectionState connState = getWorkflow().getState( EndConnectionState.class );
		if (connState == null) {
			return false;
		}
		
		//
		final Collection<IService> services = connState.getServices();
		if (services == null || services.isEmpty()) {
			return false;
		}
		
		// add the services to the catalog
		ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
		for (IService service : services) {
		    try {
		        AddServiceOperation op = new AddServiceOperation( catalog, service );
		        OperationSupport.instance().execute( op, false, false );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( CatalogPlugin.ID, this, i18n( "errorMsg" ), e );
                return false;
            }
		}
		
		// select the first service
		// TODO: this has threading issues
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                try {
                    CatalogView view = getCatalogView();
                    if (view != null) {
                        CatalogTreeViewer treeviewer = view.getTreeviewer();
                        treeviewer.setSelection( new StructuredSelection( services.iterator().next() ) );
                    }
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( CatalogPlugin.ID, CatalogImportWizard.this, "Unable to add new data source.", e );
                }
            }
        });
		return true;
	}

	
    protected boolean isShowCatalogView() {
        return true;
    }

    
    protected CatalogView getCatalogView() throws PartInitException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        return isShowCatalogView() 
                ? (CatalogView)page.showView( CatalogView.VIEW_ID )
                : (CatalogView)page.findView( CatalogView.VIEW_ID );
    }
    
    
    protected String i18n( String key, Object... args ) {
        return Messages.get( "CatalogImportWizard_" + key, args );
    }
    
}
