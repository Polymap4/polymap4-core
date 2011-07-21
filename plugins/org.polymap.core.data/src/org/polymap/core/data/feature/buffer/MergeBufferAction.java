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
package org.polymap.core.data.feature.buffer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MergeBufferAction
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( MergeBufferAction.class );

    private List<ILayer>        layers = new ArrayList();

    private IAction             action;

    
    public void run( IAction _action ) {
    }


    public void selectionChanged( IAction _action, ISelection _sel ) {
        layers.clear();
        action = _action;
        
        if (_sel instanceof IStructuredSelection) {
            Object[] elms = ((IStructuredSelection)_sel).toArray();
            
            for (Object elm : elms) {
                if (elm instanceof ILayer) {
                    layers.add( (ILayer)elm );
                }
            }
            
            action.setEnabled( !layers.isEmpty() ); 
        }
    }
    

    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

}
