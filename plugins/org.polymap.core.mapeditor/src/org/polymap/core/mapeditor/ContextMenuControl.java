/* 
 * polymap.org
 * Copyright 2012-2014, Polymap GmbH, All rights reserved.
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
package org.polymap.core.mapeditor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.IWorkbenchActionConstants;

import org.polymap.core.runtime.ListenerList;

import org.polymap.rap.openlayers.control.Control;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContextMenuControl
        extends Control 
        implements ISelectionProvider {

    private ListenerList<ISelectionChangedListener> selectionListeners = new ListenerList();
    
    private ISelection                  selection;
    
    private MapViewer                   mapViewer;
    
    /** Providers registered via {@link #addProvider(IContextMenuProvider)}. */
    private Set<IContextMenuProvider>   providers = new HashSet();
    
    
    public ContextMenuControl( MapViewer mapViewer ) {
        this.mapViewer = mapViewer;
        setMap( mapViewer.getMap() );
        //super.create( "new OpenLayers.Control.ContextMenuControl();" );
    }

    
    public boolean addProvider( IContextMenuProvider... _providers ) {
        return this.providers.addAll( Arrays.asList( _providers ) );
    }
    
    
    public boolean removeProvider( IContextMenuProvider provider ) {
        return providers.remove( provider );
    }
    
    
    public void setMap( OpenLayersMap map ) {
        // hook context menu
        final MenuManager contextMenu = new MenuManager();
        contextMenu.setRemoveAllWhenShown( true );
        
        contextMenu.addMenuListener( new IMenuListener2() {
            @Override
            public void menuAboutToHide( IMenuManager manager ) {
                // avoid displaying the old menu when opening
                contextMenu.removeAll();
            }
            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                // create site
                ContextMenuSite site = new ContextMenuSite() {
                    @Override
                    public MapViewer getMapViewer() {
                        return mapViewer;
                    }
                };
                
                // groups
                manager.add( new Separator( IContextMenuContribution.GROUP_TOP ) );
                manager.add( new GroupMarker( IContextMenuContribution.GROUP_TOP ) );
                manager.add( new Separator( IContextMenuContribution.GROUP_HIGH ) );
                manager.add( new GroupMarker( IContextMenuContribution.GROUP_HIGH ) );
                manager.add( new Separator( IContextMenuContribution.GROUP_MID ) );
                manager.add( new GroupMarker( IContextMenuContribution.GROUP_MID ) );
                manager.add( new Separator( IContextMenuContribution.GROUP_LOW ) );
                manager.add( new GroupMarker( IContextMenuContribution.GROUP_LOW ) );

//                // find extensions and add to menu
//                for (ContextMenuExtension ext : ContextMenuExtension.all()) {
//                    IContextMenuContribution item = ext.createContribution();
//                    if (item.init( site )) {
//                        contextMenu.appendToGroup( item.getMenuGroup(), item );
//                    }
//                }
                // 
                for (IContextMenuProvider provider : providers) {
                    IContextMenuContribution item = provider.createContribution();
                    if (item.init( site )) {
                        contextMenu.appendToGroup( item.getMenuGroup(), item );
                    }
                }
                
                // additions
                manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
                manager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
            }
        } );
        Menu menu = contextMenu.createContextMenu( map.getWidget() );
        map.getWidget().setMenu( menu );
    }

    
    // ISelectionProvider *********************************
    
    public void addSelectionChangedListener( ISelectionChangedListener listener ) {
        selectionListeners.add( listener );
    }

    public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
        selectionListeners.remove( listener );
    }

    public ISelection getSelection() {
        return selection;
    }

    public void setSelection( ISelection selection ) {
        this.selection = selection;
        
        SelectionChangedEvent ev = new SelectionChangedEvent( this, selection );
        for (ISelectionChangedListener l : selectionListeners) {
            l.selectionChanged( ev );
        }
    }
    
}
