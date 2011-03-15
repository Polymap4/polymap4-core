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

package org.polymap.core.mapeditor.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import org.polymap.core.mapeditor.operations.SetLayerBoundsOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.MapLayersView;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class SetLayerBoundsAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( SetLayerBoundsAction.class );

    private MapLayersView           view;
    
    private ILayer                  layer;
    
    
    public void init( IViewPart view0 ) {
        this.view = (MapLayersView)view0;
    }


    public void run( IAction action ) {
        try {
            CoordinateReferenceSystem crs = layer.getMap().getCRS();
            SetLayerBoundsOperation op = new SetLayerBoundsOperation( layer, crs );

            OperationSupport.instance().execute( op, false, false );
            System.out.println( "RESULT: " + op.result );

            MessageBox mbox = new MessageBox( 
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    SWT.YES | SWT.NO | SWT.ICON_INFORMATION | SWT.APPLICATION_MODAL );
            mbox.setMessage( "Bounds: " + op.result );
            mbox.setText( op.getLabel() );
            int result = mbox.open();
            
            if (result == SWT.YES) {
                layer.getMap().setExtent( op.result );
            }
        }
        catch (Exception e) {
            log.error( e.getMessage(), e );
            MessageBox mbox = new MessageBox( 
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL );
            mbox.setMessage( "Fehler: " + e.toString() );
            mbox.setText( "Fehler bei der Operation." );
            mbox.open();
        }

//        try {
//            CoordinateReferenceSystem crs = layer.getMap().getCRS();
//            SetLayerBoundsOperation op = new SetLayerBoundsOperation( layer, crs );
//            op.execute( new NullProgressMonitor(), null );
//            
//            System.out.println( "RESULT: " + op.result );
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException( e.getMessage(), e );
//        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        if (sel instanceof StructuredSelection) {
            Object elm = ((StructuredSelection)sel).getFirstElement();
            if (elm instanceof ILayer) {
                layer = (ILayer)elm;
                action.setEnabled( true );
                return;
            }
        }
        layer = null;
        action.setEnabled( false );
    }

}
