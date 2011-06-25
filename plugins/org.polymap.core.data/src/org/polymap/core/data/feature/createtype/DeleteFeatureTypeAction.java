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
package org.polymap.core.data.feature.createtype;

import org.geotools.data.DataStore;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Provides a popup menu for {@link IResolve}/{@link IService} entries triggering a
 * {@link CreateFeatureTypeOperation}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DeleteFeatureTypeAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( DeleteFeatureTypeAction.class );

    private IGeoResource            geores;


    public void runWithEvent( IAction action, Event event ) {
        try {
            DeleteFeatureTypeOperation op = new DeleteFeatureTypeOperation( geores );
            OperationSupport.instance().execute( op, true, true );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "", e );
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        geores = null;
        action.setEnabled( false );

        if (sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            if (elm instanceof IGeoResource) {
                try {
                    geores = (IGeoResource)elm;
                    IService service = geores.service( new NullProgressMonitor() );
                    action.setEnabled( service.canResolve( DataStore.class ) );

                    // check ACL permission
                    if (service != null && service instanceof IAdaptable) {
                        ACL acl = (ACL)((IAdaptable)service).getAdapter( ACL.class );
                        if (acl != null) {
                            action.setEnabled( ACLUtils.checkPermission( acl, AclPermission.WRITE, false ) );
                        }
                    }
                }
                catch (Exception e) {
                    log.warn( "" );
                    log.debug( "", e );
                }
            }
        }
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

}
