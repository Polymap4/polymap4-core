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

package org.polymap.core.project;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.core.runtime.IAdaptable;

import org.polymap.core.model.ACL;
import org.polymap.core.model.AssocCollection;
import org.polymap.core.model.Entity;
import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.TransientProperty;

/**
 * A Map contains Maps and Layers. It holds information about the rendering of
 * the Services of the Layers.
 * <p>
 * Setting of properties should be done inside Operations only.
 * <p>
 * Implementing class have to provide an {@link #equals(Object)} method.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface IMap
        extends Entity, Labeled, ACL, ParentMap, IAdaptable { 

    public static final String      PROP_MAPS = "maps";
    public static final String      PROP_LAYERS = "layers";
    public static final String      PROP_CRSCODE = "crscode";
    /** Fired when the map extent is changed from server side code. */
    public static final String      PROP_EXTENT = "extent";
    /** Fired when the map extent is changed from client side navigation event. */
    public static final String      PROP_EXTENT_UPDATE = "extent_update";
    public static final String      PROP_MAXEXTENT = "maxextent";
    public static final String      PROP_MAPSTATUS = "mapstatus";
    public static final String      PROP_RENDERSTATUS = "renderstatus";
    

//    public String toString();
    
    /**
     * The layers association.
     */
    public AssocCollection<ILayer> getLayers();

    @ModelProperty(PROP_LAYERS)
    public boolean addLayer( ILayer layer );
    
    @ModelProperty(PROP_LAYERS)
    public boolean removeLayer( ILayer layer );
    

    /**
     * The children maps association.
     */
    public AssocCollection<IMap> getMaps();

    @ModelProperty(PROP_MAPS)
    public boolean addMap( IMap map );
    
    @ModelProperty(PROP_MAPS)
    public boolean removeMap( IMap map );
    
    
    public String getCRSCode();

    @ModelProperty(PROP_CRSCODE)
    public void setCRSCode( String code )
            throws NoSuchAuthorityCodeException, FactoryException;
    
    public CoordinateReferenceSystem getCRS();


    /**
     * The extent property. 
     */
    public ReferencedEnvelope getExtent();

    /**
     * Sets the extent of the map. This fires a property event. If the
     * map is displayed then the visual representation is refreshed reflecting
     * the new extend. 
     */
    @TransientProperty(PROP_EXTENT)
    public ReferencedEnvelope setExtent( ReferencedEnvelope result );

    /**
     * Allow the map component to update the map extend to reflect the
     * actual bbox currently used in the map component.
     */
    @TransientProperty(PROP_EXTENT_UPDATE)
    public void updateExtent( ReferencedEnvelope extent );


    /**
     * The maxExtent property.
     * 
     * @return The envelop or null, if the map is empty and/or no extent was set
     *         yet.
     */
    public ReferencedEnvelope getMaxExtent();

    @ModelProperty(PROP_MAXEXTENT)
    public void setMaxExtent( ReferencedEnvelope result );

    
    /**
     * Indication of the general status.
     */
    public MapStatus getMapStatus();
    
    @TransientProperty(PROP_MAPSTATUS)
    public void setMapStatus( MapStatus status );


    /**
     * Indication of Layer status. This is used to provide feedback for
     * a Map's rendering status.
     */
    public RenderStatus getRenderStatus();
    
    @TransientProperty(PROP_RENDERSTATUS)
    public void setRenderStatus( RenderStatus status );
    
}
