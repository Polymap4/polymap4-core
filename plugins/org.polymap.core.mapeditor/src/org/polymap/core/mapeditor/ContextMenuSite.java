/* 
 * polymap.org
 * Copyright 2012-2014, Polymap GmbH. All rights reserved.
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
import java.util.List;
import java.util.Map;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.filter.FilterFactory2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Point;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.batik.internal.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ContextMenuSite {

    private static Log log = LogFactory.getLog( ContextMenuSite.class );

    public static final IMessages       i18n = Messages.forPrefix( "ContextMenu" );
    
    public static final FilterFactory2  ff = CommonFactoryFinder.getFilterFactory2( null );
    
    /** Maps layer id into already computed covered features for this layer. */
    private Map<String,List<Feature>>   coveredFeatures = new HashMap();
    
    private ReferencedEnvelope          mapExtent = getMapViewer().getMapExtent();
    
    private Point                       mapSize = getMapViewer().getControl().getSize();
    
    private Point                       mousePos = Polymap.getSessionDisplay().getCursorLocation();
    
    private Point                       widgetMousePos = Polymap.getSessionDisplay().getCursorControl().toControl( mousePos );
    
    
    public abstract MapViewer getMapViewer();


    /**
     * The covered bounding box in map coordinates.
     * 
     * @return Newly created bounding box that covers the right click/drag.
     */
    public ReferencedEnvelope boundingBox() {
        Point pos = widgetMousePosition();
        int delta = 2;
        double minx = mapExtent.getMinX() + (mapExtent.getWidth() * (pos.x-delta) / mapSize.x);
        double maxx = mapExtent.getMinX() + (mapExtent.getWidth() * (pos.x+delta) / mapSize.x);
        double miny = mapExtent.getMinY() + (mapExtent.getHeight() * ((mapSize.y-pos.y)-delta) / mapSize.y);
        double maxy = mapExtent.getMinY() + (mapExtent.getHeight() * ((mapSize.y-pos.y)+delta) / mapSize.y);

        return new ReferencedEnvelope( minx, maxx, miny, maxy, getMapViewer().getCRS() );
    }
    
    
    public Point getMapSize() {
        return mapSize;
    }

    
    public ReferencedEnvelope getMapExtent() {
        return mapExtent;
    }


    public Point globalMousePosition() {
        return mousePos;
    }

    
    public Point widgetMousePosition() {
        return widgetMousePos; 
    }

}
