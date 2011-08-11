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
import java.util.List;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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

import org.eclipse.core.commands.ExecutionException;

import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.operations.LayerEditableOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class LayerEditableStatusAction
        implements IObjectActionDelegate, IViewActionDelegate, PropertyChangeListener {

    private static Log log = LogFactory.getLog( LayerEditableStatusAction.class );

    /**
     * Currently just one layer is editable at a given time; keep the list for future
     * enhancements :)
     */
    private List<ILayer>        layers = new ArrayList();
    
    private IAction             action;
    

    public LayerEditableStatusAction() {
    }


    public void init( IViewPart view ) {
    }


    public void run( IAction _action ) {
        try {
            LayerEditableOperation op = new LayerEditableOperation( 
                    new ArrayList<ILayer>( layers ), _action.isChecked() );
            OperationSupport.instance().execute( op, true, false );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, "", e );
        }
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }


    public void selectionChanged( IAction _action, ISelection _sel ) {
        for (ILayer layer : layers) {
            try {
                layer.removePropertyChangeListener( this );
            }
            catch (NoSuchEntityException e) {
                // layer might have been removed
            }            
        }
        layers.clear();
        action = _action;
        
        if (_sel instanceof IStructuredSelection) {
            Object[] elms = ((IStructuredSelection)_sel).toArray();
            boolean allLayersEditable = true;
            boolean allLayersVisible = true;
            boolean allLayersNotSelectable = true;
            boolean allLayersPermitted = true;
            
            for (Object elm : elms) {
                if (elm instanceof ILayer) {
                    
                    layers.add( (ILayer)elm );
                    
                    ((ILayer)elm).addPropertyChangeListener( this );
                    
                    if (!((ILayer)elm).isEditable()) {
                        allLayersEditable = false;
                    }
                    if (!((ILayer)elm).isVisible()) {
                        allLayersVisible = false;
                    }
                    if (((ILayer)elm).isSelectable()) {
                        allLayersNotSelectable = false;
                    }

                    // check ACL permission
                    if (elm instanceof ACL 
                            && !ACLUtils.checkPermission( (ACL)elm, AclPermission.WRITE, false )) {
                    // FIXME check service ACL
//                    layer.getGeoResource().resolve( IService.class );
                        allLayersPermitted = false;
                    }

                }
            }
            action.setEnabled( !layers.isEmpty() && allLayersVisible && allLayersPermitted && allLayersNotSelectable ); 
            action.setChecked( layers.size() == 1 && layers.get( 0 ).isEditable()
                    || layers.size() > 1 && allLayersEditable );
        }
    }

    
    public void propertyChange( PropertyChangeEvent ev ) {
        String prop = ev.getPropertyName();

        if (ev.getSource() instanceof ILayer
                && (prop.equals( ILayer.PROP_EDITABLE ) || prop.equals( ILayer.PROP_VISIBLE ) || prop.equals( ILayer.PROP_SELECTABLE ))) {
            selectionChanged( action, new StructuredSelection( layers ) );
        }
    }

}
