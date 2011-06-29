/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.navigator.filter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class NewFilterAction
        extends Action
        implements IAction {

    private FiltersFolderItem           folder;
    
    
    NewFilterAction( FiltersFolderItem folder ) {
        super( Messages.get( "NewFilterAction_name" ) );
        setToolTipText( Messages.get( "NewFilterAction_tip" ) );
        this.folder = folder;
    }

    public void run() {
        try {
            Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

            final Shell dialog = new Shell( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL );

            Rectangle parentSize = parent.getBounds();
            Point prefSize = dialog.computeSize( SWT.DEFAULT, SWT.DEFAULT );
            dialog.setSize( prefSize );
            int locationX = ( parentSize.width - prefSize.x ) / 2 + parentSize.x;
            int locationY = ( parentSize.height - prefSize.y ) / 2 + parentSize.y;
            dialog.setLocation( new Point( locationX, locationY ) );

            dialog.pack();
            dialog.open();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Fehler beim Öffnen der Attributtabelle.", e );
        }
    }

}
