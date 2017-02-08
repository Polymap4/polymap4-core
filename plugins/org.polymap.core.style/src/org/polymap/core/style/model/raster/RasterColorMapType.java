/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.style.model.raster;

/**
 * The ColorMap type attribute specifies the kind of ColorMap to use. There are
 * three different types of ColorMaps that can be specified: ramp, intervals and
 * values.
 *
 * @see <a href=
 *      "http://docs.geoserver.org/stable/en/user/styling/sld/reference/rastersymbolizer.html">GeoServer
 *      doc</a>
 * @author Falko Bräutigam
 */
public enum RasterColorMapType {
    /**
     * Is the default ColorMap type. It specifies that colors should be interpolated
     * for values between the color map entries. The result is shown in the following
     * example.
     */
    RAMP,
    /**
     * Means that only pixels with the specified entry quantity values are rendered.
     * Pixels with other values are not rendered.
     */
    INTERVALLS,
    /**
     * Means that each interval defined by two entries is rendered using the color of
     * the first (lowest-value) entry. No color interpolation is applied across the
     * intervals.
     */
    VALUES
}
