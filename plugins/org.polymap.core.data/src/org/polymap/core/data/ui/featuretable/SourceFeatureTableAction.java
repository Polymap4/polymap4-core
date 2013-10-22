/* 
 * polymap.org
 * Copyright (C) 2009-2013 Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.ui.featuretable;

import net.refractions.udig.catalog.IGeoResource;

import org.geotools.data.FeatureSource;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class SourceFeatureTableAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private IWorkbenchPart      activePart;
    
    private IGeoResource        selected;
    

    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
        this.activePart = targetPart;
    }


    public void runWithEvent( IAction action, Event ev ) {
        Display.getCurrent().asyncExec( new Runnable() {
            public void run() {
                try {            
                    // FIXME check blocking
                    FeatureSource fs = selected.resolve( FeatureSource.class, null );
                    if (fs != null) {
                        SourceFeatureTableView.open( fs );
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException( e.getMessage(), e );
                }
            }
        });
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        selected = null;
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            if (elm == null) {
                return;
            }
            else if (elm instanceof ILayer) {
                selected = ((ILayer)elm).getGeoResource();
            }
            else if (elm instanceof IGeoResource) {
                selected = (IGeoResource)elm;
            }
        }
    }

}
