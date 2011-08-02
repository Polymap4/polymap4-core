package org.polymap.core.data.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import net.refractions.udig.ui.FeatureTableControl;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.ui.featureselection.GeoSelectionView;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *  
 * @deprecated Use {@link GeoSelectionView} instead.
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FeatureTableView
        extends ViewPart {

    private static Log log = LogFactory.getLog( FeatureTableView.class );

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.polymap.core.data.ui.FeatureTableView";

    private Composite               parent;
    
    private FeatureTableControl     viewer;

    private ILayer                  layer;


    /**
     * The constructor.
     */
    public FeatureTableView() {
    }


    public void setLayer( ILayer layer ) {
        this.layer = layer;
        loadTable();
        setPartName( getPartName() + layer.getLabel() );
    }


    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl( final Composite _parent ) {
        this.parent = _parent;
        
        if (layer != null) {
            loadTable();
        }
        
//        getSite().getPage().addSelectionListener( new ISelectionListener() {
//            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
//                if (!sel.isEmpty() && sel instanceof StructuredSelection) {
//                    final Object elm = ((StructuredSelection)sel).getFirstElement();
//                    //log.debug( "page selection: elm= " + elm );
//                    if (elm instanceof ILayer) {
//                        
//                        parent.getDisplay().asyncExec( new Runnable() {
//                            public void run() {
//                                loadTable( (ILayer)elm );
//                            }
//                        });
//                    }
//                }
//                else {
//                    layer = null;
//                }
//            }
//        });
        
//        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
//            public void selectionChanged( SelectionChangedEvent ev ) {
//                System.out.println( "selection: " + ev.getSelection() );
//            }
//        });
        
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
        layer = null;
    }

    
    private void loadTable() {
        try {
            // FIXME do blocking operation inside a job
            FeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
            if (fs != null) {
                log.debug( "            FeatureSource: " + fs.getName() );

                FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
//                            GeometryDescriptor geomDesc = fs.getSchema().getGeometryDescriptor();
//                            String geometryAttributeName = geomDesc.getLocalName();
//                            log.debug( "### geom attr: " + geometryAttributeName );
//                
//                            // filter to select features that intersect with the bounding box
//                            Filter filter = ff.bbox( ff.property( geometryAttributeName ), bbox);
                DefaultQuery query = new DefaultQuery( fs.getSchema().getName().getLocalPart() );
                query.setMaxFeatures( 500 );
                query.setFilter( Filter.INCLUDE );

                final FeatureCollection fc = fs.getFeatures( query );
//                log.debug( "               fc size: " + fc.size() );

                if (viewer != null) {
                    viewer.dispose();
                    viewer = null;
                }
                viewer = new FeatureTableControl( parent, fc );
                viewer.getControl().pack();
            }
        }
        catch (Exception e) {
            log.warn( "unhandled: ", e );
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, Messages.get( "FeatureTableView_loadError" ), e );
        }
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
//                    FeatureTableView.this.fillContextMenu( manager );
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