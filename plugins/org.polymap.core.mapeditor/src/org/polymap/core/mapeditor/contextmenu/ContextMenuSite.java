/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.contextmenu;

import java.util.HashMap;
import java.util.Map;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BBOX;

import org.eclipse.swt.graphics.Point;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.UIJob;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ContextMenuSite {
//        extends StructuredSelection
//        implements IStructuredSelection {

    public static final FilterFactory       ff = CommonFactoryFinder.getFilterFactory( null );
    
    /** Maps layer id into already computed covered features for this layer. */
    private Map<String,FeatureCollection>   coveredFeatures = new HashMap();
    
    private ReferencedEnvelope              mapExtent = getMap().getExtent();
    
    private Point                           mapSize = getMapEditor().getWidget().getSize();
    
    private Point                           mousePos = Polymap.getSessionDisplay().getCursorLocation();
    
    private Point                           widgetMousePos = Polymap.getSessionDisplay().getCursorControl().toControl( mousePos );
    
    
    public abstract MapEditor getMapEditor();


    public IMap getMap() {
        return getMapEditor().getMap();
    }

    
    public FeatureCollection coveredFeatures( final ILayer layer ) {
        FeatureCollection features = coveredFeatures.get( layer.id() );
        if (features == null) {
            UIJob job = new UIJob( "Filtering covered features" ) {
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    ReferencedEnvelope bbox = boundingBox();

                    ReferencedEnvelope transformed = bbox.transform( layer.getCRS(), true );

                    String propname = "";
                    String srs = CRS.toSRS( layer.getCRS() );

                    BBOX filter = ff.bbox( propname, transformed.getMinX(), transformed.getMinY(), 
                            transformed.getMaxX(), transformed.getMaxY(), srs );

                    PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
                    coveredFeatures.put( layer.id(), fs.getFeatures( filter ) );
                }
            };
            job.setUser( true );
            //job.setShowProgressDialog( "Filtering covered features...", false );
            job.schedule();
            
            try {
                job.join();
                features = coveredFeatures.get( layer.id() );
            }
            catch (InterruptedException e) {
                // no features
            }
        }
        return features;
    }
    
    
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

        return new ReferencedEnvelope( minx, maxx, miny, maxy, getMap().getCRS() );
    }
    
    
    public Point globalMousePosition() {
        return mousePos;
    }

    
    public Point widgetMousePosition() {
        return widgetMousePos; 
    }

}
