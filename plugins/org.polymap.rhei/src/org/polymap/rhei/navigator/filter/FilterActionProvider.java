/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.navigator.filter;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

import org.polymap.rhei.filter.IFilter;

/**
 * Provides {@link Action}s for {@link IFilter} and {@link FiltersFolderItem}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class FilterActionProvider
        extends CommonActionProvider {

    private Log log = LogFactory.getLog( FilterActionProvider.class );

    private static final String FILTER_ACTION_GROUP = "_filterActionGroup_"; //$NON-NLS-1$
    
    private ICommonActionExtensionSite      site;
    
    
    public FilterActionProvider() {
    }

    
    public void init( ICommonActionExtensionSite _site ) {
        log.debug( "init(): ..." );
        this.site = _site;
    }

    
    public void fillContextMenu( IMenuManager menu ) {
        log.debug( "fillContextMenu(): ..." );
        
        Set exts = site.getContentService().findContentExtensionsByTriggerPoint( 
                IFilter.class );
        
        IStructuredSelection sel = (IStructuredSelection)getContext().getSelection();
        
        // IFilter
        if (sel.getFirstElement() instanceof IFilter) {
            IFilter filter = (IFilter)sel.getFirstElement();
            if (filter.hasControl()) {
                menu.add( new OpenFilterDialogAction( filter ) );
                menu.add( new OpenFilterViewAction( filter ) );
            }
            else {
                menu.add( new OpenFilterAction( filter ) );
            }
        }
//        // FiltersFolderItem
//        else if (sel.getFirstElement() instanceof FiltersFolderItem) {
//            FiltersFolderItem folder = (FiltersFolderItem)sel.getFirstElement();
//            menu.add( new NewFilterAction( folder ) );
//        }
        
        menu.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        menu.appendToGroup( IWorkbenchActionConstants.MB_ADDITIONS, 
                new Separator( FILTER_ACTION_GROUP ) );
    }

    
    public void fillActionBars( IActionBars actionBars ) {
        log.debug( "fillActionBars(): ..." );

        IToolBarManager toolbar = actionBars.getToolBarManager();
        toolbar.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        toolbar.appendToGroup( IWorkbenchActionConstants.MB_ADDITIONS, 
                new Separator( FILTER_ACTION_GROUP ) );
    }

}
