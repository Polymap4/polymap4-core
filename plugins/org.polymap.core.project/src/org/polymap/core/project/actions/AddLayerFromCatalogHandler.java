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

package org.polymap.core.project.actions;

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
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.model.operations.NewLayerOperation;

/**
 * This is handler for the <em>....</em> command.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class AddLayerFromCatalogHandler
        extends AbstractHandler
        implements IHandler, IHandler2 {  //, IUndoableOperation {

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

        NewLayerOperation op = ProjectRepository.instance().newOperation( NewLayerOperation.class ); 
        op.init( ProjectPlugin.getSelectedMap(), selectedGeoRes ); 
        IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindow( ev ).getWorkbench();
        
        OperationSupport.instance().execute( op, true, true );
        
//        IOperationHistory operationHistory = workbench.getOperationSupport().getOperationHistory();
//        IUndoContext undoContext = workbench.getOperationSupport().getUndoContext();
//        operation.addContext( undoContext );
//        operationHistory.execute( operation, null, null );
        
        
//        // FIXME using commands instead of setters
//        ILayer layer = (ILayer)map.getDomain().createObject( ILayer.class );
//        layer.setLabel( selectedGeoRes.getTitle() );
//        layer.setGeoResource( selectedGeoRes );
//        
//        map.addLayer( layer );
        return null;
    }


    public boolean isEnabled() {
        log.debug( "currentMap= " + ProjectPlugin.getSelectedMap() );
        boolean isEnabled = ProjectPlugin.getSelectedMap() != null;
        enableListener( isEnabled );
        return isEnabled;
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


    public boolean isHandled() {
        return true;
    }


//    public static void openMap( final MapEditorInput input, IWorkbenchPage page ) 
//            throws PartInitException {
//        log.debug( "        new editor: map= " + ((MapEditorInput)input).getMap().getId() );
//
//        // check current editors
//        IEditorReference[] editors = page.getEditorReferences();
//        for (IEditorReference reference : editors) {
//            IEditorInput cursor = reference.getEditorInput();
//            if (cursor instanceof MapEditorInput) {
//                log.debug( "        editor: map= " + ((MapEditorInput)cursor).getMap().getId() );
//            }
//            if (cursor.equals( input )) {
//                Object previous = page.getActiveEditor();
//                page.activate( reference.getPart( true ) );
//                return;
//            }
//        }
//
//        // not found -> open new editor
//        IEditorPart part = page.openEditor( input, input.getEditorId(), true, IWorkbenchPage.MATCH_NONE );
//        log.debug( "editor= " + part );
//
//        //            ProjectExplorer explorer = ProjectExplorer.getProjectExplorer();
//        //            explorer.setSelection( Collections.singleton( input.getProjectElement() ), true );
//
//        //            if (part instanceof MapEditor) {
//        //                MapEditor mapEditor = (MapEditor)part;
//        //                while (!mapEditor.getComposite().isVisible()
//        //                        || !mapEditor.getComposite().isEnabled()) {
//        //                    if (!Display.getCurrent().readAndDispatch()) {
//        //                        Thread.sleep( 300 );
//        //                    }
//        //                }
//        //            }
//    }

}
