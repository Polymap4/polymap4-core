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
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import java.beans.PropertyChangeEvent;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;
import org.osgi.service.http.NamespaceException;

import org.apache.commons.lang.StringUtils;
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

import org.polymap.core.CorePlugin;
import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.mapeditor.services.SimpleWmsServer;
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
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.service.ServicesPlugin;
import org.polymap.service.http.MapHttpServer;

/**
 * The RenderManager is the bridge between an {@link IMap} and the {@link MapEditor}
 * that displays the contents of this map. It listens to all kind of events regarding
 * its map and changes its {@link #mapEditor} as needed.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class RenderManager {

    private static Log log = LogFactory.getLog( RenderManager.class );

    private IMap                    map;
    
    private MapEditor               mapEditor;

    private MapHttpServer           wmsServer;
    
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
        EventFilter eventFilter = new EventFilter<EventObject>() {
            public boolean apply( EventObject ev ) {
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
        module.addEntityListener( modelListener, eventFilter );
        
        // feature listener
        EventManager em = EventManager.instance();
        em.subscribe( featureListener, new EventFilter<FeatureChangeEvent>() {
            public boolean apply( FeatureChangeEvent ev ) {
                return RenderManager.this.map != null 
                        && ev.getSource() != null
                        && RenderManager.this.map.equals( ev.getSource().getMap() );
            }
        });
    }

    
    public void dispose() {
        clearPipelines();
        if (modelListener != null && map != null) {
            ProjectRepository module = ProjectRepository.instance();
            module.removeEntityListener( modelListener );
            modelListener = null;
        }
        if (featureListener != null) {
            EventManager.instance().unsubscribe( featureListener );
            featureListener = null;
        }
        if (wmsServer != null) {
            destroyWms( wmsServer );
            wmsServer = null;
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

        if (wmsServer != null) {
            destroyWms( wmsServer );
            wmsServer = null;
        }
        wmsServer = createWms();
        
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
                            StringUtils.removeStart( wmsServer.getPathSpec(), "/" ), 
                            false, layer.getOrderKey(), layer.getOpacity() );
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

    
    protected SimpleWmsServer createWms() {
        try {
            ISettingStore settingStore = RWT.getSettingStore();
            String wmsNamesAttr = settingStore.getAttribute( "RenderManager.wmsnames" );
            JSONObject wmsNames = wmsNamesAttr != null
                    ? new JSONObject( wmsNamesAttr ) : new JSONObject();

            String pathSpec = wmsNames.optString( map.id(), null );
            if (pathSpec == null) {
                pathSpec = ServicesPlugin.createServicePath( map.getLabel() + "--" + System.currentTimeMillis() );
                wmsNames.put( map.id(), pathSpec );
                settingStore.setAttribute( "RenderManager.wmsnames", wmsNames.toString() );
            }
        
            log.info( "Service path: " + pathSpec );

            // use our own WMS impl
            SimpleWmsServer result = new SimpleWmsServer();
            result.init( map );

            try {
                CorePlugin.registerServlet( pathSpec, result, null );
            }
            // session logged out without closing all services 
            catch (NamespaceException e) {
                CorePlugin.unregister( pathSpec );
                CorePlugin.registerServlet( pathSpec, result, null );
            }

            log.debug( "    URL: " + result.getPathSpec() );
            return result;
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "Fehler beim Starten des WMS-Services.", e );
            throw new RuntimeException( e );
        }
    }
    
    
    protected void destroyWms( MapHttpServer server ) {
        CorePlugin.unregister( server );
    }
    
    
    /**
     * 
     */
    class FeatureListener {
        
        @EventHandler(delay=750, display=true)
        public void featureChanges( List<FeatureChangeEvent> events ) {
            Set<ILayer> dirty = new HashSet( events.size() );
            for (FeatureChangeEvent ev : events) {
                dirty.add( ev.getSource() );
            }
            for (ILayer layer : dirty) {
                RenderLayerDescriptor descriptor = findDescriptorForLayer( layer );
                if (descriptor != null) {
                    mapEditor.reloadLayer( descriptor );
                }
            }
        }
    }
    
    
    /**
     * 
     */
    class MapDomainListener
            implements IModelChangeListener {

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
            // check CRS changes after an operation has has finished and
            // extends are transformed in new CRS
            Iterable<PropertyChangeEvent> crsEvents = ev.events( new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent iev ) {
                    return iev.getSource() instanceof IMap 
                            && IMap.PROP_CRSCODE.equalsIgnoreCase( iev.getPropertyName() );
                }
            });
            if (!Iterables.isEmpty( crsEvents )) {
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
        }

        @EventHandler(delay=500, display=true)
        public void propertyChange( List<PropertyChangeEvent> events ) {
            boolean updatePipelines = false;
            
            for (PropertyChangeEvent ev : events) {
                // ILayer
                if (ev.getSource() instanceof ILayer) {
                    ILayer layer = (ILayer)ev.getSource();
                    RenderLayerDescriptor descriptor = findDescriptorForLayer( layer );

                    if ("visible".equals( ev.getPropertyName() )) {
                        updatePipelines = true;
                    }
                    else if ("edit".equals( ev.getPropertyName() )) {
                        updatePipelines = true;
                    }
                    else if (ILayer.PROP_OPACITY.equals( ev.getPropertyName() )) {
                        if (descriptor != null && descriptor.layers.size() == 1) {
                            mapEditor.setLayerOpacity( descriptor, layer.getOpacity() );
                        }
                        else {
                            updatePipelines = true;
                        }
                    }
                    else if (ILayer.PROP_ORDERKEY.equals( ev.getPropertyName() )) {
                        if (descriptor != null && descriptor.layers.size() == 1) {
                            mapEditor.setLayerZPriority( descriptor, layer.getOrderKey() );
                        }
                        else {
                            updatePipelines = true;
                        }
                    }
                    else if (ILayer.PROP_STYLE.equals( ev.getPropertyName() )) {
                        if (descriptor != null) {
                            mapEditor.reloadLayer( descriptor );
                        }
                    }
                    else if (PipelineHolder.PROP_PROCS.equals( ev.getPropertyName() )) {
                        if (descriptor != null) {
                            updatePipelines = true;
                        }
                    }
                }
                // IMap
                else if (ev.getSource() instanceof IMap) {
                    // check if map was deleted
                    if (PropertyChangeSupport.PROP_ENTITY_REMOVED .equals( ev.getPropertyName() )) {
                        closeMapEditor();
                    }
                    else if (IMap.PROP_LAYERS.equals( ev.getPropertyName() )) {
                        updatePipelines = true;
                    }
                    else if (IMap.PROP_EXTENT.equals( ev.getPropertyName() )) {
                        ReferencedEnvelope extent = (ReferencedEnvelope)ev.getNewValue();
                        mapEditor.setMapExtent( extent );

                        // XXX refactor this out to MapEditor so that it can be used elsewhere 
                        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        IWorkbenchPage page = window.getActivePage();
                        if (page != null && mapEditor != null 
                                && page.findEditor( mapEditor.getEditorInput() ) != null) {
                            page.activate( mapEditor );
                        }
                    }
                    else if (IMap.PROP_MAXEXTENT.equals( ev.getPropertyName() )) {
                        mapEditor.setMapExtent( map.getMaxExtent() );
                        mapEditor.setMaxExtent( map.getMaxExtent() );
                    }
                    else if (IMap.PROP_CRSCODE.equalsIgnoreCase( ev.getPropertyName() )) {
                        // stop listening to events as extents and CRS do not match any longer;
                        // wait for ModelChangeEvent to reload
                        ProjectRepository module = ProjectRepository.instance();
                        // FIXME
                        log.warn( "!!! commented out: module.removePropertyChangeListener( modelListener );" );
                    }
                }
            }
            if (updatePipelines) {
                updatePipelines();
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
