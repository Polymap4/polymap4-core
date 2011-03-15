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
package org.polymap.core.mapeditor.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.identity.FeatureId;

import net.refractions.udig.core.StaticProvider;
import net.refractions.udig.ui.FeatureTableControl;
import net.refractions.udig.ui.PlatformJobs;
import net.refractions.udig.ui.ProgressManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.geohub.GeoEventListener;
import org.polymap.core.geohub.GeoEventSelector;
import org.polymap.core.geohub.GeoHub;
import org.polymap.core.geohub.GeoEventSelector.MapNameFilter;
import org.polymap.core.geohub.GeoEventSelector.TypeFilter;
import org.polymap.core.geohub.event.GeoEvent;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.ui.DefaultPartListener;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *  
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GeoSelectionView
        extends ViewPart { 

    private static Log log = LogFactory.getLog( GeoSelectionView.class );

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String      ID = "org.polymap.core.mapeditor.ui.GeoSelectionView";
    
    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

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
    
    private DefaultPartListener     partListener = new PartListener();

    private PropertyChangeListener  layerChangeListener = new LayerChangeListener();

    private IWorkbenchPage          page;

    private boolean                 allowModify = false;
    
    private boolean                 allowSearch = true;
    

    public GeoSelectionView() {
        log.debug( "..." );
    }

    public void setAllowModify( boolean allowModify ) {
        this.allowModify = allowModify;
    }

    public void setAllowSearch( boolean allowSearch ) {
        this.allowSearch = allowSearch;
    }

    public ILayer getLayer() {
        return layer;
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
        setPartName( layer.getLabel() );

        // property listener
        ProjectRepository.instance().addPropertyChangeListener( layerChangeListener );

        // part listener
        page = getSite().getWorkbenchWindow().getActivePage();
        page.addPartListener( partListener );

        // geo event listener
        GeoHub.instance().subscribe( geoSelectionListener, 
                new GeoEventSelector( 
                        new MapNameFilter( layer.getMap().getLabel() ),
                        new TypeFilter( GeoEvent.Type.FEATURE_HOVERED, GeoEvent.Type.FEATURE_SELECTED, GeoEvent.Type.FEATURE_CREATED ) ) );

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
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }

        if (fs != null) {
            loadTable( Filter.EXCLUDE );
        }
    }
    

    public void disconnectLayer() {
        if (page != null) {
            // domain listener
            ProjectRepository.instance().removePropertyChangeListener( layerChangeListener );

            // part listener
            page.removePartListener( partListener );
            page = null;

            // geo event listener
            if (!GeoHub.instance().unsubscribe( geoSelectionListener )) {
                throw new IllegalStateException( "Unable to unsubscribe GeoSelectionListener!" );
            }
        }

        // geo event
        if (layer != null && fc != null) {
            try {
                GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_SELECTED, 
                        layer.getMap().getLabel(), 
                        layer.getGeoResource().getIdentifier().toURI() );
                GeoHub.instance().send( event, geoSelectionListener );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
        
        layer = null;
        fs = null;
        fc = null;
        filter = null;
    }
    
    
    public void dispose() {
        log.debug( "dispose() ..." );
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
        
        Filter filter = Filter.EXCLUDE;
        // all
        if (attrCombo.getSelectionIndex() == 0) {
            for (PropertyDescriptor prop : fs.getSchema().getDescriptors()) {
                if (!(prop instanceof GeometryDescriptor)) {
                    PropertyIsLike like = ff.like( ff.property( prop.getName().getLocalPart() ), text );
                    filter = filter != null ? ff.or( filter, like ) : like;
                }
            }
        // one property
        } else {
            filter = ff.like( ff.property( attrCombo.getText() ), text );
        }
        log.debug( "        Filter: " + filter );
        loadTable( filter );
    }
    
    
    protected void loadTable( final Filter _filter ) {
        final Display display = Polymap.getSessionDisplay();

        PlatformJobs.run( new IRunnableWithProgress() {
            public void run( IProgressMonitor monitor )
            throws InvocationTargetException, InterruptedException {
                try {
                    // query features
                    GeoSelectionView.this.filter = _filter;
                    DefaultQuery query = new DefaultQuery( fs.getSchema().getName().getLocalPart(), filter );
                    query.setMaxFeatures( 500 );
                    fc = fs.getFeatures( query );
                    log.debug( "        fc size: " + fc.size() );
                    
                    // geo event
                    GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_SELECTED, 
                            layer.getMap().getLabel(), 
                            layer.getGeoResource().getIdentifier().toURI() );
                    event.setBody( fc );
                    GeoHub.instance().send( event, geoSelectionListener );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
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
                            viewer.getControl().pack();
                            viewer.addSelectionChangedListener( tableSelectionListener );
                        }
                        viewer.setFeatures( allowModify 
                                ? new ModifierFeatureCollection( (FeatureStore)fs, fc ) 
                                : fc );
//                        Shell shell = GeoSelectionView.this.getSite().getShell();
//                        Point size = shell.getSize();
//                        shell.setSize( size.x, size.y-2 );
//                        getSite().getShell().update();
                        
                        //viewer.getControl().pack();
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
            
            try {
                GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_HOVERED, 
                        layer.getMap().getLabel(), 
                        layer.getGeoResource().getIdentifier().toURI() );
                
                final Set<String> fids = new HashSet( viewer.getSelectionFids() );
                final Collection<Feature> body = new ArrayList();
                
                fc.accepts( new FeatureVisitor() {
                    public void visit( Feature feature ) {
                        if (fids.contains( feature.getIdentifier().getID() )) {
                            body.add( feature );
                        }
                    }
                }, null );
                event.setBody( body );
                
                GeoHub.instance().send( event, geoSelectionListener );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "Unable to send event.", e );
            }
        }
    }


    /**
     * 
     */
    protected class GeoSelectionListener
            implements ISelectionListener, GeoEventListener {
        
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

        public void onEvent( GeoEvent ev ) {
            log.info( "ev: " + ev );
            
            // FEATURE_CREATED
            if (ev.getType() == GeoEvent.Type.FEATURE_CREATED) {
                // reload table with current filter
                loadTable( filter );

                Set<FeatureId> fids = new HashSet();
                String sfid = null;
                for (Feature feature : ev.getBody()) {
                    fids.add( feature.getIdentifier() );
                    sfid = feature.getIdentifier().getID();
                }
                
                viewer.removeSelectionChangedListener( tableSelectionListener );
//                viewer.select( fids );
                viewer.setSelection( new StructuredSelection( sfid ) );
                viewer.addSelectionChangedListener( tableSelectionListener );
            }
            
            // FEATURE_SELECTED
            else if (ev.getType() == GeoEvent.Type.FEATURE_SELECTED) {
                //log.info( "fc: " + ev.getBody().size() );
                loadTable( ev.getFilter() );
            }
            
            // FEATURE_HOVERED
            else if (ev.getType() == GeoEvent.Type.FEATURE_HOVERED) {
                Set<FeatureId> fids = new HashSet();
                String sfid = null;
                for (Feature feature : ev.getBody()) {
                    fids.add( feature.getIdentifier() );
                    sfid = feature.getIdentifier().getID();
                }
                
                viewer.removeSelectionChangedListener( tableSelectionListener );
//                viewer.select( fids );
                viewer.setSelection( new StructuredSelection( sfid ) );
                viewer.addSelectionChangedListener( tableSelectionListener );
            }
            else {
                log.warn( "Unhandled event type: " + ev );
            }
        }
    }

    
    /**
     * 
     */
    protected class PartListener
            extends DefaultPartListener {
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