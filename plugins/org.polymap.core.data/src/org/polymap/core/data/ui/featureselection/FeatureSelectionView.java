/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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

import java.util.EventObject;
import java.util.List;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.data.FeatureChangeListener;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelectionView
        extends ViewPart 
        implements ISelectionChangedListener {

    private static Log log = LogFactory.getLog( FeatureSelectionView.class );
    
    public static final String              ID = "org.polymap.core.data.FeatureSelectionView";

    private static final FilterFactory      ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

    /* Bad but effective way to pass the layer to the view. */
    private static final ThreadLocal<ILayer>    initLayer = new ThreadLocal();


    /**
     * Makes sure that the view for the layer is open. If the view is already open,
     * then it is activated.
     * 
     * @param layer
     * @return The view for the given layer, or null if there was an error opening
     *         the view.
     */
    public static FeatureSelectionView open( final ILayer layer ) {
        final FeatureSelectionView[] result = new FeatureSelectionView[1];

        Polymap.getSessionDisplay().syncExec( new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                    initLayer.set( layer );
                    IViewPart view = page.showView( FeatureSelectionView.ID, layer.id(), IWorkbenchPage.VIEW_ACTIVATE );
                    if (view instanceof FeatureSelectionView) {
                        result[0] = (FeatureSelectionView)view;
                    }
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, null, e.getMessage(), e );
                }
                finally {
                    initLayer.remove();
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

                    FeatureSelectionView view = (FeatureSelectionView)page.findView( FeatureSelectionView.ID );
                    if (view != null) {
                        page.hideView( view );
                        view.dispose();
                    }
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getMessage(), e );
                }
            }
        });
    }


    // instance *******************************************

    private ILayer                  layer;
    
    /** Might be null if fs could not be instantiated for the layer */
    private PipelineFeatureSource   fs;

    private Filter                  filter = Filter.EXCLUDE;

    /** Might be null if fs could not be instantiated for the layer */
    private FeatureTableViewer      viewer;

    private String                  basePartName;

    private Composite               parent;
    
    private FeatureSelectionListener selectionListener = new FeatureSelectionListener();
    
    private FeatureChangeListener   changeListener;
    
    private ModelListener           modelListener = new ModelListener();
    
    public IActionDelegate          openAction;


    public void init( IViewSite site, IMemento memento )
    throws PartInitException {
        super.init( site, memento );
        if (memento != null) {
            final String layerId = memento.getString( "layerId" );
            final String filterText = memento.getTextData();
            if (layerId != null && filterText != null) {
                try {
                    layer = ProjectRepository.instance().findEntity( ILayer.class, layerId );
                    if (layer != null) {
                        Configuration config = new org.geotools.filter.v1_1.OGCConfiguration();
                        Parser parser = new Parser( config );
                        filter = (Filter)parser.parse( new ByteArrayInputStream( filterText.getBytes( "UTF8" ) ) );
                        
                        // *after* createPartControl()
                        Polymap.getSessionDisplay().asyncExec( new Runnable() {
                            public void run() {
                                LayerFeatureSelectionManager.forLayer( layer ).changeSelection( filter );
                                //loadTable( filter );
                            }
                        });
                    }
                }
                catch (NoSuchEntityException e) {
                    log.warn( "Layer does no longer exists: " + layerId );
                }
                catch (Exception e) {
                    log.warn( "Unable to restore state.", e );
                }
            }
        }
    }

    
    public void saveState( IMemento memento ) {
        if (layer != null) {
            try {
                if (filter == null) {
                    filter = Filter.EXCLUDE;
                }
                if (filter instanceof Id) {
                    // save max. 500 Fids
                    if (((Id)filter).getIdentifiers().size() > 500) {
                        return;
                    }
                }
                OGCConfiguration config = new org.geotools.filter.v1_1.OGCConfiguration();
                Encoder encoder = new Encoder( config );
                encoder.setIndenting( true );
                encoder.setIndentSize( 4 );
                ByteArrayOutputStream bout = new ByteArrayOutputStream( 4096 );
                encoder.encode( filter, org.geotools.filter.v1_0.OGC.Filter, bout );
    
                if (bout.size() < 10*1024) {
                    memento.putTextData( bout.toString( "UTF8" ) );
                    memento.putString( "layerId", layer.id() );
                    log.info( bout.toString( "UTF8" ) );
                }
            }
            catch (Exception e) {
                log.warn( "Unable to save state:", e );
            }
        }
    }


    protected void init( ILayer _layer ) {
        try {
            this.layer = _layer;
            this.basePartName = layer.getLabel(); 
            setPartName( basePartName );
            
            // PropertyChangeListener: layer
            layer.addPropertyChangeListener( modelListener );

            // FeatureSelectionListener
            LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
            filter = fsm.getFilter();
            fsm.addSelectionChangeListener( selectionListener );

            // FeatureChangeListener
            EventManager.instance().subscribe( changeListener = 
                new FeatureChangeListener() {
                    public void featureChanges( List<FeatureChangeEvent> events ) {
                        loadTable( filter );
                    }
                }, 
                new EventFilter<EventObject>() {
                    public boolean apply( EventObject ev ) {
                        return viewer != null && layer.equals( ev.getSource() );
                    }
                }
            );
                
            this.fs = PipelineFeatureSource.forLayer( layer, false );            
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "", e );
        }
    }
    

    public void dispose() {
        super.dispose();
        
        if (layer != null) {
            try {
                layer.removePropertyChangeListener( modelListener );
                LayerFeatureSelectionManager.forLayer( layer ).removeSelectionChangeListener( selectionListener );
                EventManager.instance().unsubscribe( changeListener ); 
            }
            catch (NoSuchEntityException e) {
                // layer is deleted -> ignore
            }        
        }
        if (viewer != null) {
            viewer.dispose();
            viewer = null;
        }
        layer = null;
        fs = null;
    }

    
    public ILayer getLayer() {
        return layer;
    }

    
    public PipelineFeatureSource getFeatureStore() {
        return fs;
    }
    
    
    public Filter getFilter() {
        return filter; 
    }

    
    public IFeatureTableElement[] getTableElements() {
        return fs != null ? viewer.getElements() : new IFeatureTableElement[0];
    }
    
    
    public IFeatureTableElement[] getSelectedElements() {
        return viewer.getSelectedElements();
    }
    
    
    public void createPartControl( @SuppressWarnings("hiding") Composite parent ) {
        if (initLayer.get() != null) {
            init( initLayer.get() );
        }
        else {
            if (layer == null) {
                log.warn( "No layer set. Closing view..." );
                Label msg = new Label( parent, SWT.NONE );
                msg.setText( "No layer." );
                close( null );
            }
            else {
                init( layer );
            }
        }
        
        this.parent = parent;
        this.parent.setLayout( new FormLayout() );
        
        // check fs -> error message
        if (fs == null) {
            Label msg = new Label( parent, SWT.NONE );
            msg.setText( "No feature pipeline for layer: " + layer.getLabel() );
            return;
        }

        viewer = new FeatureTableViewer( parent, SWT.MULTI );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().create() );

        viewer.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                if (ev.getPropertyName().equals( FeatureTableViewer.PROP_CONTENT_SIZE )) {
                    Integer count = (Integer)ev.getNewValue();
                    setPartName( basePartName + " (" + count + ")" );
                }
            }
        });
        viewer.addSelectionChangedListener( this );
        
        // double-click
        viewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                log.info( "doubleClick(): " + ev );
                //IAction openHandler = getViewSite().getActionBars().getGlobalActionHandler( "org.polymap.rhei.OpenFormAction" );
                if (openAction != null) {
                    openAction.run( null );
                }
            }
        });
        
        // columns
        assert fs != null : "fs not set. Call init() first.";
        SimpleFeatureType schema = fs.getSchema();
        for (PropertyDescriptor prop : schema.getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                viewer.addColumn( new DefaultFeatureTableColumn( prop ) );
            }
        }

        viewer.getTable().pack( true );

        getSite().setSelectionProvider( viewer );
        hookContextMenu();
        
        loadTable( filter );
    }


    protected void hookContextMenu() {
        final MenuManager contextMenu = new MenuManager( "#PopupMenu" );
        contextMenu.setRemoveAllWhenShown( true );
        
        contextMenu.addMenuListener( new IMenuListener() {
            public void menuAboutToShow( IMenuManager manager ) {
                // Other plug-ins can contribute there actions here
                manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
                manager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
                //manager.add( new Separator() );

//                TreeSelection sel = (TreeSelection)viewer.getSelection();
//                if (!sel.isEmpty() && sel.getFirstElement() instanceof IMap) {
//                    ProjectView.this.fillContextMenu( manager );
//                }
            }
        } );
        Menu menu = contextMenu.createContextMenu( viewer.getControl() );        
        viewer.getControl().setMenu( menu );
        getSite().registerContextMenu( contextMenu, viewer );
    }


    protected void loadTable( @SuppressWarnings("hiding") Filter filter ) {
        this.filter = filter;
        viewer.setContent( fs, filter );
    }

    
    /**
     * 
     */
    class FeatureSelectionListener
            implements PropertyChangeListener {
        
        /**
         * Other party has changed feature selection.
         */
        public void propertyChange( final PropertyChangeEvent ev ) {

            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    // select
                    if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_FILTER )) {
                        loadTable( (Filter)ev.getNewValue() );
                    }
                    // hover
                    else if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_HOVER )) {
                        LayerFeatureSelectionManager fsm = (LayerFeatureSelectionManager)ev.getSource();
                        viewer.removeSelectionChangedListener( FeatureSelectionView.this );
                        viewer.selectElement( fsm.getHovered(), true );
                        viewer.addSelectionChangedListener( FeatureSelectionView.this );
                    }
                }
            });
        }
    }
    

    /**
     * 
     */
    class ModelListener {
        @EventHandler(display=true)
        public void propertyChange( PropertyChangeEvent ev ) {
            if (PropertyChangeSupport.PROP_ENTITY_REMOVED.equals( ev.getPropertyName() )) {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                page.hideView( FeatureSelectionView.this );
            }
        }
    }
    
    /*
     * Element was selected in the table.
     */
    public void selectionChanged( SelectionChangedEvent ev ) {
        IStructuredSelection sel = (IStructuredSelection)ev.getSelection();
        IFeatureTableElement elm = (IFeatureTableElement)sel.getFirstElement();
        
        LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
        fsm.setHovered( elm.fid() );
    }            
    
    
    public void setFocus() {
    }
    
}
