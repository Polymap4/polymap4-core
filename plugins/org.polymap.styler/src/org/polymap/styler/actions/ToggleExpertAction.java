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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;


import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.polymap.styler.ui.StyleView;
import org.polymap.styler.ui.StylerSessionSingleton;

/**
 * Action to show the StyleView
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class ToggleExpertAction 
	implements IViewActionDelegate 
{
	
	private static Log log = LogFactory.getLog(ToggleExpertAction.class);

	private IViewPart my_view;

	/**
	 * The constructor.
	 */
	public ToggleExpertAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		log.info("toggle expert action called " + action.isChecked());
		StylerSessionSingleton.getInstance().setExpertMode(action.isChecked());
		((StyleView)my_view).refresh_tree();
	}
	
	
	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		log.info("toggle expert sel changed called");
		
	}

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
		log.info("window init");
	}

	@Override
	public void init(IViewPart view) {
		my_view=view;
		setActionByView();
		log.info("Toggle Expert Action got a view");
	}
	
	/**
	 * when the expert state is changed somewhere else update the state here
	 * 
	 * @param new_state
	 */
	public static void setExpertState(boolean new_state) {
	
		/*
		 * if (my_view==null)	{
		 
			log.warn("trying to set action state without view");
			return;
		}
		
		IViewSite site = (IViewSite) my_view.getSite();
		IContributionItem item = 
		site.getActionBars().getToolBarManager().find("org.polymap.styler.actions.ToggleExpertAction");
		 ((ActionContributionItem) item).getAction().setChecked(new_state);
		 		 
		 log.info("setting expert mode to " + new_state);
		*/ 
		if (action==null) {
			log.warn("action is null");
			return;
		}
		
		action.setChecked(new_state);
		log.info("setting expert mode to " + new_state);
	}
	
	private static IAction action;
	
	public void setActionByView() {
		if (my_view==null)	{
			log.warn("trying to set action state without view");
			return;
		}
		
		IViewSite site = (IViewSite) my_view.getSite();
		IContributionItem item = 
		site.getActionBars().getToolBarManager().find("org.polymap.styler.actions.ToggleExpertAction");
		action=((ActionContributionItem) item).getAction();
		 
		 log.info("got action " + action);
	}
}