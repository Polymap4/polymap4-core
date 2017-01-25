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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.FeatureStore;
import org.opengis.feature.type.FeatureType;

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

    @Mandatory
    public Config<Property<StylePropertyValue>> prop;

    /** Optional: present if layer is connected to a feature data source. */
    public Config<FeatureStore>                 featureStore;

    /** Optional: present if layer is connected to a feature data source. */
    public Config<FeatureType>                  featureType;
    
    /** Optional: present if layer is connected to a raster data source. */
    public Config<GridCoverage2D>               gridCoverage;
}
