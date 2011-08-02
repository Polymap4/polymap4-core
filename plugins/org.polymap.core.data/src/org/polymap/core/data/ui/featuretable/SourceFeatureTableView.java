package org.polymap.core.data.ui.featuretable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *  
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class SourceFeatureTableView
        extends ViewPart {

    private static Log log = LogFactory.getLog( SourceFeatureTableView.class );

    public static final String ID = "org.polymap.core.data.SourceFeatureTableView";

    /* Bad but effective way to pass the fs to the view. */
    private static final ThreadLocal<FeatureSource> initFs = new ThreadLocal();
    
    
    /**
     * Makes sure that the view for the layer is open. If the view is already
     * open, then it is activated.
     *
     * @param layer
     * @return The view for the given layer.
     */
    public static SourceFeatureTableView open( final FeatureSource fs ) {
        final SourceFeatureTableView[] result = new SourceFeatureTableView[1];

        Polymap.getSessionDisplay().syncExec( new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                    initFs.set( fs );
                    result[0] = (SourceFeatureTableView)page.showView(
                            SourceFeatureTableView.ID, 
                            fs.getSchema().getName().getLocalPart(), 
                            IWorkbenchPage.VIEW_ACTIVATE );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, null, e.getMessage(), e );
                }
                finally {
                    initFs.remove();
                }
            }
        });
        return result[0];
    }

    
    // instance *******************************************
    
    private Composite               parent;
    
    private FeatureTableViewer      viewer;

    private FeatureSource           fs;

    private String                  basePartName;
    

    public SourceFeatureTableView() {
    }


    public void createPartControl( final Composite _parent ) {
        this.parent = _parent;
        this.fs = initFs.get();
        this.basePartName = fs.getSchema().getName().getLocalPart(); 
        setPartName( basePartName );

        this.parent.setLayout( new FormLayout() );

        viewer = new FeatureTableViewer( parent, SWT.NONE );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().create() );

        viewer.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                if (ev.getPropertyName().equals( FeatureTableViewer.PROP_CONTENT_SIZE )) {
                    Integer count = (Integer)ev.getNewValue();
                    setPartName( basePartName + " (" + count + ")" );
                }
            }
        });
        // columns
        assert fs != null : "fs not set. Call init() first.";
        SimpleFeatureType schema = (SimpleFeatureType)fs.getSchema();
        for (PropertyDescriptor prop : schema.getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                viewer.addColumn( new DefaultFeatureTableColumn( prop ) );
            }
        }

        // load table
        viewer.setContent( (PipelineFeatureSource)fs, Filter.INCLUDE );

        viewer.getTable().pack( true );

        getSite().setSelectionProvider( viewer );
        
        makeActions();
        hookContextMenu();
        
        // contributeToActionBars
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown( bars.getMenuManager() );
        fillLocalToolBar( bars.getToolBarManager() );
    }


    public void dispose() {
        if (viewer != null) {
            viewer.dispose();
            viewer = null;
        }
        fs = null;
    }

    
    private void hookContextMenu() {
//        final MenuManager contextMenu = new MenuManager( "#PopupMenu" );
//        contextMenu.setRemoveAllWhenShown( true );
//        
//        contextMenu.addMenuListener( new IMenuListener() {
//            public void menuAboutToShow( IMenuManager manager ) {
//                manager.add( newWizardAction );
//                
//                // Other plug-ins can contribute there actions here
//                manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
//                manager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
//                manager.add( new Separator() );
//
//                TreeSelection sel = (TreeSelection)viewer.getSelection();
//                if (!sel.isEmpty() && sel.getFirstElement() instanceof IMap) {
//                    SourceFeatureTableView.this.fillContextMenu( manager );
//                }
//            }
//        } );
//        Menu menu = contextMenu.createContextMenu( viewer.getControl() );
//        viewer.getControl().setMenu( menu );
//        getSite().registerContextMenu( contextMenu, viewer );
    }


    private void fillLocalPullDown( IMenuManager manager ) {
//        manager.add( renameAction );
//        manager.add( openMapAction );
    }


    private void fillContextMenu( IMenuManager manager ) {
//        manager.add( renameAction );
//        manager.add( openMapAction );
//        manager.add( new Separator() );
//        drillDownAdapter.addNavigationActions( manager );
    }


    private void fillLocalToolBar( IToolBarManager manager ) {
        manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
        manager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        manager.add( new Separator() );
    }


    private void makeActions() {
    }


    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        if (viewer != null) {
            viewer.getControl().setFocus();
        }
    }


    class NameSorter
            extends ViewerSorter {
    }

}