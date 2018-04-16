/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.style.serialize.sld2;

import java.util.List;
import java.util.stream.Collectors;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.SLD;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rits.cloning.Cloner;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.feature.LineStyle;
import org.polymap.core.style.model.feature.PointStyle;
import org.polymap.core.style.model.feature.PolygonStyle;
import org.polymap.core.style.model.feature.ShadowStyle;
import org.polymap.core.style.model.feature.TextStyle;
import org.polymap.core.style.model.raster.RasterColorMapStyle;
import org.polymap.core.style.model.raster.RasterGrayStyle;
import org.polymap.core.style.model.raster.RasterRGBStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer;

/**
 * Creates a {@link org.geotools.styling.Style GeoTools Style} out of a
 * {@link FeatureStyle} model.
 * <p/>
 * This is the second design of a SLD serializer (see {@link SLDSerializer}). This
 * implementation does not build an intermediate representation of
 * filter/symbolizers. Rather than it copies rules, fills filters and symbolizers and
 * creates combinations in one loop. This makes implementation much leaner and
 * simpler. It relies on the {@link Cloner} library for creating deep copies (because
 * clone() methods of the geotools styling classes are super buggy).
 * <p/>
 * <b>Transform the resulting Style into SLD:</b>
 * 
 * <pre>
 *   {@link SLDTransformer} styleTransform = new {@link SLDTransformer}();
 *   String xml = styleTransform.transform( style );
 * </pre>
 * 
 * @see SLDSerializer
 * @author Falko Bräutigam
 */
public class SLDSerializer2
        extends FeatureStyleSerializer<org.geotools.styling.Style> {

    private static final Log log = LogFactory.getLog( SLDSerializer2.class );

    public static final StyleFactory sf = CommonFactoryFinder.getStyleFactory( null );  //new DeepCopyStyleFactory();

    public static final FilterFactory2 ff = DataPlugin.ff;

    /**
     * Converts the given color into string in the form of <code>#rrggbb</code.
     * <p/>
     * {@link SLD#colorToHex(java.awt.Color)} is buggy. 
     */
    public static String toHexString( java.awt.Color c ) {
        return "#" + StringUtils.leftPad( Integer.toHexString( c.getRGB() & 0x00ffffff ), 6, '0' );
    }
    
    
    @Override
    public org.geotools.styling.Style serialize( Context context ) {
        FeatureStyle featureStyle = context.featureStyle.get();
        
        List<Style> sorted = featureStyle.members().stream()
                .sorted( (s1,s2) -> s1.zPriority.get().compareTo( s2.zPriority.get() ) )
                .collect( Collectors.toList() );
        
        org.geotools.styling.Style result = sf.createStyle();
        for (Style style : sorted) {
            if (style.active.get()) {
                StyleSerializer serializer = null;
                if (style instanceof PointStyle) {
                    serializer = new PointStyleSerializer( context );
                }
                else if (style instanceof PolygonStyle) {
                    serializer = new PolygonStyleSerializer( context );
                }
                else if (style instanceof TextStyle) {
                    serializer = new TextStyleSerializer( context );
                }
                else if (style instanceof LineStyle) {
                    serializer = new LineStyleSerializer( context );
                }
                else if (style instanceof ShadowStyle) {
                    serializer = new ShadowStyleSerializer( context );
                }
                else if (style instanceof RasterGrayStyle) {
                    serializer = new RasterSerializer.GraySerializer( context );
                }
                else if (style instanceof RasterRGBStyle) {
                    serializer = new RasterSerializer.RGBSerializer( context );
                }
                else if (style instanceof RasterColorMapStyle) {
                    serializer = new RasterSerializer.ColorMapSerializer( context );
                }
                else {
                    throw new RuntimeException( "Unhandled Style type: " + style.getClass().getName() );
                }
                serializer.serialize( style, result );
            }
        }
        return result;
    }

}
