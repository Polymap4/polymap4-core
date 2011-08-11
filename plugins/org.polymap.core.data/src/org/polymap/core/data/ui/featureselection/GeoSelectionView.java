/*
 * polymap.org
 * Copyright 2009, 2011, Polymap GmbH, and individual contributors
 * as indicated by the @authors tag.
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
package org.polymap.core.data.ui.featureselection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.core.StaticProvider;
import net.refractions.udig.ui.FeatureTableControl;
import net.refractions.udig.ui.PlatformJobs;
import net.refractions.udig.ui.ProgressManager;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsLike;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.ui.PartListenerAdapter;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.WeakListener;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 * @deprecated Use FeatureTableViewer instead.
 */
public class GeoSelectionView
        extends ViewPart {

    private static Log log = LogFactory.getLog( GeoSelectionView.class );

    /**
     * Makes sure that the view for the layer is open. If the view is already
     * open, then it is activated.
     *
     * @param layer
     * @param allowSearch XXX
     * @return The view for the given layer.
     */
    public static GeoSelectionView open( final ILayer layer, final boolean allowSearch ) {
        final GeoSelectionView[] result = new GeoSelectionView[1];

        Polymap.getSessionDisplay().syncExec( new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    ensureMaxViews( page );

                    result[0] = (GeoSelectionView)page.showView(
                            GeoSelectionView.ID, layer.id(), IWorkbenchPage.VIEW_ACTIVATE );
                    result[0].setAllowSearch( allowSearch );
                    result[0].connectLayer( layer );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, null, e.getMessage(), e );
                }
            }
        });
        return result[0];
    }

    public static void close( final ILayer layer ) {
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                    GeoSelectionView view = (GeoSelectionView)page.findView( GeoSelectionView.ID );
                    if (view != null) {
                        page.hideView( view );
                        view.disconnectLayer();
                    }
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getMessage(), e );
                }
            }
        });
    }

    protected static void ensureMaxViews( IWorkbenchPage page ) {
    }


    // instance *******************************************

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String      ID = "org.polymap.core.data.ui.featureselection.view";

    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

    public static final int         DEFAULT_MAX_RESULTS = 10000;

    private Composite               parent;

    private FeatureTableControl     viewer;

    private Text                    searchText;

    private Combo                   attrCombo;

    private Label                   sizeLabel;

    private FormData                dTable;

    private ILayer                  layer;

    private FeatureSource           fs;

    /** The currently displayed features. */
    private FeatureCollection       fc;

    /** The filter that was used to produce the {@link #fc}. */
    private Filter                  filter;

    private ISelectionChangedListener tableSelectionListener = new TableSelectionListener();

    private GeoSelectionListener    geoSelectionListener = new GeoSelectionListener();

    private PartListenerAdapter     partListener = new PartListener();

    private PropertyChangeListener  layerChangeListener = new LayerChangeListener();

    private IWorkbenchPage          page;

    private boolean                 allowModify = false;

    private boolean                 allowSearch = true;

    private String                  basePartName;


    public GeoSelectionView() {
        log.debug( "..." );
    }

    public void setAllowModify( boolean allowModify ) {
        this.allowModify = allowModify;
    }

    public void setAllowSearch( boolean allowSearch ) {
        this.allowSearch = allowSearch;
        if (attrCombo != null) {
            attrCombo.dispose(); attrCombo = null;
            searchText.dispose(); searchText = null;
            sizeLabel.dispose(); sizeLabel = null;
        }
    }

    public ILayer getLayer() {
        return layer;
    }

    public FeatureCollection getFeatureCollection() {
        return fc;
    }

    public FeatureStore getFeatureStore() {
        return (FeatureStore)fs;
    }

    public Filter getFilter() {
        return filter;
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl( final Composite _parent ) {
        this.parent = _parent;

        viewer = new FeatureTableControl( ProgressManager.instance() );
//      Color bg = Graphics.getColor( 255, 200, 0 );
//      viewer.getViewer().getTable().setBackgroundMode( SWT.INHERIT_FORCE );
//      viewer.getViewer().getTable().setBackground( bg );
//        Color bg = Polymap.getSessionDisplay().getSystemColor( SWT.COLOR_LIST_SELECTION );
        viewer.setSelectionColor( new StaticProvider<RGB>( new RGB( 0x50, 0x88, 0xee ) ) );
        viewer.addSelectionChangedListener( tableSelectionListener );
        getSite().setSelectionProvider( viewer );

        if (allowSearch) {
            // attrCombo
            attrCombo = new Combo( parent, SWT.BORDER );
            attrCombo.setToolTipText( Messages.get( "GeoSelectionView_attrComboTip" ) );

            // searchText
            searchText = new Text( parent, SWT.BORDER );
            searchText.setText( Messages.get( "GeoSelectionView_searchTextDefault" ) );
            searchText.setToolTipText( Messages.get( "GeoSelectionView_searchTextTip" ) );
            searchText.setEditable( true );
            searchText.addSelectionListener( new SelectionListener() {
                public void widgetDefaultSelected( SelectionEvent ev ) {
                    log.debug( "widgetDefaultSelected(): ..." );
                    search();
                }
                public void widgetSelected( SelectionEvent ev ) {
                    log.debug( "widgetSelected(): ..." );
                    search();
                }
            } );

            // result size
            sizeLabel = new Label( parent, SWT.NONE );
            sizeLabel.setText( "ausgewählt: 0" );
            sizeLabel.setToolTipText( "Anzahl der ausgewählten Objekte" );
        }

        // layout
        FormLayout layout = new FormLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.spacing = 0;
        parent.setLayout( layout );

        if (allowSearch) {
            FormData dAttr = new FormData(); // bind to left & text
            dAttr.left = new FormAttachment( 0 );
            dAttr.top = new FormAttachment( searchText, 5, SWT.CENTER );
            attrCombo.setLayoutData( dAttr );

            FormData dText = new FormData( 400, SWT.DEFAULT ); // bind to top, label, bbox
            dText.top = new FormAttachment( 1 );
            dText.left = new FormAttachment( attrCombo, 5 );
            //dText.right = new FormAttachment( sizeLabel, 5 );
            searchText.setLayoutData(dText);

            FormData dSize = new FormData(); // size label & right
            dSize.right = new FormAttachment( 100 );
            dSize.left = new FormAttachment( searchText, 5 );
            dSize.top = new FormAttachment( 3 );
            sizeLabel.setLayoutData( dSize );
        }

        dTable = new FormData( 100, 100 ); // text & bottom
        dTable.left = new FormAttachment( 0 ); // bind to left of form
        dTable.right = new FormAttachment( 100 ); // bind to right of form
        dTable.top = new FormAttachment( searchText, 2 ); // attach with 5 pixel offset
        dTable.bottom = new FormAttachment( 100 ); // bind to bottom of form
//        tableComposite.setLayoutData( dTable );

        makeActions();
        hookContextMenu();

        // contributeToActionBars
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown( bars.getMenuManager() );
        fillLocalToolBar( bars.getToolBarManager() );
    }


    public void connectLayer( ILayer _layer ) {
        this.layer = _layer;
        this.basePartName = layer.getLabel(); 
        setPartName( basePartName );

        // property listener
        layer.addPropertyChangeListener( WeakListener.forListener( layerChangeListener ) );

        // part listener
        page = getSite().getWorkbenchWindow().getActivePage();
        page.addPartListener( partListener );

        // geo event listener
        LayerFeatureSelectionManager.forLayer( layer ).addChangeListener( geoSelectionListener );
        
        try {
            // FIXME do blocking operation inside a job
            fs = PipelineFeatureSource.forLayer( layer, false );

            if (attrCombo != null) {
                attrCombo.removeAll();
                for (PropertyDescriptor prop : fs.getSchema().getDescriptors()) {
                    if (!(prop instanceof GeometryDescriptor)) {
                        attrCombo.add( prop.getName().getLocalPart() );
                    }
                }
                attrCombo.add( Messages.get( "GeoSelectionView_all" ), 0 );
                attrCombo.setText( Messages.get( "GeoSelectionView_all" ) );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }

//        if (fs != null) {
//            loadTable( Filter.EXCLUDE );
//        }
    }


    public void disconnectLayer() {
        if (page != null) {
            // domain listener
            ProjectRepository.instance().removePropertyChangeListener( layerChangeListener );

            // part listener
            page.removePartListener( partListener );
            page = null;

            // geo event listener
            LayerFeatureSelectionManager.forLayer( layer ).removeChangeListener( geoSelectionListener );
        }

        layer = null;
        fs = null;
        fc = null;
        filter = null;
    }


    public void dispose() {
        log.debug( "dispose() ..." );
        if (layer != null) {
            layer.removePropertyChangeListener( layerChangeListener );
        }
        disconnectLayer();
        if (viewer != null) {
            viewer.removeSelectionChangedListener( tableSelectionListener );
            viewer.dispose();
            viewer = null;
        }
        super.dispose();
    }


    protected void search() {
        String text = searchText.getText();
        if (text.indexOf( '*' ) <= 0 && text.indexOf( '?' ) <= 0) {
            text = "*" + text + "*";
        }

        Filter _filter = Filter.EXCLUDE;
        // all
        if (attrCombo.getSelectionIndex() == 0) {
            for (PropertyDescriptor prop : fs.getSchema().getDescriptors()) {
                if (!(prop instanceof GeometryDescriptor)) {
                    PropertyIsLike like = ff.like( ff.property( prop.getName().getLocalPart() ), text );
                    _filter = _filter != null ? ff.or( _filter, like ) : like;
                }
            }
        // one property
        } else {
            _filter = ff.like( ff.property( attrCombo.getText() ), text );
        }
        log.debug( "        Filter: " + _filter );
        loadTable( _filter );
    }


    protected void loadTable( final Filter _filter ) {
        final Display display = Polymap.getSessionDisplay();

        PlatformJobs.run( new IRunnableWithProgress() {
            public void run( IProgressMonitor monitor )
            throws InvocationTargetException, InterruptedException {
                try {
                    long start = System.currentTimeMillis();

                    // query features
                    GeoSelectionView.this.filter = _filter;
                    DefaultQuery query = new DefaultQuery( fs.getSchema().getName().getLocalPart(), filter );
                    query.setMaxFeatures( DEFAULT_MAX_RESULTS );
                    fc = fs.getFeatures( query );
                    //log.debug( "        fc size: " + fc.size() );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                    return;
                }

                // update UI
                display.asyncExec( new Runnable() {
                    public void run() {
                        if (sizeLabel != null) {
                            sizeLabel.setText( "ausgewählt: " + fc.size() );
                        }

                        log.debug( "viewer= " + viewer );
                        if (viewer.getViewer() == null) {
                            viewer.createTableControl( parent );
                            viewer.getControl().setLayoutData( dTable );
                            //viewer.getControl().pack();

                            viewer.addSelectionChangedListener( tableSelectionListener );

                            Shell shell = GeoSelectionView.this.getSite().getShell();
                            shell.layout( true );
                            Point size = shell.getSize();
                            log.info( "Size: " + size.toString() );
                            viewer.getControl().setSize( 1200, 200 /*size.y, size.x*/ );
                        }
                        viewer.setFeatures( allowModify
                                ? new ModifierFeatureCollection( layer, (FeatureStore)fs, fc )
                                : fc );

                        // view title
                        int count = fc.size();  //getviewer.getViewer().getTable().getItemCount()
                        String title = basePartName + " (" + count + ")";
                        setPartName( title );

                        //viewer.getViewer().getTable().pack( true );
                        viewer.getViewer().getTable().layout( true );
                        //viewer.getViewer().refresh();
                        //viewer.getControl().update();
                        //viewer.getControl().redraw();
                    }
                });
            }
        } );
    }


    /**
     *
     */
    protected class TableSelectionListener
            implements ISelectionChangedListener {

        public void selectionChanged( SelectionChangedEvent ev ) {
            log.info( "TableSelectionListener.selectionChanged(): ev= " + ev.getSelection() + ", " + ev.getSource() );

            LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
            fsm.setHovered( viewer.getSelectionFids().iterator().next() );
        }
    }


    /**
     *
     */
    protected class GeoSelectionListener
            implements ISelectionListener, PropertyChangeListener {
        
        public void propertyChange( PropertyChangeEvent ev ) {
            log.info( "ev = " + ev );
            LayerFeatureSelectionManager fsm = (LayerFeatureSelectionManager)ev.getSource();
            
            // select
            if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_FILTER )) {
                log.info( "    filter = " + ev.getNewValue() );
                loadTable( (Filter)ev.getNewValue() );
            }
            // hover
            if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_HOVER )) {
                viewer.removeSelectionChangedListener( tableSelectionListener );
                viewer.setSelection( new StructuredSelection( fsm.getHovered() ) );
                viewer.addSelectionChangedListener( tableSelectionListener );
            }
        }

        public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
            log.info( "selectionChanged(): ev= " + selection );
//            try {
//                Set<String> fids = new HashSet( ((IStructuredSelection)selection).toList() );
//
//                log.debug( "            FeatureSource: " + fs.getName() );
//
//                Set<FeatureId> featureIds = new HashSet<FeatureId>();
//                for (String fid : fids ){
//                   featureIds.add( ff.featureId( fid ) );
//                }
//                Filter filter = ff.id( featureIds );
//
//                log.info( "Filter: " + filter );
//                loadTable( filter );
//            }
//            catch (Exception e) {
//                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
//            }
        }
    }


    /**
     *
     */
    protected class PartListener
            extends PartListenerAdapter {
        public void partActivated( IWorkbenchPart part ) {
            log.debug( "partActivated: " + part );
//            IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
//            if (editor != null && editor instanceof MapEditor) {
        }
    }


    /**
     *
     */
    protected class LayerChangeListener
            implements PropertyChangeListener {

        public void propertyChange( PropertyChangeEvent ev ) {
            if (layer != null
                    && ev.getPropertyName().equals( "edit" )
                    && ev.getNewValue().equals( false )) {

                Display display = Polymap.getSessionDisplay();
                display.asyncExec( new Runnable() {
                    public void run() {
                        if (page != null) {
                            page.hideView( GeoSelectionView.this );
                            disconnectLayer();
                        }
                    }
                });
            }
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
//                    GeoSelectionView.this.fillContextMenu( manager );
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
        if (viewer != null && viewer.getViewer() != null) {
            viewer.getControl().setFocus();
        }
    }


    class NameSorter
            extends ViewerSorter {
    }

}