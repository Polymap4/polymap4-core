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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

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

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.operation.FeatureOperationFactory.IContextProvider;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultMenuContribution
        extends CompoundContributionItem
        implements IExecutableExtension, IContextProvider {

    private static Log log = LogFactory.getLog( DefaultMenuContribution.class );
    
    private static Image icon = DataPlugin.getDefault().imageForName( "icons/etool16/feature_ops.gif" );

    
    public DefaultMenuContribution() {
    }

    public DefaultMenuContribution( String id ) {
        super( id );
    }

    @Override
    public void fill( Menu menu, int index ) {
        super.fill( menu, index );
        
        for (MenuItem item : menu.getItems()) {
            if (item.getMenu() != null
                    && item.getText().equals( Messages.get( "FeatureOperationMenu_title" ) )) {
                item.setImage( icon );
            }
        }
    }

    @Override
    public void fill( ToolBar toolbar, int index ) {
        super.fill( toolbar, index );

        for (ToolItem item : toolbar.getItems()) {
            if (item.getParent() != null
                    && item.getText().equals( Messages.get( "FeatureOperationMenu_title" ) )) {
                item.setImage( icon );
            }
        }
    }


    protected IStructuredSelection currentSelection() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            ISelection sel = window.getSelectionService().getSelection();
            if (sel instanceof IStructuredSelection) {
                return (IStructuredSelection)sel;
            }
        }
        return null;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        DefaultOperationContext context = newContext();

        if (context == null) {
            return new IContributionItem[] {};
        }
        
        MenuManager subMenu = new MenuManager( Messages.get( "FeatureOperationMenu_title" ), "featureOperations" );
        subMenu.setVisible( true );

        FeatureOperationFactory factory = FeatureOperationFactory.forContext( this );
        for (Action action : factory.actions()) {
            ActionContributionItem item = new ActionContributionItem( action ); 
            item.setVisible( true );
            subMenu.add( item );
        }

        return new IContributionItem[] { subMenu };
    }

    @Override
    public void setInitializationData( 
            IConfigurationElement config, String propertyName, Object data ) 
            throws CoreException {
        log.debug( "setInitializationData(): config=" + config + ", propertyName=" + propertyName );
    }

}
