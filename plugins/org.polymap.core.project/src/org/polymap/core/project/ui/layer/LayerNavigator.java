/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, All rights reserved.
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
package org.polymap.core.project.ui.layer;

import java.util.Arrays;

import net.refractions.udig.internal.ui.IDropTargetProvider;
import net.refractions.udig.internal.ui.UDIGViewerDropAdapter;
import net.refractions.udig.ui.IDropAction;
import net.refractions.udig.ui.IDropHandlerListener;
import net.refractions.udig.ui.UDIGDragDropUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.navigator.CommonNavigator;

import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.ui.PartListenerAdapter;
import org.polymap.core.ui.SelectionAdapter;

/**
 * Spreading the Rhei while listening to Charlotte McKinnon... :) 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class LayerNavigator
        extends CommonNavigator 
        implements IDropTargetProvider, IModelChangeListener {

    private Log log = LogFactory.getLog( LayerNavigator.class );

    public static final String ID = "org.polymap.core.project.LayerNavigator";

    /** The map that is currently displayed. */
    private IMap                        map;
    
    private IWorkbenchPage              page;

    private PartListener                partListener;

    private UDIGViewerDropAdapter       dropAdapter;

    private LayerStatusLineAdapter      statusLineAdapter;


    public void init( IViewSite _site, final IMemento _memento )
    throws PartInitException {
        super.init( _site, _memento );

//        // restore state
//        if (memento != null) {
//            final String mapId = memento.getString( "mapId" );
//            if (mapId != null) {
//                // set input *after* createPartControl(); give (geo) resources time to setup
//                Polymap.getSessionDisplay().asyncExec( new Runnable() {
//                    public void run() {
//                        try {
//                            // set map input
//                            ProjectRepository repo = ProjectRepository.instance();
//                            IMap _map = repo.findEntity( IMap.class, mapId );
//                            if (_map != null) {
//                                setInputMap( _map );
//                            }
//                        }
//                        catch (NoSuchEntityException e) {
//                            log.warn( "Map does no longer exists: " + mapId );
//                        }
//                        catch (Exception e) {
//                            log.warn( "Unable to restore view.", e );
//                        }
//                    }
//                });
//            }
//        }
    }


    public void saveState( IMemento _memento ) {
        super.saveState( _memento );
        _memento.putString( "mapId", map != null ? map.id() : "" );
    }


    public void createPartControl( Composite parent ) {
        super.createPartControl( parent );
        
//        getCommonViewer().getTree().addListener( SWT.MouseUp, new Listener() {
//            public void handleEvent( Event ev ) {
//                log.info( "Mouse up, at: " + ev.x + ", " + ev.y );
//            }
//        });

        getSite().setSelectionProvider( getCommonViewer() );

        // DnD support
        UDIGDragDropUtilities.addDragSupport( getCommonViewer().getControl(), getCommonViewer() );
        dropAdapter = (UDIGViewerDropAdapter)UDIGDragDropUtilities.addDropSupport( getCommonViewer(), this, true, true );
        dropAdapter.getDropHandler().addListener( new IDropHandlerListener() {
            public void noAction( Object data ) {
                log.info( "DnD: no action ..." );
                getViewSite().getActionBars().getStatusLineManager().setMessage( "Kein passendes Ziel." );
                getViewSite().getActionBars().getStatusLineManager().setErrorMessage( "Kein passendes Ziel." );
            }
            public void starting( IDropAction action ) {
            }
            public void done( IDropAction action, Throwable error ) {
            }
        });

//        // selection listener
//        getSite().getPage().addSelectionListener( new ISelectionListener() {
//            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
//                if (!sel.isEmpty() && sel instanceof StructuredSelection) {
//                    Object elm = ((StructuredSelection)sel).getFirstElement();
//                    log.debug( "page selection: elm= " + elm );
//                    if (elm instanceof IMap) {
//                        setInputMap( (IMap)elm );
//                    }
//                }
//            }
//        });

        // statusLineAdapter
        statusLineAdapter = new LayerStatusLineAdapter( this );
        getCommonViewer().addSelectionChangedListener( statusLineAdapter );

        // part listener
        partListener = new PartListener();
        page = getSite().getPage();
        page.addPartListener( partListener );
    }

    
    public void dispose() {
        super.dispose();
        if (partListener != null) {
            page.removePartListener( partListener );
            page = null;
            partListener = null;
        }
        if (this.map != null) {
            ProjectRepository.instance().removeEntityListener( this );
        }
    }

    
    @Override
    protected void handleDoubleClick( DoubleClickEvent ev ) {
        log.info( "Double clicked: " + ev );
        String[] menuIds = ((ViewSite)getSite()).getContextMenuIds();
        log.info( "Context menus: " + Arrays.asList( menuIds ) );
        
        Object selected = new SelectionAdapter( ev.getSelection() ).first();
        if (selected instanceof ILayer) {
            ILayer layer = (ILayer)selected;
            layer.setVisible( !layer.isVisible() );
        }
        else {
            super.handleDoubleClick( ev );
        }
    }


    @Override
    protected ActionGroup createCommonActionGroup() {
        // no actions at all
        return new ActionGroup() {
        };
    }


    public IMap getInputMap() {
        return (IMap)getCommonViewer().getInput();
    }


    protected void setInputMap( IMap map ) {
        if (map != null && !map.equals( this.map )) {
            // disconnect old map
            if (this.map != null) {
                ProjectRepository.instance().removeEntityListener( this );
            }
            // set input
            this.map = map;
            getCommonViewer().setInput( this.map );
            getCommonViewer().refresh();
            ProjectRepository.instance().addEntityListener( this );
        }
    }

    
    public void modelChanged( ModelChangeEvent ev ) {
        getCommonViewer().setInput( LayerNavigator.this.map );
        getCommonViewer().refresh();
    }

    
    public Object getTarget( DropTargetEvent ev ) {
        log.info( "DnD: ev= " + ev );
        return this;
    }


    /**
     * Listen to part activations. If an editor is activated and its input is
     * an IMap, then load it into the navigator.
     */
    class PartListener
            extends PartListenerAdapter {
        
        public void partActivated( IWorkbenchPart part ) {
            if (part instanceof IEditorPart) {
                IEditorPart editor = (IEditorPart)part;
                IMap newMap = (IMap)editor.getEditorInput().getAdapter( IMap.class );
                if (newMap != null) {
                    setInputMap( newMap );
                }
            }
        }

        public void partDeactivated( IWorkbenchPart part ) {
        }

    }

}
