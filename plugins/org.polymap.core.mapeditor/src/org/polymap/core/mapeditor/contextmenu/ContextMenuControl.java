/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH, All rights reserved.
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

import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.IWorkbenchActionConstants;

import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.runtime.ListenerList;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.controls.Control;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class ContextMenuControl
        extends Control 
        implements ISelectionProvider {

    private ListenerList<ISelectionChangedListener> selectionListeners = new ListenerList();
    
    private ISelection              selection;
    
    private MapEditor               mapEditor;
    
    
    public ContextMenuControl( MapEditor mapEditor ) {
        this.mapEditor = mapEditor;
        setMap( mapEditor.getWidget().getMap() );
        //super.create( "new OpenLayers.Control.ContextMenuControl();" );
    }

    
    public void setMap( OpenLayersMap map ) {
        // hook context menu
        final MenuManager contextMenu = new MenuManager();
        contextMenu.setRemoveAllWhenShown( true );
        
        contextMenu.addMenuListener( new IMenuListener() {
            public void menuAboutToShow( IMenuManager manager ) {
                // create site
                ContextMenuSite site = new ContextMenuSite() {
                    public MapEditor getMapEditor() {
                        return mapEditor;
                    }
                };
                
//                // extensions -> sort priority
//                Multimap<Integer,IContextMenuContribution> sorted = Multimaps.newListMultimap( 
//                        new TreeMap<Integer,Collection<IContextMenuContribution>>(), 
//                        new Supplier<List<IContextMenuContribution>>() {
//                            public List<IContextMenuContribution> get() {
//                                return new ArrayList();
//                            }
//                        } );
                
                // groups
                manager.add( new Separator( IContextMenuContribution.GROUP_TOP ) );
                manager.add( new GroupMarker( IContextMenuContribution.GROUP_TOP ) );
                manager.add( new Separator( IContextMenuContribution.GROUP_HIGH ) );
                manager.add( new GroupMarker( IContextMenuContribution.GROUP_HIGH ) );
                manager.add( new Separator( IContextMenuContribution.GROUP_MID ) );
                manager.add( new GroupMarker( IContextMenuContribution.GROUP_MID ) );
                manager.add( new Separator( IContextMenuContribution.GROUP_LOW ) );
                manager.add( new GroupMarker( IContextMenuContribution.GROUP_LOW ) );

                // add to menu
                for (ContextMenuExtension ext : ContextMenuExtension.all()) {
                    IContextMenuContribution item = ext.newProvider().init( site );
                    contextMenu.appendToGroup( item.getMenuGroup(), item );
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
