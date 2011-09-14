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
package org.polymap.core.data.operations.feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CountFeaturesOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( CountFeaturesOperation.class );


    public boolean init( IFeatureOperationContext context ) {
        return true;
    }

    public String getLabel() {
        return "Elementzahl";
    }

    public String getTooltip() {
        return "Anzahl der Elemente";
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }


    public Status execute( IFeatureOperationContext context, IProgressMonitor monitor )
    throws Exception {
        final int result = context.featuresSize();
        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                Shell shell = PolymapWorkbench.getShellToParentOn();
                MessageDialog.openInformation( shell, "Ergebnis", "Anzahl der Features: " + result );
            }
        });
        return Status.OK;
    }


    public Status undo( IFeatureOperationContext context, IProgressMonitor monitor )
    throws Exception {
        return Status.OK;
    }


    public Status redo( IFeatureOperationContext context, IProgressMonitor monitor )
    throws Exception {
        return Status.OK;
    }


    public boolean canExecute() {
        return true;
    }

    public boolean canRedo() {
        return true;
    }

    public boolean canUndo() {
        return true;
    }
    
}
