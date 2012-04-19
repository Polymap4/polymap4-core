/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.contextmenu;

import java.util.Collections;

import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;

import org.eclipse.core.commands.ExecutionException;

import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.operations.LayerSelectableOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerSelectableContribution
        extends ContributionItem
        implements IContextMenuContribution {

    private ContextMenuSite            site;

    
    public LayerSelectableContribution() {
    }

    public IContextMenuContribution init( ContextMenuSite _site ) {
        this.site = _site;
        
        setVisible( false );
        for (ILayer layer : site.getMap().getLayers()) {
            if (layer.isVisible()) {
                setVisible( true );
                break;
            }
        }
        return this;
    }

    public String getMenuGroup() {
        return GROUP_MID;
    }

    public void fill( Menu parent, int index ) {
        for (final ILayer layer : site.getMap().getLayers()) {
            if (layer.isVisible()) {
                Action action = new Action( layer.getLabel(), Action.AS_CHECK_BOX ) {
                    public void run() {
                        try {
                            LayerSelectableOperation op = new LayerSelectableOperation( 
                                    Collections.singletonList( layer ), !layer.isSelectable() );
                            OperationSupport.instance().execute( op, true, false );
                        }
                        catch (ExecutionException e) {
                            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
                        }
                    }            
                };
                action.setChecked( layer.isSelectable() );
                action.setImageDescriptor( MapEditorPlugin.imageDescriptorFromPlugin(
                        MapEditorPlugin.PLUGIN_ID, "icons/etool16/layer_selectable.gif" ) );
                new ActionContributionItem( action ).fill( parent, index );
            }
        }
    }

}
