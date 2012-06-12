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
 * $Id: $
 */
package org.polymap.core.project.ui.layer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.refractions.udig.internal.ui.IDropTargetProvider;
import net.refractions.udig.internal.ui.UDIGViewerDropAdapter;
import net.refractions.udig.ui.IDropAction;
import net.refractions.udig.ui.IDropHandlerListener;
import net.refractions.udig.ui.UDIGDragDropUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;

import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.event.SourceClassPropertyEventFilter;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.ui.PartListenerAdapter;
import org.polymap.core.project.ui.LabeledLabelProvider;
import org.polymap.core.project.ui.LayerStatusLineAdapter;

/**
 * 
 * @deprecated See new {@link LayerNavigator}.
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class MapLayersView
        extends ViewPart
        implements IDropTargetProvider {

    private static Log log = LogFactory.getLog( MapLayersView.class );

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.polymap.core.project.ui.MapLayersView";

    private CheckboxTreeViewer      viewer;

    private ILabelDecorator         decorator;

    private DrillDownAdapter        drillDownAdapter;
    
    private LayerStatusLineAdapter  statusLineAdapter;
    
    private IMap                    root;
    
    private Action                  action2;
    
    private Action                  propertiesAction;

    private Action                  doubleClickAction;

    private IModelChangeListener     modelListener;

    private LayersCheckStateListener checkStateListener;
    
    private IPartListener           partListener;

    private UDIGViewerDropAdapter   dropAdapter;
    
    
    /**
     * The constructor.
     */
    public MapLayersView() {
    }


    public void firePropertyChanged( int propertyId ) {
        super.firePropertyChange( propertyId );
    }


    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl( Composite parent ) {
        viewer = new CheckboxTreeViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        getSite().setSelectionProvider( viewer );
        
        // DnD support
        UDIGDragDropUtilities.addDragSupport( viewer.getControl(), viewer );
        dropAdapter = (UDIGViewerDropAdapter)UDIGDragDropUtilities.addDropSupport( viewer, this, true, true );
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

        // part listener
        partListener = new PartListenerAdapter() {
            public void partActivated( IWorkbenchPart part ) {
                log.debug( "part= " + part );
                if (part instanceof IEditorPart) {
                    IEditorPart editor = (IEditorPart)part;
                    IMap map = (IMap)editor.getEditorInput().getAdapter( IMap.class );
                    if (map != null) {
                        setRootMap( map );
                    }
                }
            }

            public void partDeactivated( IWorkbenchPart part ) {
                log.debug( "part= " +part );
            }
        };
        getSite().getPage().addPartListener( partListener );
        
        drillDownAdapter = new DrillDownAdapter( viewer );
        
        // ContentProvider
        viewer.setContentProvider( new LayersContentProvider() );
        
        // LabelProvider
        decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
        LayersLabelProvider labelProvider = new LayersLabelProvider();
        viewer.setLabelProvider( new DecoratingLabelProvider( labelProvider, decorator ) );
        viewer.setSorter( new LayersSorter() );
        setRootMap( root );
        
//        getSite().getPage().addSelectionListener( new ISelectionListener() {
//            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
//                if (sel instanceof IStructuredSelection) {
//                    Object elm = ((IStructuredSelection)sel).getFirstElement();
//                    if (elm instanceof IMap) {
//                        log.debug( "page selection: " + elm );
//                        setRootMap( (IMap)elm );
//                    }
//                }
//            }
//        });
        
        // statusLineAdapter
        statusLineAdapter = new LayerStatusLineAdapter( this );
        viewer.addSelectionChangedListener( statusLineAdapter );
        
        // checkStateListener
        checkStateListener = new LayersCheckStateListener();
        viewer.addCheckStateListener( checkStateListener );
        
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        
        // contributeToActionBars
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown( bars.getMenuManager() );
        fillLocalToolBar( bars.getToolBarManager() );
    }


    public void dispose() {
        if (PlatformUI.getWorkbench().isClosing()) {
            return;
        }
        if (root != null && modelListener != null) {
            ProjectRepository.instance().removeModelChangeListener( modelListener );
            modelListener = null;
        }
        root = null;
        if (decorator != null) {
            decorator.dispose();
            decorator = null;
        }
        if (partListener != null) {
            getSite().getPage().removePartListener( partListener );
            partListener = null;
        }
        if (viewer != null && statusLineAdapter != null) {
            viewer.removeSelectionChangedListener( statusLineAdapter );
            statusLineAdapter = null;
        }
//        viewer.setInput( root );
//        viewer.refresh();
        super.dispose();
    }


    /**
     * Returns the {@link IMap} that is currently displayed in this view.
     */
    public IMap getRootMap() {
        return root;    
    }
    
    
    private void setRootMap( IMap map ) {
        if (map != null && !map.equals( root )) {
            if (root != null && modelListener != null) {
                ProjectRepository.instance().removeModelChangeListener( modelListener );
                modelListener = null;
            }
            root = map;
            viewer.setInput( root );
            viewer.refresh();
            
            modelListener = new IModelChangeListener() {
                public void modelChanged( ModelChangeEvent ev ) {
                    log.debug( "ev= " + ev + ", display= " + Display.getCurrent() );
                    viewer.getControl().getDisplay().asyncExec( new Runnable() {
                        public void run() {
                            viewer.setInput( root );
                            viewer.refresh();
                        }
                    });
                }
            };
            ProjectRepository.instance().addModelChangeListener( modelListener,
                    new SourceClassPropertyEventFilter( ILayer.class ));
        }
    }

            
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager( "#PopupMenu" );
        menuMgr.setRemoveAllWhenShown( true );
        menuMgr.addMenuListener( new IMenuListener() {

            public void menuAboutToShow( IMenuManager manager ) {
                TreeSelection sel = (TreeSelection)viewer.getSelection();
                log.debug( "sel= " + sel.getFirstElement() );
                if (!sel.isEmpty() && sel.getFirstElement() instanceof ILayer) {
                    // Other plug-ins can contribute there actions here
                    manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
                    
                    manager.add( new Separator() );
                    drillDownAdapter.addNavigationActions( manager );

                    //manager.add( new Separator() );
                    //manager.add( propertiesAction );
                }
            }
        } );
        Menu menu = menuMgr.createContextMenu( viewer.getControl() );
        viewer.getControl().setMenu( menu );
        getSite().registerContextMenu( menuMgr, viewer );
    }


    private void fillLocalPullDown( IMenuManager manager ) {
//        manager.add( action1 );
//        manager.add( new Separator() );
//        manager.add( action2 );
    }


    private void fillLocalToolBar( IToolBarManager manager ) {
//        manager.add( action1 );
//        manager.add( action2 );
//        manager.add( new Separator() );
//        drillDownAdapter.addNavigationActions( manager );
    }


    private void makeActions() {
        // propertiesAction
        propertiesAction = new PropertyDialogAction( 
                getViewSite().getWorkbenchWindow(), viewer );
        propertiesAction.setText( org.polymap.core.project.Messages.get( "MapLayersView_propertiesDialog" ) );
        getViewSite().getActionBars().setGlobalActionHandler( 
                ActionFactory.PROPERTIES.getId(), 
                propertiesAction );        
        getViewSite().getActionBars().setGlobalActionHandler(
                IWorkbenchActionConstants.PROPERTIES,
                propertiesAction );

        action2 = new Action() {
            public void run() {
                showMessage( "Action 2 executed" );
            }
        };
        action2.setText( "Action 2" );
        action2.setToolTipText( "Action 2 tooltip" );
        action2.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJS_INFO_TSK ) );
    }


    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent event ) {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection)selection).getFirstElement();
                
                // check box
                viewer.setChecked( obj, !viewer.getChecked( obj ) );
                
                // try openMap command
                ICommandService service =
                        (ICommandService)PlatformUI.getWorkbench().getService( ICommandService.class );
                Command command = service.getCommand( "org.polymap.core.project.command.openMap" );
                log.debug( "command: isEnabled= " + command.isEnabled() );
                if (command.isEnabled()) {
                    try {
                        command.executeWithChecks( new ExecutionEvent() );
                    }
                    catch (Exception e) {
                        log.error( "", e );
                    }
                }
            }
        });
    }


    private void showMessage( String message ) {
        MessageDialog.openInformation( viewer.getControl().getShell(), "Project View", message );
    }


    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    
    public Object getTarget( DropTargetEvent ev ) {
        log.info( "DnD: ev= " + ev );
        return this;
    }


    /**
     * The ContentProvider of the {@link MapLayersView}. 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    class LayersContentProvider
            implements ITreeContentProvider {

        private IMap                input;
        
        private Map<String,ILayer>  sortedLayers = new TreeMap();
        
        
        public void dispose() {
            sortedLayers = null;
        }

        public void inputChanged( Viewer _viewer, Object oldInput, Object newInput ) {
            log.debug( "newInput= " + newInput );
            input = (IMap)newInput;
            
            sortedLayers.clear();
            if (input == null) {
                return;
            }
            
            LinkedList<IMap> stack = new LinkedList();
            stack.addLast( input );
            while (!stack.isEmpty()) {
                IMap map = stack.removeLast();
                for (ILayer layer : map.getLayers()) {
                    sortedLayers.put( layer.getLabel() + layer.id(), layer );
                };
                for (IMap child : map.getMaps() ) {
                    stack.add( child );
                }
            }
        }
        
        public boolean hasChildren( Object elm ) {
            return elm instanceof IMap;
        }

        public Object[] getChildren( Object parent ) {
            if (parent == root) {
                // async set checked state
                viewer.getControl().getDisplay().asyncExec( new Runnable() {
                    public void run() {
                        for (ILayer layer : sortedLayers.values()) {
                            viewer.setChecked( layer, layer.isVisible() );
                        }
                    }
                });
                // return values;
                return sortedLayers.values().toArray();
            }
            else {
                log.debug( "No children for parent: " + parent );
                return new Object[] {};
            }
        }

        public Object getParent( Object element ) {
            return input;
        }

        public Object[] getElements( Object _input ) {
            return getChildren( _input );
        }

    }
        
    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    class LayersCheckStateListener
            implements ICheckStateListener {

        public void checkStateChanged( CheckStateChangedEvent ev ) {
            log.debug( "ev= " + ev );
            ILayer layer = (ILayer)ev.getElement();
            // XXX inside operation!
            layer.setVisible( ev.getChecked() );
        }
        
    }
            
    
    /**
     * 
     */
    class LayersLabelProvider
            extends LabeledLabelProvider {

        public Image getImage( Object elm ) {
            String imageKey = (elm instanceof IMap) ? ISharedImages.IMG_OBJ_FOLDER : ISharedImages.IMG_OBJ_ELEMENT;
            return PlatformUI.getWorkbench().getSharedImages().getImage( imageKey );
        }
    }
    
    
    /**
     * Sort layers after {@link ILayer#getOrderKey()}.
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    class LayersSorter
            extends ViewerSorter {

        public void sort( Viewer _viewer, Object[] elements ) {
            try {
                Arrays.sort( elements, new Comparator() {
                    public int compare( Object o1, Object o2 ) {
                        ILayer l1 = (ILayer)o1;
                        ILayer l2 = (ILayer)o2;
                        return l2.getOrderKey() - l1.getOrderKey();
                    }
                });
            }
            catch (Exception e) {
                log.warn( "Exception while sorting:", e );
            }
        }
        
    }

}