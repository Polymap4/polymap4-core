/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.operation.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;

import org.polymap.core.CorePlugin;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.StatusDispatcher;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class SaveChangesAction
        implements IWorkbenchWindowActionDelegate, IOperationHistoryListener {

    private static Log log = LogFactory.getLog( SaveChangesAction.class );

    /** The action we are working for. Can I just store this and use in modelChanged() ? */
    private IAction                 action;
    
    private OperationSupport        operationSupport;

    private static ImageDescriptor  origImage = CorePlugin.instance().imageDescriptor( "icons/etool16/save.gif" );

    
    public void init( IWorkbenchWindow window ) {
        operationSupport = OperationSupport.instance();
        operationSupport.addOperationHistoryListener( this );
    }

    
    public void dispose() {
        operationSupport.removeOperationHistoryListener( this );
        
    }


    public void historyNotification( OperationHistoryEvent ev ) {
        log.debug( "History changed: ev= " + ev );

        if (action != null) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    if (operationSupport.undoHistorySize() > 0) {
                        Image image = CorePlugin.instance().imageForDescriptor( origImage, "_saveActionOrig" );
                        ImageDescriptor ovr = CorePlugin.instance().imageDescriptor( "icons/ovr16/dirty_ovr2.png" );
                        action.setImageDescriptor( new DecorationOverlayIcon( image, ovr, IDecoration.BOTTOM_RIGHT ) );
                        //action.setToolTipText( "Operations: " + operationSupport.undoHistorySize() );
                    }
                    else {
                        action.setImageDescriptor( origImage );
                        //action.setToolTipText( "Save (including open editors)" );                        
                    }
                }
            });
        }
    }


    public void run( IAction _action ) {
        try {
            operationSupport.saveChanges();
        }
        catch (Throwable e) {
            StatusDispatcher.handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }

    
    public void selectionChanged( IAction _action, ISelection _selection ) {
        this.action = _action;
    }
    
}
