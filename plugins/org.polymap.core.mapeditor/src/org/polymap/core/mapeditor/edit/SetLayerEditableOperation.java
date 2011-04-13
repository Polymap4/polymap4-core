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

package org.polymap.core.mapeditor.edit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.ui.OffThreadProgressMonitor;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.ui.featureTable.GeoSelectionView;
import org.polymap.core.mapeditor.IEditFeatureSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorInput;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.operation.JobMonitors;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * This operation is triggered by {@link SetLayerEditableAction} it puts the
 * given layer in "features editable" mode.
 * <p>
 * This operation changes the layer state, which inturn triggers domain
 * listeners. Besides it adapts the UI by adding {@link EditFeatureSupport} to
 * the {@link MapEditor}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class SetLayerEditableOperation
        extends AbstractOperation
        implements IUndoableOperation {
    
    private static Log log = LogFactory.getLog( SetLayerEditableOperation.class );

    private ILayer                      layer;
    
    private boolean                     editable;
    

    /**
     * 
     */
    public SetLayerEditableOperation( ILayer layer, boolean editable ) {
        super( Messages.get( "SetLayerEditableOperation_labelPrefix" ) + layer.getLabel() );
        this.layer = layer;
        this.editable = editable;
    }


    public IStatus execute( IProgressMonitor _monitor, IAdaptable info )
            throws ExecutionException {
        try {
            Display display = Polymap.getSessionDisplay();
            log.debug( "### Display: " + display );
            OffThreadProgressMonitor monitor = new OffThreadProgressMonitor( _monitor );
            JobMonitors.set( monitor );
            monitor.subTask( getLabel() );
            
            // do all work in the domain listeners
            layer.setEditable( editable );
            monitor.worked( 1 );
            
            //
            display.asyncExec( new Runnable() {
                public void run() {
                    try {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        // FIXME search for the associated editor vie EditorInput
                        MapEditor mapEditor = (MapEditor)page.getActiveEditor();
                        if (!mapEditor.getEditorInput().equals( new MapEditorInput( layer.getMap() ) )) {
                            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "Active editor is not associated with this map.", new Exception() );
                        }
                        
                        if (editable) {
                            GeoSelectionView view = (GeoSelectionView)page.showView( GeoSelectionView.ID );
                            view.setAllowModify( true );
                            view.setAllowSearch( false );
                            view.connectLayer( layer );

                            EditFeatureSupport support = (EditFeatureSupport)mapEditor.findSupport( IEditFeatureSupport.class );
                            assert support == null;
                            support = new EditFeatureSupport( mapEditor, layer );
                            mapEditor.addSupport( support );
                            mapEditor.activateSupport( support, true );
                        }
                        else {
                            GeoSelectionView view = (GeoSelectionView)page.findView( GeoSelectionView.ID );
                            if (view != null) {
                                page.hideView( view );
                                view.disconnectLayer();
                            }
                            
                            EditFeatureSupport support = (EditFeatureSupport)mapEditor.findSupport( IEditFeatureSupport.class );
                            if (support != null) {
                                mapEditor.removeSupport( support );
                                support.dispose();
                            }
                        }
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getMessage(), e );
                    }
                }
            });

        }
        catch (Exception e) {
            throw new ExecutionException( "Failure...", e );
        }
        finally {
            JobMonitors.remove();
        }
        return Status.OK_STATUS;
    }


    public boolean canUndo() {
        return false;
    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean canRedo() {
        return false;
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

}
