/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.polymap.styler.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;


import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.polymap.core.model.ACL;
import org.polymap.core.model.ACLUtils;
import org.polymap.core.model.AclPermission;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.MapLayersView;
import org.polymap.styler.ui.StyleView;

/**
 * Action to show the StyleView
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class EditStyleAction 
	implements IViewActionDelegate
	//,	IWorkbenchWindowActionDelegate 
{
	
	private static Log log = LogFactory.getLog(EditStyleAction.class);

	// private IWorkbenchWindow window;

	private MapLayersView map_layers_view;
	
	private ILayer                  layer;
	    
	/**
	 * The constructor.
	 */
	public EditStyleAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {

		try {
			log.info("showing Style view" + map_layers_view);
			
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			
			StyleView style_view = (StyleView)page.showView(StyleView.ID);			
			//style_view.setLayerBySelection(map_layers_view.getSite().getSelectionProvider().getSelection());
			style_view.setLayer(layer);
			//page.getV
			
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	
	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	//public void selectionChanged(IAction action, ISelection selection) {
	//}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
	
	}

	@Override
	public void init(IViewPart view) {
		map_layers_view=(( MapLayersView )view);
		// TODO Auto-generated method stub
	//log.info("action  " + view.getClass());	
	//(( MapLayersView )view).getSite().getSelectionProvider().getSelection();
	}
	
	
	
	public void selectionChanged( IAction action, ISelection sel ) {
	    if (sel instanceof StructuredSelection) {
	        Object elm = ((StructuredSelection)sel).getFirstElement();
	        if (elm instanceof ILayer) {
	            layer = (ILayer)elm;
	            
	            // check ACL permission
	            boolean hasPermission = layer instanceof ACL
	            ? ACLUtils.checkPermission( (ACL)layer, AclPermission.WRITE, false )
	                    : true;
	            
	            action.setEnabled( hasPermission );
	            return;
	        }
	    }
	    layer = null;
	    action.setEnabled( false );
	}
	
}