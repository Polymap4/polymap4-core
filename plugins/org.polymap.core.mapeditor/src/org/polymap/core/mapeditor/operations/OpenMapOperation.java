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

package org.polymap.core.mapeditor.operations;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.ui.OffThreadProgressMonitor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorInput;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.operation.JobMonitors;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.LayerStatus;

/**
 * Put the given layer in edit mode.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class OpenMapOperation
        extends AbstractOperation
        implements IUndoableOperation {
    
    private static Log log = LogFactory.getLog( OpenMapOperation.class );

    private IMap                map;
    
    private IWorkbenchPage      page;
    
    private MapEditor           openedEditor;
    
    private Exception           resultException;

    /**
     * 
     */
    public OpenMapOperation( IMap map, IWorkbenchPage page ) {
        super( Messages.get( "OpenMapOperation_titlePrefix" ) + map.getLabel() ); //$NON-NLS-1$
        this.map = map;
        this.page = page;
    }


    public IStatus execute( final IProgressMonitor _monitor, IAdaptable _info )
            throws ExecutionException {
        log.debug( "..." ); //$NON-NLS-1$
        Display display = page.getWorkbenchWindow().getShell().getDisplay();
        final OffThreadProgressMonitor monitor = new OffThreadProgressMonitor( _monitor, display );
        JobMonitors.set( monitor );
        monitor.subTask( getLabel() );
        
        resultException = null;
        
        // set map extent
        try {
            monitor.subTask( Messages.get( "OpenMapOperation_calcLayersBounds" ) ); //$NON-NLS-1$
            final ReferencedEnvelope bbox = map.getMaxExtent() == null
                ? calcLayersBounds( map.getLayers(), map.getCRS(), monitor )
                : map.getMaxExtent();
                
            if (map.getMaxExtent() == null && bbox != null) {
                log.info( "### No map max extent -> using calculated values." );
                map.setMaxExtent( bbox );
            }
            display.syncExec( new Runnable() {
                public void run() {
                    if (bbox == null && !map.getLayers().isEmpty()) {
                        MessageBox box = new MessageBox( page.getWorkbenchWindow().getShell() );
                        box.setText( Messages.get( "OpenMapOperation_bboxErrorText" ) ); //$NON-NLS-1$
                        box.setMessage( Messages.get( "OpenMapOperation_bboxErrorMsg" ) ); //$NON-NLS-1$
                        box.open();
                    }
                }
            });
        }
        catch (Exception e) {
            resultException = e;
        }

        // open editor
        display.syncExec( new Runnable() {
            public void run() {
                try {
                    monitor.subTask( getLabel() );
                    MapEditorInput input = new MapEditorInput( map );
                    openMap( input, page, monitor );
                    monitor.worked( 1 );
                }
                catch (PartInitException e) {
                    resultException = e;
                    //throw new ExecutionException( e.getMessage(), e );
                }
            }
        });
        JobMonitors.remove();
        // result
        return resultException == null
                ? Status.OK_STATUS
                : new Status( Status.ERROR, MapEditorPlugin.PLUGIN_ID, resultException.getMessage(), resultException );
    }


    public boolean canUndo() {
        return false;
    }

    
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        throw new RuntimeException( "not yet implemented." ); //$NON-NLS-1$
    }

    
    public boolean canRedo() {
        return false;
    }

    
    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." ); //$NON-NLS-1$
    }


    private static void openMap( final MapEditorInput input, IWorkbenchPage page, IProgressMonitor monitor )
            throws PartInitException {
        log.debug( "        new editor: map= " + ((MapEditorInput)input).getMap().id() ); //$NON-NLS-1$

        // check current editors
        IEditorReference[] editors = page.getEditorReferences();
        for (IEditorReference reference : editors) {
            IEditorInput cursor = reference.getEditorInput();
            if (cursor instanceof MapEditorInput) {
                log.debug( "        editor: map= " + ((MapEditorInput)cursor).getMap().id() ); //$NON-NLS-1$
            }
            if (cursor.equals( input )) {
                Object previous = page.getActiveEditor();
                page.activate( reference.getPart( true ) );
                return;
            }
        }

        // not found -> open new editor
        IEditorPart part = page.openEditor( input, input.getEditorId(), true,
                IWorkbenchPage.MATCH_NONE );
        log.debug( "editor= " + part ); //$NON-NLS-1$

        // ProjectExplorer explorer = ProjectExplorer.getProjectExplorer();
        // explorer.setSelection( Collections.singleton(
        // input.getProjectElement() ), true );

        // if (part instanceof MapEditor) {
        // MapEditor mapEditor = (MapEditor)part;
        // while (!mapEditor.getComposite().isVisible()
        // || !mapEditor.getComposite().isEnabled()) {
        // if (!Display.getCurrent().readAndDispatch()) {
        // Thread.sleep( 300 );
        // }
        // }
        // }
    }


    private ReferencedEnvelope calcLayersBounds( Collection<ILayer> layers, CoordinateReferenceSystem crs,
            IProgressMonitor monitor )
            throws Exception {
        log.debug( "### mapCRS: " + crs ); //$NON-NLS-1$

        ReferencedEnvelope result = null; // new ReferencedEnvelope( crs );
        for (ILayer layer : layers) {
            try {
                IGeoResource res = layer.getGeoResource();
                if (res == null) {
                    continue;
                }
                ReferencedEnvelope bbox = SetLayerBoundsOperation
                        .obtainBoundsFromResources( layer, crs, monitor );
                bbox = bbox.transform( crs, true );
                log.debug( "layer: " + layer + ", bbox= " + bbox ); //$NON-NLS-1$ //$NON-NLS-2$

                if (result == null) {
                    result = bbox;
                } else {
                    result.expandToInclude( bbox );
                }
                log.debug( "result: bbox= " + result ); //$NON-NLS-1$
            }
            catch (Exception e) {
                // XXX mark layers!?
                log.debug( "", e ); //$NON-NLS-1$
                log.warn( "skipping layer: " + layer.getLabel() + " (" + e.toString() ); //$NON-NLS-1$ //$NON-NLS-2$
                layer.setLayerStatus( new LayerStatus( Status.WARNING, LayerStatus.UNSPECIFIED, 
                        Messages.get( "LayerStatus_noCrs" ), e ) ); //$NON-NLS-1$
            }
        }
        return result;
    }

}
