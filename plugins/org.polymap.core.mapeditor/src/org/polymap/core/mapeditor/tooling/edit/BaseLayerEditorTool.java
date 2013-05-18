/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling.edit;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.mapeditor.tooling.DefaultEditorTool;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.model.AssocCollection;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Layers;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Provides base class for tools that choose one vector layer to work with.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class BaseLayerEditorTool
        extends DefaultEditorTool
        implements VectorLayerStylerAware {

    private static Log log = LogFactory.getLog( BaseLayerEditorTool.class );
    
    static final Set<String>        allowedMapEvents = Sets.newHashSet( new String[] 
            {IMap.PROP_LAYERS, ILayer.PROP_VISIBLE, ILayer.PROP_LABEL } );

    public static final String      PROP_CREATED = "created";
    public static final String      PROP_DISPOSED = "disposed";
    public static final String      PROP_LAYER_ACTIVATED = "layer_activated";
    public static final String      PROP_LAYER_DEACTIVATED = "layer_deactivated";
    
    /**
     *
     */
    public static class SessionTools
            extends SessionSingleton {
        
        protected ListenerList<PropertyChangeListener> listeners = new ListenerList();

        public boolean addListener( PropertyChangeListener listener ) {
            return listeners.add( listener );
        }

        public boolean removeListener( PropertyChangeListener listener ) {
            return listeners.remove( listener );
        }
    }
    
    /**
     * Add/remove session wide listeners to all tools.
     */
    public static SessionTools sessionTools() {
        return SessionTools.instance( SessionTools.class );
    }
    
    
    // instance *******************************************
    
    protected CCombo                layersList;
    
    private IMap                    map;
    
    private ILayer                  selectedLayer;

    private MapListener             mapListener;

    private IWorkbenchPage          page;

    private ISelectionListener      pageListener;
    
    private ListenerList<PropertyChangeListener> listeners = new ListenerList();
    
    
    @Override
    public boolean init( IEditorToolSite site ) {
        boolean result = super.init( site );
        this.map = site.getEditor().getMap();
        
        // get config from memento
        String selectedLayerId = site.getMemento().getString( "selectedLayerId" );
        for (ILayer layer : selectableLayers()) {
            if (layer.id().equals( selectedLayerId )) {
                selectedLayer = layer;
                break;
            }
        }
        
        fireEvent( this, PROP_CREATED, null );
        return result;
    }

    
    @Override
    public void dispose() {        
        super.dispose();
        fireEvent( this, PROP_DISPOSED, null );
    }


    public boolean addListener( PropertyChangeListener listener ) {
        return listeners.add( listener );
    }

    public boolean removeListener( PropertyChangeListener listener ) {
        return listeners.remove( listener );
    }

    protected void fireEvent( BaseLayerEditorTool tool, String name, Object newValue ) {
        PropertyChangeEvent ev = new PropertyChangeEvent( tool, name, null, newValue );
        for (PropertyChangeListener l : listeners) {
            try {
                l.propertyChange( ev );
            }
            catch (Exception e) {
                log.warn( e, e );
            }
        }
        for (PropertyChangeListener l : sessionTools().listeners) {
            try {
                l.propertyChange( ev );
            }
            catch (Exception e) {
                log.warn( e, e );
            }
        }
    }


    public ILayer getSelectedLayer() {
        return selectedLayer;
    }

    /** Allow {@link VectorLayerStyler} to access. */
    @Override
    public IEditorToolSite getSite() {
        return super.getSite();
    }

    /** Allow {@link VectorLayerStyler} to access. */
    @Override
    public void layoutControl( String label, Control control ) {
        super.layoutControl( label, control );
    }


    public abstract BaseVectorLayer getVectorLayer();
    

    protected List<ILayer> selectableLayers() {
        AssocCollection<ILayer> layers = getSite().getEditor().getMap().getLayers();
        Iterable<ILayer> result = filter( layers, Layers.isVisible() );
        result = filter( result, isVector() );
        
        List<ILayer> list = newArrayList( result );
        Collections.sort( list, Layers.zPrioComparator() );        
        return Lists.reverse( list );
    }

    
    protected String[] labels( List<ILayer> layers ) {
        return toArray( transform( layers, Layers.asLabel() ), String.class );
    }
    
    
    protected void changeLayer( ILayer layer ) {
        assert layer != null;
        if (!selectedLayer.equals( layer )) {
            onDeactivate();
            selectedLayer = layer;
            onActivate();
        }
    }


    /**
     * Install {@link #mapListener} and {@link #pageListener}.
     * <p/>
     * Beware that this is also called from {@link #changeLayer(ILayer)}. You may
     * check {@link #selectedLayer} to see if this call comes from
     * {@link #changeLayer(ILayer)}.
     * <p/>
     * <b>Beware:</b>This method does <b>not</b>
     * {@link #fireEvent(BaseLayerEditorTool, String, Object)}. Caller has to do so
     * after activation is complete!
     */
//    @Override
    public void onActivate() {
        super.onActivate();
        
        // find best initial layer (if not called from changeLayer())
        if (selectedLayer == null) {
            List<ILayer> selectableLayers = selectableLayers();
            if (selectableLayers.size() >= 1) {
                selectedLayer = selectableLayers.get( 0 );
            }
        }

        mapListener = new MapListener();
        EventManager.instance().subscribe( mapListener, mapListener );

        // Hmmm...?! the PageListener is annoying without the possibility to pin the active layer
//        pageListener = new PageListener();
//        page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//        page.addSelectionListener( pageListener );
        
//        // delay event until sub-class has initialized all fields
//        // so that listeners can access those fields
//        Polymap.getSessionDisplay().asyncExec( new Runnable() {
//            public void run() {
//                fireEvent( BaseLayerEditorTool.this, PROP_LAYER_ACTIVATED, selectedLayer );
//            }
//        });
    }


    /**
     * Uninstall {@link #mapListener} and {@link #pageListener}.
     * <p/>
     * Beware that this is also called from {@link #changeLayer(ILayer)}. You may
     * check {@link #selectedLayer} to see if this call comes from
     * {@link #changeLayer(ILayer)}.
     */
    @Override
    public void onDeactivate() {
        super.onDeactivate();
        
        if (mapListener != null) {
            EventManager.instance().unsubscribe( mapListener );
            mapListener = null;
        }
        if (pageListener != null) {
            page.removeSelectionListener( pageListener );
            pageListener = null;
        }
//        // delay event until sub-class has initialized all fields
//        // so that listeners can access those fields
//        Polymap.getSessionDisplay().asyncExec( new Runnable() {
//            public void run() {
                fireEvent( BaseLayerEditorTool.this, PROP_LAYER_DEACTIVATED, selectedLayer );
//            }
//        });
        
        //selectedLayer = null;
        getSite().getMemento().putString( "selectedLayerId", selectedLayer != null ? selectedLayer.id() : "" );
    }

    
    @Override
    public void createPanelControl( Composite parent ) {
        super.createPanelControl( parent );
    
        // layer(s)
        List<ILayer> selectableLayers = selectableLayers();
        layersList = getSite().getToolkit().createCombo( parent, labels( selectableLayers ) );
        layersList.setEditable( false );
        layersList.setVisibleItemCount( 18 );
        layersList.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
        layoutControl( i18n( "layerLabel" ), layersList );
    
        // select layer item
        if (selectedLayer != null) {
            String[] items = layersList.getItems();
            for (int i=0; i < items.length; i++) {
                if (items[i].equalsIgnoreCase( selectedLayer.getLabel() )) {
                    layersList.select( i );
                }
            }
        }
        
        layersList.addSelectionListener( new SelectionAdapter() {            
            public void widgetSelected( SelectionEvent ev ) {
                final String item = layersList.getItem( layersList.getSelectionIndex() );
                AssocCollection<ILayer> layers = getSite().getEditor().getMap().getLayers();
                changeLayer( getFirst( filter( layers, Layers.hasLabel( item ) ), null ) );
            }
        });
    }

    
    /**
     * Listen to selction changes inside the Workbench page. 
     */
    protected class PageListener
            implements ISelectionListener {

        public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
            if (sel instanceof IStructuredSelection) {
                Object elm = ((IStructuredSelection)sel).getFirstElement();
                if (elm instanceof ILayer
                        && selectableLayers().contains( elm )
                        && ((ILayer)elm).getMap().equals( map )
                        && !selectedLayer.equals( elm )) {
                    ILayer layer = (ILayer)elm;
                    
                    boolean confirmed = MessageDialog.openConfirm( PolymapWorkbench.getShellToParentOn(), 
                            i18n( "layerChangedTitle" ), i18n( "layerChangedMsg", layer.getLabel() ) );

                    if (confirmed) {
                        String[] items = layersList.getItems();
                        for (int i=0; i < items.length; i++) {
                            if (items[i].equalsIgnoreCase( layer.getLabel() )) {
                                layersList.select( i );
                                changeLayer( layer );
                            }
                        }
                    }
                }
            }
        }
    }

    
    /**
     * Listen to property changes of the {@link #map} or its layers. 
     */
    protected class MapListener
            implements EventFilter<PropertyChangeEvent> {
    
        @EventHandler(display=true)
        public void propertyChange( PropertyChangeEvent ev ) {
            if (layersList == null) {
                return;
            }
            String[] items = labels( selectableLayers() );
            layersList.setItems( items );

            // old layer still visible? -> select in combo
            if (selectedLayer != null && selectedLayer.isVisible()) {
                for (int i=0; i < items.length; i++) {
                    if (items[i].equalsIgnoreCase( selectedLayer.getLabel() )) {
                        layersList.select( i );
                    }
                }
            }
            // otherwise deactivate
            else {
                onDeactivate();
            }
        }
        
        public boolean apply( PropertyChangeEvent ev ) {
            if (allowedMapEvents.contains( ev.getPropertyName() )) {
                if (ev.getSource() instanceof IMap) {
                    return ev.getSource().equals( map );
                }
                else if (ev.getSource() instanceof ILayer) {
                    return ((ILayer)ev.getSource()).getMap().equals( map );
                }
            }
            return false;
        }
    }

    
    /**
     * 
     * @return Newly created {@link Predicate}.
     */
    public static Predicate<ILayer> isVector() {
        return new Predicate<ILayer>() {
            public boolean apply( ILayer input ) {
                try {
                    return PipelineFeatureSource.forLayer( input, false ) != null;
                }
                catch (Exception e) {
                    return false;
                }
            }
        };
    }

    
    public abstract String i18n( String key, Object... args );
    
}
