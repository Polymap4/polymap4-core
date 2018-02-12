/* 
 * polymap.org
 * Copyright (C) 2009-2016, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.image;

import java.util.HashMap;
import java.util.List;

import java.awt.Color;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.polymap.core.data.pipeline.ProcessorRequest;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GetMapRequest
        implements ProcessorRequest {

    private String                  crs;

    private ReferencedEnvelope      bbox;

    private String                  format = "image/png";

    private int                     width, height;

    private boolean                 transparent = true;

    private Color                   bgcolor = null;

    private HashMap<String, List<?>> dimensions = new HashMap<String, List<?>>();

    private double                  scale;

    private double                  pixelSize = 0.28;
    
    private List<String>            layers;

    private List<String>            styles;

//    private Map<LayerType,Interpolation> interpolations = new HashMap();
//
//    private Map<LayerType,Antialias> antialiases = new HashMap();
//
//    private Map<LayerType,Quality>   qualities = new HashMap();

    private long                     ifModifiedSince = -1;
    

    public GetMapRequest( List<String> layers, List<String> styles, String crs, ReferencedEnvelope bbox, 
            String format, int width, int height, long ifModifiedSince ) {
        this.layers = layers;
        this.styles = styles;
        this.crs = crs;
        this.bbox = bbox;
        this.format = format;
        this.width = width;
        this.height = height;
        this.ifModifiedSince = ifModifiedSince;
    }

    public GetMapRequest( GetMapRequest rhs ) {
        this.layers = rhs.layers;
        this.styles = rhs.styles;
        this.crs = rhs.crs;
        this.bbox = rhs.bbox;
        this.format = rhs.format;
        this.width = rhs.width;
        this.height = rhs.height;
        this.ifModifiedSince = rhs.ifModifiedSince;
    }

    
    public long getIfModifiedSince() {
        return ifModifiedSince;
    }

    /**
     * @return the coordinate system of the bbox
     */
    public String getCRS() {
        return crs;
    }

    public ReferencedEnvelope getBoundingBox() {
        return bbox;
    }

    public List<String> getLayers() {
        return layers;
    }

    public List<String> getStyles() {
        return styles;
    }

    /**
     * @return the image format
     */
    public String getFormat() {
        return format;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean getTransparent() {
        return transparent;
    }

    /**
     * @return the desired background color or null if not specified.
     */
    public Color getBgColor() {
        return bgcolor;
    }

    /**
     * @return returns a map with the requested dimension values
     */
    public HashMap<String, List<?>> getDimensions() {
        return dimensions;
    }

//    /**
//     * @param layer
//     * @param filter
//     */
//    public void addFilter( String layer, Filter filter ) {
//        if ( filters.get( layer ) != null ) {
//            Operator oldop = ( (OperatorFilter) filters.get( layer ) ).getOperator();
//            Operator snd = ( (OperatorFilter) filter ).getOperator();
//            filters.put( layer, new OperatorFilter( new And( oldop, snd ) ) );
//        } else {
//            filters.put( layer, filter );
//        }
//    }
//
//    /**
//     * @param name
//     * @param values
//     */
//    public void addDimensionValue( String name, List<?> values ) {
//        dimensions.put( name, values );
//    }
//
//    /**
//     * @param name
//     * @param filter
//     * @return a new filter for the layer, fulfilling the filter parameter as well
//     */
//    public Filter getFilterForLayer( String name, Filter filter ) {
//        if ( filter != null ) {
//            Filter extra = filters.get( name );
//            if ( extra != null ) {
//                Operator op = ( (OperatorFilter) extra ).getOperator();
//                Operator op2 = ( (OperatorFilter) filter ).getOperator();
//                return new OperatorFilter( new And( op, op2 ) );
//            }
//            return filter;
//        }
//        return filters.get( name );
//    }

    /**
     * @return the scale as WMS 1.3.0/SLD scale
     */
    public double getScale() {
        return scale;
    }

//    /**
//     * @return the quality settings for the layers
//     */
//    public Quality getQuality( LayerType layer) {
//        return qualities.get( layer );
//    }
//
//    void setQuality( LayerType layer, Quality quality ) {
//        qualities.put( layer, quality );
//    }
//    
//    /**
//     * @return the interpolation settings for the layers
//     */
//    public Interpolation getInterpolation( LayerType layer ) {
//        return interpolations.get( layer );
//    }
//
//    /**
//     * @return the antialias settings for the layers
//     */
//    public Antialias getAntialias( LayerType layer ) {
//        return antialiases.get( layer );
//    }

    /**
     * @return the value of the pixel size parameter (default is 0.28 mm).
     */
    public double getPixelSize() {
        return pixelSize;
    }

    /**
     * <code>Quality</code>
     */
    public static enum Quality {
        LOW,
        NORMAL,
        HIGH
    }

    /**
     * <code>Interpolation</code>
     */
    public static enum Interpolation {
        NEARESTNEIGHBOR,
        NEARESTNEIGHBOUR,
        BILINEAR,
        BICUBIC
    }

    /**
     * <code>Antialias</code>
     */
    public static enum Antialias {
        /***/
        IMAGE, /***/
        TEXT, /***/
        BOTH, /***/
        NONE
    }

}
