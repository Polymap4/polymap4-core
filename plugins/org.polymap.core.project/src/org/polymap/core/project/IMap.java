/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.eclipse.core.runtime.IAdaptable;

import org.polymap.model2.Computed;
import org.polymap.model2.Concerns;
import org.polymap.model2.Defaults;
import org.polymap.model2.Entity;
import org.polymap.model2.ManyAssociation;
import org.polymap.model2.Mixins;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.event.PropertyChangeSupport;

/**
 * A Map contains Maps and Layers. It holds information about the rendering of the
 * Services of the Layers.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@Concerns({
    PropertyChangeSupport.class
})
@Mixins({
    Node.class,
    Labeled.class,
    Visible.class,
    //ACL.class
})
public class IMap
        extends Entity { 

    @Defaults
    public ManyAssociation<ILayer>      layers;

    public ManyAssociation<IMap>        children;

    public Property<String>             srsCode;

//    @ModelProperty(PROP_CRSCODE)
//    public void setCRSCode( String code )
//            throws NoSuchAuthorityCodeException, FactoryException, TransformException;
//    
//    public CoordinateReferenceSystem getCRS();


    /**
     * The current extent of the map. This fires a property event. If the
     * map is displayed then the visual representation is refreshed reflecting
     * the new extend. 
     */
    public Property<EnvelopeComposite>  extent;

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
    
    
    public <T> T visit( LayerVisitor<T> visitor );

}
