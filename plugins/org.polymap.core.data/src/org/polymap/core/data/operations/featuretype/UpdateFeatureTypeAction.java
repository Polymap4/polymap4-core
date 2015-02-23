/*
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.operations.featuretype;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.ui.featuretypeeditor.FeatureTypeEditor;
import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Provides a popup menu for {@link IResolve}/{@link IService} entries triggering a
 * {@link UpdateFeatureTypeOperation}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UpdateFeatureTypeAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( UpdateFeatureTypeAction.class );

    private static final IMessages  i18n = Messages.forClass( UpdateFeatureTypeAction.class );
    
    private IGeoResource            geores;


    public void runWithEvent( IAction action, Event event ) {
        try {
            IService service = geores.service( new NullProgressMonitor() );
            // DataAccess -> start update operation
            if (service != null && service.canResolve( DataAccess.class )) {
                UpdateFeatureTypeOperation op = new UpdateFeatureTypeOperation( geores );
                OperationSupport.instance().execute( op, true, true );
            }
            // no DataAccess (EntityFeatureSource) -> try FeatureSource and display simple dialog
            else if (geores.canResolve( FeatureSource.class )) {
                final FeatureSource fs = geores.resolve( FeatureSource.class, null );
                TitleAreaDialog dialog = new TitleAreaDialog( PolymapWorkbench.getShellToParentOn() ) {
                    @Override
                    protected Control createDialogArea( Composite parent ) {
                        setTitle( i18n.get( "EditorDialog_title", fs.getSchema().getName().getLocalPart() ) );
                        setMessage( i18n.get( "EditorDialog_msg" ) );
                        Composite area = (Composite)super.createDialogArea( parent );
                        FeatureTypeEditor editor = new FeatureTypeEditor();
                        editor.createTable( area, null, (SimpleFeatureType)fs.getSchema() );
                        return area;
                    }
                };
                dialog.open();
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
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
                    IService service = ((IGeoResource)elm).service( new NullProgressMonitor() );
                    boolean enabled = service != null; // && service.canResolve( DataAccess.class );

                    // check ACL permission
                    if (enabled && service instanceof IAdaptable) {
                        ACL acl = (ACL)((IAdaptable)service).getAdapter( ACL.class );
                        if (acl != null) {
                            boolean deletePermitted = ACLUtils.checkPermission( acl, AclPermission.WRITE, false );
                            log.info( "delete permitted: " + deletePermitted + " on service: " + acl );
                            enabled &= deletePermitted;
                        }
                    }

                    if (!enabled) {
                        return;
                    }
                }
                catch (Exception e) {
                    log.warn( "" );
                    log.debug( "", e );
                }
            }
            action.setEnabled( true );
        }
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

}
