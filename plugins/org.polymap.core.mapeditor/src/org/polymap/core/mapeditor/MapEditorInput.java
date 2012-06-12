/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH, and individual contributors
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
package org.polymap.core.mapeditor;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import org.eclipse.core.runtime.IAdaptable;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class MapEditorInput
        implements IEditorInput, IPersistableElement, IElementFactory {

    private static Log log = LogFactory.getLog( MapEditorInput.class );

    public static final String  FACTORY_ID = "org.polymap.core.mapeditor.MapEditorInputFactory";

    private IMap                map;
    
    private ReferencedEnvelope  extent;
    
    private List<ILayer>        visibleLayers;
    
    
    public MapEditorInput( IMap map ) {
        super();
        if (map == null) {
            throw new IllegalArgumentException( "map is null!" );
        }
        this.map = map;
    }


    public void saveState( IMemento memento ) {
        if (map != null) {
            try {
                memento.putString( "mapId", map.id() );
                ReferencedEnvelope mapExtent = map.getExtent();
                if (mapExtent != null) {
                    JSONObject json = new JSONObject();
                    json.put( "minx", mapExtent.getMinX() );
                    json.put( "maxx", mapExtent.getMaxX() );
                    json.put( "miny", mapExtent.getMinY() );
                    json.put( "maxy", mapExtent.getMaxY() );
                    json.put( "srs", CRS.toSRS( mapExtent.getCoordinateReferenceSystem() ) );
                    memento.putString( "mapExtent", json.toString() );
                }

                JSONArray json = new JSONArray();
                for (ILayer layer : map.getLayers()) {
                    if (layer.isVisible()) {
                        json.put( layer.id() );
                    }
                }
                memento.putString( "visibleLayers", json.toString() );
            }
            catch (Exception e) {
                log.warn( "Unable to save state.", e );
            }
        }
    }


    /**
     * Creates the factory instance that is used to {@link #createElement(IMemento)}.
     */
    public MapEditorInput() {
    }


    /**
     * Implements {@link IElementFactory}: initialize a new instance from settings in
     * the memento. This is called after no-args ctor.
     */
    public IAdaptable createElement( final IMemento memento ) {
        String mapId = memento.getString( "mapId" );
        if (mapId != null) {
            try {
                final IMap _map = ProjectRepository.instance().findEntity( IMap.class, mapId );
                if (_map != null) {
                    MapEditorInput result = new MapEditorInput( _map );

                    // extent
                    String mapExtentJson = memento.getString( "mapExtent" );
                    if (mapExtentJson != null) {
                        JSONObject json = new JSONObject( mapExtentJson );
                        final CoordinateReferenceSystem crs = CRS.decode( json.getString( "srs" ) );
                        result.extent = new ReferencedEnvelope( 
                                json.getDouble( "minx" ), json.getDouble( "maxx" ),
                                json.getDouble( "miny" ), json.getDouble( "maxy" ), crs )
                                .transform( _map.getCRS(), true );
                    }

                    // layer visibility
                    String visibleLayersJson = memento.getString( "visibleLayers" );
                    if (visibleLayersJson != null) {
                        result.visibleLayers = new ArrayList(); 
                        JSONArray json2 = new JSONArray( visibleLayersJson );
                        for (int i=0; i<json2.length(); i++) {
                            try {
                                result.visibleLayers.add( ProjectRepository.instance().findEntity( 
                                        ILayer.class, json2.getString( i ) ) );
                            }
                            catch (NoSuchEntityException e) {
                                log.warn( "Layer does no longer exists: " + e.getLocalizedMessage() );
                            }
                        }
                    }
                    return result;                    
                }
            }
            catch (NoSuchEntityException e) {
                log.warn( "Element does no longer exists: " + e.getLocalizedMessage() );
            }
            catch (Exception e) {
                log.warn( "Unable to restore FormEditorInput.", e );
            }
        }
        return null;
    }

    
    public void restoreMapEditor() {
        try {
            // extent
            if (extent != null) {
                map.setExtent( map.getMaxExtent().covers( extent )
                        ? extent : map.getMaxExtent() );
            }
            // layer visibility
            if (visibleLayers != null) {
                for (ILayer layer : visibleLayers) {
                    layer.setVisible( true );
                }
            }
        }
        catch (Exception e) {
            log.warn( "Unable to restore FormEditorInput.", e );
        }
    }

    
    public String getFactoryId() {
        return FACTORY_ID;
    }


    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof MapEditorInput) {
            return ((MapEditorInput)obj).map.equals( map );
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return map.hashCode();
    }

    public IMap getMap() {
        return map;
    }

    public String getEditorId() {
        return "org.polymap.core.mapeditor.MapEditor";
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "MapEditorInput";
    }

    public IPersistableElement getPersistable() {
        return this;
    }

    public String getToolTipText() {
        return map.getLabel();
    }

    public Object getAdapter( Class adapter ) {
        if (adapter.isAssignableFrom( map.getClass() )) {
            return map;
        }
        return null;
    }

}
