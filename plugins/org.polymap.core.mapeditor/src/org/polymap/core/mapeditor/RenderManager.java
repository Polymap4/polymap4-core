/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.ISettingStore;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.data.FeatureChangeListener;
import org.polymap.core.data.FeatureEventManager;
import org.polymap.core.mapeditor.services.SimpleWmsServer;
import org.polymap.core.model.event.IEventFilter;
import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.PipelineHolder;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.model.LayerComposite;
import org.polymap.core.project.operations.OpenMapOperation;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.service.http.HttpServiceFactory;
import org.polymap.service.http.WmsService;

/**
 * The RenderManager is the bridge between an {@link IMap} and the {@link MapEditor}
 * that displays the contents of this map. It listens to all kind of events regarding
 * its map and changes its {@link #mapEditor} as needed.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class RenderManager {

    private static Log log = LogFactory.getLog( RenderManager.class );

    private IMap                    map;
    
    private MapEditor               mapEditor;

    private WmsService              wmsService;
    
    private TreeMap<String,RenderLayerDescriptor> descriptors = new TreeMap();
    
    private MapDomainListener       modelListener = new MapDomainListener();
    
    private FeatureListener         featureListener = new FeatureListener();
    
    /** The layer that is currently in edit (vector) mode or null. */
    private ILayer                  editLayer;
    
    
    public RenderManager( IMap map, MapEditor mapEditor ) {
        super();
        this.map = map;
        this.mapEditor = mapEditor;
        
        // model listener
        IEventFilter eventFilter = new IEventFilter() {
            public boolean accept( EventObject ev ) {
                if (RenderManager.this.map == null || ev.getSource() == null) {
                    return false;
                }
                if (ev.getSource() instanceof IMap) {
                    IMap eventMap = (IMap)ev.getSource();
                    return RenderManager.this.map.equals( eventMap );
                }
                else if (ev.getSource() instanceof ILayer) {
                    ILayer layer = (ILayer)ev.getSource();
                    return RenderManager.this.map.equals( layer.getMap() );
                }
                return false;
            }
        };
        ProjectRepository module = ProjectRepository.instance();
        module.addPropertyChangeListener( modelListener, eventFilter ); 
        module.addModelChangeListener( modelListener, eventFilter );
        
        // feature listener
        FeatureEventManager fem = FeatureEventManager.instance();
        fem.addFeatureChangeListener( featureListener, new IEventFilter<FeatureChangeEvent>() {
            public boolean accept( FeatureChangeEvent ev ) {
                if (RenderManager.this.map == null || ev.getSource() == null) {
                    return false;
                }
                return RenderManager.this.map.equals( ev.getSource().getMap() );
            }
        });
    }

    
    public void dispose() {
        clearPipelines();
        if (modelListener != null && map != null) {
            ProjectRepository module = ProjectRepository.instance();
            module.removePropertyChangeListener( modelListener );
            module.removeModelChangeListener( modelListener );
            modelListener = null;
        }
        if (featureListener != null) {
            FeatureEventManager.instance().removeFeatureChangeListener( featureListener );
            featureListener = null;
        }
        if (wmsService != null) {
            deleteWms( wmsService );
            wmsService = null;
        }
        this.map = null;
        this.mapEditor = null;
    }
    
    
    /** 
     * The layer that is currently in edit (vector) mode or null. 
     */
    public ILayer getEditLayer() {
        return editLayer;
    }


    protected synchronized void clearPipelines() {
        if (mapEditor != null) {
            for (RenderLayerDescriptor descriptor : descriptors.values()) {
                mapEditor.removeLayer( descriptor );
            }
        }
        descriptors.clear();
        editLayer = null;
    }
    
    
    /**
     * Prepares the render pipelines / OwsServers for the map.
     * <ol>
     * <li>find the {@link IService}s that are associated with the layers of the {@link #map}.</li>
     * <li>create a render pipeline for each service.<li>
     * <li>create a {@link WmsService} server for each service/pipeline.</li>
     * </ol>
     * @throws Exception 
     */
    protected synchronized void updatePipelines() {
        clearPipelines();
        IProgressMonitor monitor = UIJob.monitorForThread();

        if (wmsService != null) {
            deleteWms( wmsService );
            wmsService = null;
        }
        wmsService = createWms();
        
        // check geoResource of layers
        for (final ILayer layer : map.getLayers()) {
            try {
                log.debug( "layer: " + layer + ", label= " + layer.getLabel() + ", visible= " + layer.isVisible() );
                if (layer.isVisible()) {
                    monitor.subTask( "Find geo-resource of layer: " + layer.getLabel() );
                    IGeoResource res = layer.getGeoResource();
                    if (res == null) {
                        log.warn( "Unable to find geo resource of layer: " + layer );
                        Display.getCurrent().asyncExec( new Runnable() {
                            public void run() {
                                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                MessageBox box = new MessageBox( window.getShell(), SWT.APPLICATION_MODAL | SWT.ICON_WARNING );
                                String label = layer instanceof LayerComposite
                                        ? ((LayerComposite)layer).georesId().get()
                                        : layer.getLabel();
                                box.setMessage( "Geo-Ressource kann im Katalog nicht gefunden werden: " + label );
                                box.setText( "Achtung." );
                                box.open();
                            }
                        });
                        continue;
                    }

                    RenderLayerDescriptor descriptor = new RenderLayerDescriptor( 
                            wmsService.getURL(), layer.isEditable(), layer.getOrderKey(), layer.getOpacity() );
                    descriptor.layers.add( layer );
                    descriptors.put( descriptor.renderLayerKey(), descriptor );
                    
                    monitor.worked( 1 );
                }
            }
            catch (Exception e) {
                // XXX mark layers!?
                log.warn( "skipping layer: " + layer.getLabel() + " (" + e.toString(), e );
            }
        }
        
        // add layers to mapEditor
        for (RenderLayerDescriptor descriptor : descriptors.values()) {
            mapEditor.addLayer( descriptor );
        }
    }

    protected WmsService createWms() {
        try {
            ISettingStore settingStore = RWT.getSettingStore();
            String wmsNamesAttr = settingStore.getAttribute( "RenderManager.wmsnames" );
            JSONObject wmsNames = wmsNamesAttr != null
                    ? new JSONObject( wmsNamesAttr ) : new JSONObject();

            String pathSpec = wmsNames.optString( map.id(), null );
            if (pathSpec == null) {
                pathSpec = "/services/" + map.getLabel() + "--" + System.currentTimeMillis();
                wmsNames.put( map.id(), pathSpec );
                settingStore.setAttribute( "RenderManager.wmsnames", wmsNames.toString() );
            }
        
            log.info( "Service pathSpec: " + pathSpec );

            // use our own WMS impl
            SimpleWmsServer wmsServer = new SimpleWmsServer();
            log.debug( "    service: " + wmsServer.getClass().getName() );

            String url = null;
            try {
                url = HttpServiceFactory.registerServer( wmsServer, pathSpec, true );
            }
            catch (/*Namespace*/Exception e) {
                HttpServiceFactory.unregisterServer( pathSpec, false );
                url = HttpServiceFactory.registerServer( wmsServer, pathSpec, true );                
            }
            wmsServer.init( url, map );

            log.debug( "    URL: " + wmsServer.getURL() );
            return wmsServer;
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "Fehler beim Starten des WMS-Services.", e );
            throw new RuntimeException( e );
        }
    }
    
    
    protected void deleteWms( WmsService wmsServer ) {
        HttpServiceFactory.unregisterServer( wmsServer, true );
    }
    
    
    /**
     * 
     */
    class FeatureListener
            extends FeatureChangeListener {
        
        public void featureChange( FeatureChangeEvent ev ) {
            RenderLayerDescriptor descriptor = findDescriptorForLayer( ev.getSource() );
            if (descriptor != null) {
                // XXX delay reload for subsequent changes?
                mapEditor.reloadLayer( descriptor );
            }
        }
    }
    
    
    /**
     * 
     */
    class MapDomainListener
            implements IModelChangeListener, PropertyChangeListener {

        /**
         * Close the corresponding {@link MapEditor}. This call triggers {@link MapEditor#dispose()}
         * and then {@link RenderManager#dispose()}. So after this call <code>map</code> is null.
         * @return
         */
        protected IWorkbenchPage closeMapEditor() {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            page.closeEditor( mapEditor, false );
            return page;
        }
        
        public void modelChanged( ModelChangeEvent ev ) {
            Display display = mapEditor.getEditorSite().getShell().getDisplay();
            
            // check CRS changes after an operation has has finished and
            // extends are transformed in new CRS
            Iterable<PropertyChangeEvent> crsEvents = ev.events( new IEventFilter<PropertyChangeEvent>() {
                public boolean accept( PropertyChangeEvent iev ) {
                    return iev.getSource() instanceof IMap 
                            && IMap.PROP_CRSCODE.equalsIgnoreCase( iev.getPropertyName() );
                }
            });
            if (!Iterables.isEmpty( crsEvents )) {
                display.asyncExec( new Runnable() {
                    public void run() {
                        try {
                            // map is null after MapEditor closed and dispose()
                            IMap savedMap = map;
                            IWorkbenchPage page = closeMapEditor();
                            
                            // re-open editor
                            OpenMapOperation op = new OpenMapOperation( savedMap, page );
                            OperationSupport.instance().execute( op, true, true );
                        }
                        catch (Exception e) {
                            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
                        }

//                        MessageDialog.openInformation( PolymapWorkbench.getShellToParentOn(),
//                                Messages.get( "RenderManager_updateCrs_title" ),
//                                Messages.get( "RenderManager_updateCrs_msg" ) );
//                        //mapEditor.updateMapCRS();
                    }
                });
            }
        }

        public void propertyChange( PropertyChangeEvent ev ) {
            Display display = mapEditor.getEditorSite().getShell().getDisplay();

            // ILayer
            if (ev.getSource() instanceof ILayer) {
                ILayer layer = (ILayer)ev.getSource();
                RenderLayerDescriptor descriptor = findDescriptorForLayer( layer );
                
                if ("visible".equals( ev.getPropertyName() )) {
                    updatePipelines();
                }
                else if ("edit".equals( ev.getPropertyName() )) {
                    updatePipelines();
                }
                else if (ILayer.PROP_OPACITY.equals( ev.getPropertyName() )) {
                    if (descriptor != null && descriptor.layers.size() == 1) {
                        mapEditor.setLayerOpacity( descriptor, layer.getOpacity() );
                    }
                    else {
                        updatePipelines();
                    }
                }
                else if (ILayer.PROP_ORDERKEY.equals( ev.getPropertyName() )) {
                    if (descriptor != null && descriptor.layers.size() == 1) {
                        mapEditor.setLayerZPriority( descriptor, layer.getOrderKey() );
                    }
                    else {
                        updatePipelines();
                    }
                }
                else if (ILayer.PROP_STYLE.equals( ev.getPropertyName() )) {
                    if (descriptor != null) {
                        mapEditor.reloadLayer( descriptor );
                    }
                }
                else if (PipelineHolder.PROP_PROCS.equals( ev.getPropertyName() )) {
                    if (descriptor != null) {
                        updatePipelines();
                    }
                }
            }
            // IMap
            else if (ev.getSource() instanceof IMap) {
                // check if map was deleted
                if (PropertyChangeSupport.PROP_ENTITY_REMOVED .equals( ev.getPropertyName() )) {
                    display.asyncExec( new Runnable() {
                        public void run() {
                            closeMapEditor();
                            return;
                        }
                    });
                }
                else if (IMap.PROP_LAYERS.equals( ev.getPropertyName() )) {
                    updatePipelines();                    
                }
                else if (IMap.PROP_EXTENT.equals( ev.getPropertyName() )) {
                    ReferencedEnvelope extent = (ReferencedEnvelope)ev.getNewValue();
                    mapEditor.setMapExtent( extent );
                    
                    // XXX refactor this out to MapEditor so that it can be used elsewhere 
                    display.asyncExec( new Runnable() {
                        public void run() {
                            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                            IWorkbenchPage page = window.getActivePage();
                            if (page != null && mapEditor != null 
                                    && page.findEditor( mapEditor.getEditorInput() ) != null) {
                                page.activate( mapEditor );
                            }
                        }
                    });
                }
                else if (IMap.PROP_MAXEXTENT.equals( ev.getPropertyName() )) {
                    display.syncExec( new Runnable() {
                        public void run() {
                            mapEditor.setMapExtent( map.getMaxExtent() );
                            mapEditor.setMaxExtent( map.getMaxExtent() );
                        }
                    });
                }
                else if (IMap.PROP_CRSCODE.equalsIgnoreCase( ev.getPropertyName() )) {
                    // stop listening to events as extents and CRS do not match an longer;
                    // wait for ModelChangeEvent to reload
                    ProjectRepository module = ProjectRepository.instance();
                    module.removePropertyChangeListener( modelListener );
                }
            }
        }
    }

    
    private final RenderLayerDescriptor findDescriptorForLayer( ILayer layer ) {
        for (RenderLayerDescriptor descriptor : descriptors.values()) {
            if (descriptor.layers.contains( layer )) {
                return descriptor;
            }
        }
        return null;
    }

    
    /**
     * A set of {@link ILayer}s along with render properties to be
     * displayed by a {@link MapEditor}.
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    class RenderLayerDescriptor {
        
        String              title;
        
        int                 zPriority;
        
        int                 opacity;
        
        boolean             isEdit;
        
        Set<ILayer>         layers = new HashSet();
        
        String              servicePath;

        RenderLayerDescriptor( String servicePath, boolean isEdit, int zPriority, int opacity ) {
            super();
            this.servicePath = servicePath;
            this.isEdit = isEdit;
            this.zPriority = zPriority;
            this.opacity = opacity;
            this.title = servicePath;
        }

        /**
         * Creates a sort key for the values of this descriptor. Sort: service
         * -> edit -> zPriority -> opacity.
         */
        String renderLayerKey() {
            StringBuilder result = new StringBuilder( 256 );
            result.append( hashCode() /*service.getIdentifier()*/ )
                    .append( isEdit ? "1" : "0" ).append( zPriority ).append( opacity );
            return result.toString();
        }

    }
    
}
