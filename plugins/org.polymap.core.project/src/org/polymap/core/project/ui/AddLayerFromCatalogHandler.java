/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.IGeoResource;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.operations.NewLayerOperation;

/**
 * This is handler for the <em>....</em> command.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class AddLayerFromCatalogHandler
        extends AbstractHandler
        implements IHandler, IHandler2 {

    private static Log log = LogFactory.getLog( AddLayerFromCatalogHandler.class );

    private ISelectionListener          selectionListener;
    
    private IGeoResource                selectedGeoRes;
    
    
    public AddLayerFromCatalogHandler() {
        super();
        Display.getCurrent().asyncExec( new Runnable() {
            public void run() {
                enableListener( true );
            }
        });
    }


    public void dispose() {
        enableListener( false );
    }


    public Object execute( ExecutionEvent ev )
            throws ExecutionException {
        IMap map = ProjectPlugin.getSelectedMap();

        NewLayerOperation op = new NewLayerOperation(); 
        op.init( ProjectPlugin.getSelectedMap(), selectedGeoRes ); 
        IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindow( ev ).getWorkbench();
        
        OperationSupport.instance().execute( op, true, false );
        return null;
    }


    public boolean isEnabled() {
        log.debug( "currentMap= " + ProjectPlugin.getSelectedMap() );
        boolean isEnabled = selectedGeoRes != null && ProjectPlugin.getSelectedMap() != null;
//        enableListener( isEnabled );
        return isEnabled;
    }


    public boolean isHandled() {
        return isEnabled();
    }


    private void enableListener( boolean enable ) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                // enable
                if (enable) {
                    if (selectionListener == null) {
                        selectionListener = new ISelectionListener() {
                            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
                                selectedGeoRes = null;
                                if (sel instanceof IStructuredSelection) {
                                    Object elm = ((IStructuredSelection)sel).getFirstElement();
                                    if (elm != null && elm instanceof IGeoResource) {
                                        selectedGeoRes = (IGeoResource)elm;
                                    }
                                }
                            }
                        };
                        page.addSelectionListener( selectionListener );
                    }
                }
                // disable
                else {
                    if (selectionListener != null) {
                        page.removeSelectionListener( selectionListener );
                        selectionListener = null;
                    }
                }
            }
        }
        log.debug( "handler status: enabled= " + (selectionListener != null) );
    }

}