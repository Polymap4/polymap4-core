/* 
 * polymap.org
 * Copyright 2012-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.catalog.actions;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

import org.polymap.core.catalog.Messages;
import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 * <p/>
 * XXX With new catalog implementation, this should be an operation.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RemoveServiceAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( RemoveServiceAction.class );
    
    public static String i18n( String key, Object... args) {
        return Messages.get( "RemoveServiceAction_" + key, args );
    }

    private IStructuredSelection    selection;


    public void run( IAction action ) {
        try {
//            if (MessageDialog.openQuestion( PolymapWorkbench.getShellToParentOn(),
//                    i18n( "confirmTitle" ), i18n( "confirmMessage" ) ) ) {
                ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
                for (Object elm : selection.toList()) {
                    catalog.remove( (IService)elm );
//                }
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( "", this, e.getLocalizedMessage(), e );
        }
    }

    
    public void selectionChanged( IAction action, ISelection _selection ) {
        if (!_selection.isEmpty() && _selection instanceof IStructuredSelection) {
            selection = (IStructuredSelection)_selection;

            for (Object elm : selection.toList()) {
                if (elm instanceof IService) {
                    try {
                        IService service = (IService)elm;
                        
                        // check ACL permission
                        ACL acl = (ACL)service.getAdapter( ACL.class );
                        if (acl != null) {
                            boolean deletePermitted = ACLUtils.checkPermission( acl, AclPermission.DELETE, false );
                            log.info( "delete permitted: " + deletePermitted + " on service: " + acl );
                            if (!deletePermitted) {
                                selection = null;
                                break;
                            }
                        }
                    }
                    catch (Exception e) {
                        log.warn( "" );
                        log.debug( "", e );
                    }
                }
                else {
                    selection = null;
                    break;
                }
            }

            action.setEnabled( selection != null );
        }
    }

    
    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

}
