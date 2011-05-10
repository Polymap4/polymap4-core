package org.polymap.core.project.ui;

import net.refractions.udig.internal.ui.IDropTargetProvider;
import net.refractions.udig.ui.UDIGDragDropUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.commands.ExecutionException;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.model.operations.SetPropertyOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * The maps tree view. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ProjectView
        extends ViewPart
        implements IDropTargetProvider {

    private static Log log = LogFactory.getLog( ProjectView.class );

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.polymap.core.project.ProjectView";

    private ProjectTreeViewer  viewer;

    private DrillDownAdapter   drillDownAdapter;
    
    private LayerStatusLineAdapter  statusLineAdapter;
    
    private IMap               root;
    
    private Action             renameAction;

    private Action             openMapAction;
    
    private IAction            newWizardAction;
    
    private Action             collapsAllAction = new CollapseAllAction();

    private Action             doubleClickAction;

    private IMap               selectedMap;

    private IPartListener      partListener;
    

    /**
     * The constructor.
     */
    public ProjectView() {
    }


    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl( Composite parent ) {
        viewer = new ProjectTreeViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        getSite().setSelectionProvider( viewer );        
        UDIGDragDropUtilities.addDragDropSupport( viewer, this );

        root = ProjectRepository.instance().getRootMap();
        viewer.setRootMap( root );

        // XXX the drilldownadapter needs to get informed of real input changes 
        drillDownAdapter = new DrillDownAdapter( viewer );
                
        getSite().getPage().addSelectionListener( new ISelectionListener() {
            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
                if (!sel.isEmpty() && sel instanceof StructuredSelection) {
                    Object elm = ((StructuredSelection)sel).getFirstElement();
                    //log.debug( "page selection: elm= " + elm );
                    if (elm instanceof IMap) {
                        selectedMap = (IMap)elm;
                    }
                }
                else {
                    selectedMap = null;
                }
            }
        });
        
        // part listener
        partListener = new DefaultPartListener() {
            public void partActivated( IWorkbenchPart part ) {
                log.debug( "part= " + part );
                if (part instanceof IEditorPart) {
                    IEditorPart editor = (IEditorPart)part;
                    IMap map = (IMap)editor.getEditorInput().getAdapter( IMap.class );
                    if (map != null) {
                        viewer.setSelection( new StructuredSelection( map ), true );
                    }
                }
            }
        };
        getSite().getPage().addPartListener( partListener );
        
        // statusLineAdapter
        statusLineAdapter = new LayerStatusLineAdapter( this );
        viewer.addSelectionChangedListener( statusLineAdapter );
        
        makeActions();
        hookContextMenu();
        
        // hookDoubleClickAction
        viewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent event ) {
                doubleClickAction.run();
            }
        });

        // contributeToActionBars
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown( bars.getMenuManager() );
        fillLocalToolBar( bars.getToolBarManager() );
    }


    public void dispose() {
        if (viewer != null && statusLineAdapter != null) {
            viewer.removeSelectionChangedListener( statusLineAdapter );
            statusLineAdapter = null;
        }
        if (partListener != null) {
            getSite().getPage().removePartListener( partListener );
            partListener = null;
        }
    }


    private void hookContextMenu() {
        final MenuManager contextMenu = new MenuManager( "#PopupMenu" );
        contextMenu.setRemoveAllWhenShown( true );
        
        contextMenu.addMenuListener( new IMenuListener() {
            public void menuAboutToShow( IMenuManager manager ) {
                manager.add( newWizardAction );
                
                // Other plug-ins can contribute there actions here
                manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
                manager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
                manager.add( new Separator() );

                TreeSelection sel = (TreeSelection)viewer.getSelection();
                if (!sel.isEmpty() && sel.getFirstElement() instanceof IMap) {
                    ProjectView.this.fillContextMenu( manager );
                }
            }
        } );
        Menu menu = contextMenu.createContextMenu( viewer.getControl() );
        viewer.getControl().setMenu( menu );
        getSite().registerContextMenu( contextMenu, viewer );
    }


    private void fillLocalPullDown( IMenuManager manager ) {
//        manager.add( renameAction );
//        manager.add( openMapAction );
    }


    private void fillContextMenu( IMenuManager manager ) {
        manager.add( renameAction );
        //manager.add( openMapAction );
        manager.add( new Separator() );
        drillDownAdapter.addNavigationActions( manager );
    }


    private void fillLocalToolBar( IToolBarManager manager ) {
        manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
        manager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        manager.add( new Separator() );

        System.out.println( "TOOLBAR Items: " + manager.getItems() );
//        manager.add( renameAction );
//        manager.add( openMapAction );
//        manager.add( new Separator() );
//        manager.add( collapsAllAction );
        drillDownAdapter.addNavigationActions( manager );
    }


    private void makeActions() {
        IWorkbenchWindow window = getSite().getWorkbenchWindow();
        newWizardAction = ActionFactory.NEW_WIZARD_DROP_DOWN.create( window );
        newWizardAction.setText( "Neu" );

        renameAction = new Action() {
            public void run() {
                TreeSelection sel = (TreeSelection)viewer.getSelection();
                log.debug( "sel= " + sel.getFirstElement() );
                if (!sel.isEmpty() && sel.getFirstElement() instanceof IMap) {
                    IMap map = (IMap)sel.getFirstElement();
                    Shell shell = ProjectView.this.getSite().getWorkbenchWindow().getShell();
                    InputDialog dialog = new InputDialog( shell, Messages.get( "ProjectView_title" ), 
                            Messages.get( "ProjectView_fieldName" ), map.getLabel(), null );
                    dialog.setBlockOnOpen( true );
                    dialog.open();
                    
                    if (dialog.getReturnCode() == InputDialog.OK) {
                        try {
                            SetPropertyOperation op = ProjectRepository.instance().newOperation( SetPropertyOperation.class );
                            op.init( IMap.class, map, IMap.PROP_LABEL, dialog.getValue() );
                            OperationSupport.instance().execute( op, false, false );
                        }
                        catch (ExecutionException e) {
                            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, "", e );
                        }
                    }                    
                    viewer.refresh();
                }
            }
        };
        renameAction.setText( Messages.get( "actions_renameMap" ) );
        renameAction.setToolTipText( Messages.get( "actions_renameMapTip" ) );
//        renameAction.setImageDescriptor( ProjectPlugin.getImageDescriptor( "icons/etool16/newprj_wiz.gif" ) );

        openMapAction = new Action() {
            public void run() {
                try {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPage page = window.getActivePage();
                    
                    IHandlerService service = (IHandlerService)getSite().getService( IHandlerService.class );
                    service.executeCommand( "org.polymap.core.project.command.openMap", null );
                    
                    //OpenMapHandler.openMap( new MapEditorInput( selectedMap ), page );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        openMapAction.setText( "Karte öffnen (action)" );
        openMapAction.setToolTipText( "Karte öffnen" );
        openMapAction.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJ_ADD ) );
        
        doubleClickAction = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection)selection).getFirstElement();
                //showMessage( "Double-click detected on " + obj.toString() );
                if (obj instanceof IMap) {
                    openMapAction.run();
                }
            }
        };
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

    
    /**
     * Collaps the viewer tree.
     */
    class CollapseAllAction
            extends Action {

        public String getText() {
            return "Baum schließen";
        }

        public String getDescription() {
            return "Baum ganz schließen";
        }

        public String getToolTipText() {
            return "Baum ganz schließen";
        }

        public ImageDescriptor getDisabledImageDescriptor() {
            ISharedImages images = getViewSite().getWorkbenchWindow().getWorkbench().getSharedImages();
            return images.getImageDescriptor( ISharedImages.IMG_ELCL_COLLAPSEALL_DISABLED );
        }

        public ImageDescriptor getImageDescriptor() {
            ISharedImages images = getViewSite().getWorkbenchWindow().getWorkbench().getSharedImages();
            return images.getImageDescriptor( ISharedImages.IMG_ELCL_COLLAPSEALL );
        }

        public void run() {
            viewer.collapseAll();
        }
        
    }
    

    public Object getTarget( DropTargetEvent ev ) {
        log.info( "DnD: ev= " + ev );
        return this;
    }

}