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
package org.polymap.rhei.navigator.layer;

import java.util.ArrayList;
import java.util.List;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerVisibleStatusAction
        //extends ActionDelegate
        implements IObjectActionDelegate, IViewActionDelegate, PropertyChangeListener {

    private static Log log = LogFactory.getLog( LayerVisibleStatusAction.class );

    private List<ILayer>        layers = new ArrayList();
    
    private IAction             action;
    

    public LayerVisibleStatusAction() {
    }


    public void init( IViewPart view ) {
    }


    public void run( IAction _action ) {
        for (ILayer layer : layers) {
            layer.setVisible( _action.isChecked() );
        }
        selectionChanged( _action, new StructuredSelection( layers ) );
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }


    public void selectionChanged( IAction _action, ISelection _sel ) {
        for (ILayer layer : layers) {
            layer.addPropertyChangeListener( this );            
        }
        layers.clear();
        action = _action;
        
        if (_sel instanceof IStructuredSelection) {
            Object[] elms = ((IStructuredSelection)_sel).toArray();
            boolean allLayersVisible = true;
            for (Object elm : elms) {
                if (elm instanceof ILayer) {
                    layers.add( (ILayer)elm );
                    ((ILayer)elm).addPropertyChangeListener( this );
                    if (!((ILayer)elm).isVisible()) {
                        allLayersVisible = false;
                    }
                }
            }
            action.setEnabled( !layers.isEmpty() ); 
            action.setChecked( layers.size() == 1 && layers.get( 0 ).isVisible()
                    || layers.size() > 1 && allLayersVisible );
        }
    }

    
    public void propertyChange( PropertyChangeEvent ev ) {
        if (ev.getSource() instanceof ILayer
                && ev.getPropertyName().equals( ILayer.PROP_VISIBLE )) {
            selectionChanged( action, new StructuredSelection( layers ) );
        }
    }

}
