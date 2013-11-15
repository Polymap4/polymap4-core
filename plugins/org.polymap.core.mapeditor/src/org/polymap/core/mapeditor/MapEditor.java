/* 
 * polymap.org
 * Copyright (C) 2009-2013 Falko Bräutigam. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.mapeditor.RenderManager.RenderLayerDescriptor;
import org.polymap.core.mapeditor.contextmenu.ContextMenuControl;
import org.polymap.core.mapeditor.tooling.ToolingModel;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.base_types.Projection;
import org.polymap.openlayers.rap.widget.base_types.Size;
import org.polymap.openlayers.rap.widget.controls.Control;
import org.polymap.openlayers.rap.widget.controls.LoadingPanelControl;
import org.polymap.openlayers.rap.widget.controls.MousePositionControl;
import org.polymap.openlayers.rap.widget.controls.NavigationHistoryControl;
import org.polymap.openlayers.rap.widget.controls.PanZoomBarControl;
import org.polymap.openlayers.rap.widget.controls.ScaleControl;
import org.polymap.openlayers.rap.widget.controls.ScaleLineControl;
import org.polymap.openlayers.rap.widget.layers.Layer;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * A map editor based on {@link OpenLayersWidget}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class MapEditor
        extends EditorPart
        implements IEditorPart, IAdaptable, OpenLayersEventListener {

    static Log log = LogFactory.getLog( MapEditor.class );

    /**
     * 
     *
     * @param map
     * @param createIfAbsent
     * @throws PartInitException
     */
    public static MapEditor openMap( IMap map, boolean createIfAbsent ) throws PartInitException {
        assert map != null;
        MapEditorInput input = new MapEditorInput( map );

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        
        // check current editors
        MapEditor result = null;
        for (IEditorReference reference : page.getEditorReferences()) {
            IEditorInput candidate = reference.getEditorInput();
            if (candidate.equals( input )) {
                result = (MapEditor)reference.getPart( true );
                page.activate( result );
            }
        }

        // not found -> open new editor
        if (result == null && createIfAbsent) {
            result = (MapEditor)page.openEditor( input, input.getEditorId(), true, IWorkbenchPage.MATCH_NONE );
        }
        return result;
    }

    
    // instance *******************************************
    
    private IMap                    map;
    
    private Composite               composite;

    protected OpenLayersWidget      olwidget;

    private RenderManager           renderManager;
    
    /** The currently displayed layers. Sorted in layer zIndex order. */
    protected Map<RenderLayerDescriptor,Layer> layers = new TreeMap();
    
    private MapEditorOverview       overview;
    
    private NavigationHistory       naviHistory;
    

    public void init( IEditorSite _site, IEditorInput _input )
            throws PartInitException {
        setSite( _site );
        setInput( _input );
        this.map = ((MapEditorInput)_input).getMap();
        setPartName( map.getLabel() );
        naviHistory = new NavigationHistory( this );
        
        // initialize my ToolingModel so that at least navigation is enabled
        ToolingModel.instance( this );
    }


    public void dispose() {
        log.debug( "dispose: ..." );
        if (overview != null) {
            overview.dispose();
            overview = null;
        }
        if (renderManager != null) {
            renderManager.dispose();
            renderManager = null;
        }
        if (olwidget != null) {
            olwidget.dispose();
            olwidget = null;
        }
        if (naviHistory != null) {
            naviHistory.dispose();
            naviHistory = null;
        }
    }


    public void createPartControl( Composite parent ) {
        composite = new Composite( parent, SWT.NONE /*_p3:SWT.EMBEDDED | SWT.NO_BACKGROUND*/ );
        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        composite.setLayout( layout );
        composite.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );

        parent.addControlListener( new ControlListener() {
            public void controlResized( ControlEvent ev ) {
                olwidget.getMap().updateSize();
            }
            public void controlMoved( ControlEvent ev ) {
            }
        });

        createWidget();
        
        // renderManager
        renderManager = new RenderManager( map, this );
        renderManager.updatePipelines();

        // restore additional input state
        ((MapEditorInput)getEditorInput()).restoreMapEditor();
    }

    
    public OpenLayersWidget getWidget() {
        return olwidget;
    }


    protected void createWidget() {
        // the widget (use internally provided OpenLayers lib)
        olwidget = new OpenLayersWidget( composite, SWT.MULTI | SWT.WRAP, "openlayers/full/OpenLayers-2.12.1.js" );
        olwidget.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        // projection
        String crsCode = map.getCRSCode();
        log.info( "### CRS: " + crsCode );
        Projection proj = new Projection( crsCode );
        String units = crsCode.equals( "EPSG:4326" ) ? "degrees" : "m";
        float maxResolution = crsCode.equals( "EPSG:4326" ) ? (360/256) : 500000;
        ReferencedEnvelope bbox = map.getMaxExtent();
        log.info( "### maxExtent: " + bbox );
        Bounds maxExtent = bbox != null
                ? new Bounds( bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() )
                : null;
        olwidget.createMap( proj, proj, units, maxExtent, maxResolution );

        // add some controls to the map
        OpenLayersMap olmap = olwidget.getMap();
        olmap.setMaxScale( 100 );
        
        olmap.addControl( new LoadingPanelControl() );
        
//        olmap.addControl( new LayerSwitcherControl() );
        olmap.addControl( new PanZoomBarControl() );
        olmap.addControl( new MousePositionControl() );
        olmap.addControl( new NavigationHistoryControl() );
        // OL >= 2.12 seems to catch each and every keyboard event
      // olmap.addControl( new KeyboardDefaultsControl() );

        olmap.addControl( new ScaleLineControl() );
        olmap.addControl( new ScaleControl() );

        ContextMenuControl contextMenu = new ContextMenuControl( this );
        getSite().setSelectionProvider( contextMenu );
//        olmap.addControl( contextMenu );
//        contextMenu.activate();
        
        // map events
        HashMap<String, String> payload = new HashMap<String, String>();
        payload.put( "left", "event.object.getExtent().toArray()[0]" );
        payload.put( "bottom", "event.object.getExtent().toArray()[1]" );
        payload.put( "right", "event.object.getExtent().toArray()[2]" );
        payload.put( "top", "event.object.getExtent().toArray()[3]" );
        olmap.events.register( this, OpenLayersMap.EVENT_MOUSE_OVER, payload );
        olmap.events.register( this, OpenLayersMap.EVENT_MOVEEND, payload );
        
        // empty base layer
        VectorLayer baseLayer = new VectorLayer( "[Base]" );
        baseLayer.setIsBaseLayer( true );
        olmap.addLayer( baseLayer );
        
        olmap.zoomToExtent( maxExtent, false );

        overview = new MapEditorOverview( this );
    }

    
    /*
     * Processes events triggered by the OpenLayers map. 
     */
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
        if (olwidget.getMap() != obj) {
            return;
        }
        // mouse over
        if (OpenLayersMap.EVENT_MOUSE_OVER.equals( name )) {
            try {
                final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                if (page.getActivePart() != this) {
                    Display.getCurrent().asyncExec( new Runnable() {
                        public void run() {
                            page.activate( MapEditor.this );
                        }
                    });
                }
            }
            catch (Exception e) {
                log.error( "", e );
            }
        }
        // map zoom/pan
        String left = payload.get( "left" );
        if (left != null) {
            try {
                ReferencedEnvelope bbox = new ReferencedEnvelope(
                        Double.parseDouble( payload.get( "left" ) ),
                        Double.parseDouble( payload.get( "right" ) ),
                        Double.parseDouble( payload.get( "bottom" ) ),
                        Double.parseDouble( payload.get( "top" ) ),
                        map.getCRS() );
                log.debug( "### process_event: bbox= " + bbox );
                ReferencedEnvelope old = map.getExtent();
                if (!bbox.equals( old )) {
                    map.updateExtent( bbox );
                }
            }
            catch (Exception e) {
                log.error( "unhandled:", e );
            }
        }
    }


    public IMap getMap() {
        return map;
    }


    public void addControl( Control control ) {
        olwidget.getMap().addControl( control );
    }

    public void removeControl( Control control ) {
        olwidget.getMap().removeControl( control );
    }

    public void addLayer( Layer olayer ) {
        olwidget.getMap().addLayer( olayer );
    }
    
    public void removeLayer( Layer olayer ) {
        olwidget.getMap().removeLayer( olayer );
    }
    
    public Layer findLayer( ILayer search ) {
        for (Map.Entry<RenderLayerDescriptor,Layer> entry: layers.entrySet()) {
            if (search.equals( entry.getKey().layer )) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public Set<RenderLayerDescriptor> layers() {
        return layers.keySet();
    }
    
    /**
     * Fills the {@link #olwidget} with the internal servers of the
     * {@link #renderManager}.
     */
    void addLayer( RenderLayerDescriptor descriptor ) {
        assert descriptor != null;
        assert !layers.containsKey( descriptor );

        WMSLayer olayer = new WMSLayer( descriptor.title(), 
                descriptor.servicePath, descriptor.layer.getRenderKey() );
        olayer.setFormat( "image/png" );
        olayer.setVisibility( true );
        olayer.setIsBaseLayer( false );
//        olayer.setSingleTile( true );
        olayer.setTileSize( new Size( 400, 400 ) );
        olayer.setBuffer( 0 );
        olayer.setOpacity( ((double)descriptor.opacity) / 100 );
        olayer.setTransitionEffect( Layer.TRANSITION_RESIZE );
        
        layers.put( descriptor, olayer );

        olwidget.getMap().addLayer( olayer );
        int layerIndex = Iterables.indexOf( layers.keySet(), Predicates.equalTo( descriptor ) );
        olwidget.getMap().setLayerIndex( olayer, layerIndex );

        //overview.addLayer( olLayer );

        if (overview != null) {
            overview.addLayer( descriptor );
        }
    }
    
    
    /**
     * 
     */
    void removeLayer( RenderLayerDescriptor descriptor ) {
        assert descriptor != null;
        assert layers.containsKey( descriptor );

        if (overview != null) {
            overview.removeLayer( descriptor );
        }

        Layer olayer = layers.remove( descriptor );
        olwidget.getMap().removeLayer( olayer );
    }
   
   
    public void reloadLayer( RenderLayerDescriptor descriptor ) {
        assert descriptor != null;
        assert layers.containsKey( descriptor );

        // force next SimpleWmsServer to use a new, unique layer key
        // to prevent old content to be shown
        descriptor.layer.updateRenderKey();
        removeLayer( descriptor );
        addLayer( descriptor );
        
//        Layer ollayer = layers.get( descriptor );
//        ollayer.redraw();
        
//        if (ollayer instanceof WMSLayer) {
//            ((WMSLayer)ollayer).redraw( true );
//        }
//        else {
//            ollayer.redraw();
//        }
    }
   

    public void setLayerOpacity( RenderLayerDescriptor descriptor, int opacity ) {
        assert descriptor != null;
        assert layers.containsKey( descriptor );
        
        Layer ollayer = layers.get( descriptor );
        ollayer.setOpacity( ((double)opacity) / 100 );
    }


    public void setLayerZPriority( RenderLayerDescriptor descriptor, int zPriority ) {
        assert descriptor != null;
        assert layers.containsKey( descriptor );
        
        Layer ollayer = layers.get( descriptor );
        ollayer.setZIndex( zPriority );
        log.debug( "ollayer=" + ollayer + ", zPriority=" + zPriority );
    }


    public void setMapExtent( ReferencedEnvelope bbox ) {
        log.debug( "mapExtent: " + bbox );
        assert bbox != null : "bbox == null";
        try {
            //log.debug( "median(0)=" + bbox.getMedian( 0 ) + ", median(1)=" + bbox.getMedian( 1 ) );
            Bounds extent = new Bounds( bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() );
            olwidget.getMap().zoomToExtent( extent, false );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
    }

    
    public void setMaxExtent( ReferencedEnvelope bbox ) {
        log.debug( "maxExtent: " + bbox );
        assert bbox != null : "bbox == null";
        try {
            //log.debug( "median(0)=" + bbox.getMedian( 0 ) + ", median(1)=" + bbox.getMedian( 1 ) );
            Bounds extent = new Bounds( bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() );
            olwidget.getMap().setMaxExtent( extent );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
    }

    
    /**
     * FIXME does not work
     */
    public void updateMapCRS() {
        OpenLayersMap olmap = olwidget.getMap();
        olmap.events.unregister( this, OpenLayersMap.EVENT_MOUSE_OVER );
        olmap.events.unregister( this, OpenLayersMap.EVENT_MOVEEND );
        
        olwidget.dispose();
        
        createWidget();
    }
    
    
    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
        if (!composite.isDisposed()) {
            composite.setFocus();
            //        updateCRS();
            //        updateScaleLabel();
        }
    }

    public void doSave( IProgressMonitor monitor ) {
        throw new RuntimeException( "not yet implemented." );
//        try {
//            log.debug( "..." );
//            layers.get( vectorLayer );
//            FinishEditLayerOperation op = new FinishEditLayerOperation( vectorLayer, true );
//
//            OperationSupport.instance().execute( op, false, false );
//        }
//        catch (Exception e) {
//            log.error( e.getMessage(), e );
//            MessageBox mbox = new MessageBox( 
//                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
//                    SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL );
//            mbox.setMessage( "Fehler: " + e.toString() );
//            mbox.setText( "Fehler bei der Operation." );
//            mbox.open();
//        }

    }

    public void doSaveAs() {
        throw new RuntimeException( "not yet implemented." );
    }


//    /**
//     * Returns {@link #findSupport(Class)}.
//     */
//    public Object getAdapter( Class adapter ) {
//        return findSupport( adapter );
//    }
    

    /**
     * Sets the application defined property of the receiver with the specified name
     * to the given value. See {@link Widget#setData(String,Object)} for more detail.
     * <p/>
     * Applications may associate arbitrary objects with the receiver in this
     * fashion. If the objects stored in the properties need to be notified when the
     * widget is disposed of, it is the application's responsibility to hook the
     * Dispose event on the widget and do so.
     * 
     * @see Composite#setData(String,Object) 
     * @see #getData(Class)
     */
    public void setData( Object value ) {
        composite.setData( value.getClass().getName(), value );
    }

    public <T> T getData( Class<T> cl ) {
        return (T)composite.getData( cl.getName() );
    }
    
    public void clearData( Class cl ) {
        composite.setData( cl.getName(), null );
    }


    public NavigationHistory getNaviHistory() {
        return naviHistory;
    }
    
}
