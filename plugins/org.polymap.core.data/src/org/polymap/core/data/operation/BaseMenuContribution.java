/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class BaseMenuContribution
        extends CompoundContributionItem
        implements IExecutableExtension {

    private static Log log = LogFactory.getLog( BaseMenuContribution.class );

    
    public BaseMenuContribution() {
    }

    public BaseMenuContribution( String id ) {
        super( id );
    }


    /**
     * Creates a new context in the given environment and for the
     * {@link #currentSelection()}.
     * 
     * @return A new context, or null it there is nothing to contribute.
     */
    protected abstract IFeatureOperationContext newContext();


    public IStructuredSelection currentSelection() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            ISelection sel = window.getSelectionService().getSelection();
            if (sel instanceof IStructuredSelection) {
                return (IStructuredSelection)sel;
            }
        }
        return null;
    }

    
    protected IContributionItem[] getContributionItems() {
        IFeatureOperationContext context = newContext();

        if (context == null) {
            return new IContributionItem[] {};
        }
        
        MenuManager subMenu = new MenuManager( "Feature Operations", "featureOperations" );

        FeatureOperationFactory factory = FeatureOperationFactory.instance();
        for (Action action : factory.actionsFor( context )) {
            ActionContributionItem item = new ActionContributionItem( action ); 
            item.setVisible( true );
            subMenu.add( item );
        }

        return new IContributionItem[] { subMenu };
    }

    
    public void setInitializationData( 
            IConfigurationElement config, String propertyName, Object data ) 
            throws CoreException {
        log.info( "setInitializationData(): config=" + config + ", propertyName=" + propertyName );
    }

}
