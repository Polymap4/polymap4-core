/*
 * polymap.org Copyright (C) 2016-2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.ui;

import java.util.Optional;

import java.lang.reflect.ParameterizedType;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.FeatureStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Envelope;

import org.eclipse.swt.graphics.Point;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.Property;

/**
 * 
 * @author Steffen Stundzig
 * @author Falko Bräutigam
 */
public class StylePropertyFieldSite
        extends Configurable {

    /**
     * The {@link Property} of the Style that contains the {@link StylePropertyValue}
     * this editor is working on.
     */
    @Mandatory
    public Config<Property<StylePropertyValue>> prop;

    /** Optional: present if layer is connected to a feature data source. */
    public Config<FeatureStore>                 featureStore;

    /** Optional: present if layer is connected to a feature data source. */
    public Config<FeatureType>                  featureType;
    
    /** Optional: present if layer is connected to a raster data source. */
    public Config<GridCoverage2D>               gridCoverage;
    
    /** Optional: the current extent of the map. */
    public Config<Envelope>                     mapExtent;

    /** Optional: the maximum extent of the map. */
    public Config<ReferencedEnvelope>           maxExtent;

    /** Optional: the maximum extent of the map. */
    public Config<Point>                        mapSize;
    
    /**
     * Returns the <b>declared</b> type of the given property:
     * <pre>
     * Property&lt;StylePropertyValue&lt;Number&gt;&gt; -> Number
     * </pre>
     */
    public Lazy<Class>                          targetType = new PlainLazyInit( () -> {
        assert StylePropertyValue.class.isAssignableFrom( prop.get().info().getType() );
        Optional<ParameterizedType> o = prop.get().info().getParameterizedType();
        ParameterizedType p = o.orElseThrow( () -> 
                new RuntimeException( "StylePropertyValue has no type parameter: " + prop.toString() ) );
        return (Class)p.getActualTypeArguments()[0];
    });

    /**
     * 
     *
     * @see #targetType
     */
    public <R> Class<R> targetType() {
        return targetType.get();
    }
}
