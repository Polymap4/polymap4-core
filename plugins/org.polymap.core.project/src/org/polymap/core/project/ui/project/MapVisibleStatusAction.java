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
package org.polymap.core.project.ui.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.commands.ExecutionException;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.operations.OpenMapOperation;
import org.polymap.core.project.ui.util.SelectionAdapter;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapVisibleStatusAction
        implements IObjectActionDelegate, IViewActionDelegate {

    private static Log log = LogFactory.getLog( MapVisibleStatusAction.class );

    private Set<IMap>           selected = new HashSet();
    
    private IAction             action;
    

    public MapVisibleStatusAction() {
    }


    public void init( IViewPart view ) {
        ProjectRepository.instance().addEntityListener( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent ev ) {
                return ev.getPropertyName().equals( IMap.PROP_VISIBLE )
                        && ev.getSource() instanceof IMap
                        && selected.contains( ev.getSource() );
            }
        });
    }


    public void run( IAction _action ) {
        boolean visible = _action.isChecked();
        for (IMap map : new ArrayList<IMap>( selected )) {
            if (map.isVisible()) {
                map.setVisible( false );
            }
            else {
                try {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    OpenMapOperation op = new OpenMapOperation( map, window.getActivePage() );
                    OperationSupport.instance().execute( op, true, true );
                }
                catch (ExecutionException e) {
                    PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, Messages.get( "operationFailed" ), e );
                }
            }
        }
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }


    public void selectionChanged( IAction _action, ISelection _sel ) {
        selected.clear();
        action = _action;
        
        boolean allMapsVisible = true;
        for (IMap map : new SelectionAdapter( _sel ).elementsOfType( IMap.class )) {
            try {
                if (!map.isVisible()) {
                    allMapsVisible = false;
                }
                selected.add( map );
            }
            catch (NoSuchEntityException e) {
                log.debug( "Map is removed." );
            }                    
        }
        action.setEnabled( !selected.isEmpty() ); 
        action.setChecked( selected.size() == 1 && selected.iterator().next().isVisible()
                || selected.size() > 1 && allMapsVisible );
    }

    
    @EventHandler(display=true)
    public void propertyChange( PropertyChangeEvent ev ) {
        selectionChanged( action, new StructuredSelection( selected.toArray() ) );
    }

}
