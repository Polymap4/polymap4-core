/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.project.ui.layer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerVisibleStatusAction
        //extends ActionDelegate
        implements IObjectActionDelegate, IViewActionDelegate {

    private static Log log = LogFactory.getLog( LayerVisibleStatusAction.class );

    private Set<ILayer>         layers = new HashSet();
    
    private IAction             action;
    

    public LayerVisibleStatusAction() {
    }


    public void init( IViewPart view ) {
        ProjectRepository.instance().addEntityListener( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent ev ) {
                return ev.getPropertyName().equals( ILayer.PROP_VISIBLE )
                        && ev.getSource() instanceof ILayer
                        && layers.contains( ev.getSource() );
            }
        });
    }


    public void run( IAction _action ) {
        boolean visible = _action.isChecked(); 
        for (ILayer layer : new ArrayList<ILayer>( layers )) {
            layer.setVisible( visible );
        }
        selectionChanged( _action, new StructuredSelection( layers ) );
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }


    public void selectionChanged( IAction _action, ISelection _sel ) {
        layers.clear();
        action = _action;
        
        if (_sel instanceof IStructuredSelection) {
            Object[] elms = ((IStructuredSelection)_sel).toArray();
            boolean allLayersVisible = true;
            for (Object elm : elms) {
                if (elm instanceof ILayer) {
                    try {
                        if (!((ILayer)elm).isVisible()) {
                            allLayersVisible = false;
                        }
                        layers.add( (ILayer)elm );
                    }
                    catch (NoSuchEntityException e) {
                        log.debug( "Layer is removed." );
                    }                    
                }
            }
            action.setEnabled( !layers.isEmpty() ); 
            action.setChecked( layers.size() == 1 && layers.iterator().next().isVisible()
                    || layers.size() > 1 && allLayersVisible );
        }
    }

    
    @EventHandler(display=true)
    public void propertyChange( PropertyChangeEvent ev ) {
        selectionChanged( action, new StructuredSelection( layers ) );
    }

}
