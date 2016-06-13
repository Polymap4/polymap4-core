/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld;

import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import java.util.List;

import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

import com.google.common.collect.Lists;

import org.polymap.core.style.model.PropertyNumber;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.FeatureStyleSerializer.OutputFormat;

/**
 * @author Steffen Stundzig
 */
public class PropertyNumberHandler
        extends StylePropertyValueHandler<PropertyNumber,Object> {

    @Override
    public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, PropertyNumber spv, SD sd,
            StylePropertyValueHandler.Setter<SD> setter ) {
        List<SD> result = Lists.newArrayList();
        if (context.outputFormat.get().equals( OutputFormat.GEOSERVER )) {
            Expression exp = ff.property( (String)spv.propertyName.get() );
            if (spv.minimumValue.get() != null) {
                exp = ff.function( "max", exp, ff.literal( spv.minimumValue.get() ) );
            }
            if (spv.maximumValue.get() != null) {
                exp = ff.function( "min", exp, ff.literal( spv.maximumValue.get() ) );
            }
            setter.set( sd, exp );
            result.add( sd );
        }
        else {
            // return OGC standard
            Expression exp = ff.property( (String)spv.propertyName.get() );
            Double minimum = (Double)spv.minimumValue.get();
            Double maximum = (Double)spv.maximumValue.get();
            if (minimum == null && maximum == null) {
                // no filter required
                setter.set( sd, exp );
                result.add( sd );
            }
            else {
                List<Filter> filters = Lists.newArrayList();
                if (minimum != null) {
                    SD sdMin = (SD)sd.clone();
                    sdMin.filterAnd( ff.lessOrEqual( exp, ff.literal( minimum ) ) );
                    setter.set( sdMin, ff.literal( minimum ) );
                    result.add( sdMin );
                    filters.add( ff.greater( exp, ff.literal( minimum ) ) );
                }
                if (maximum != null) {
                    SD sdMax = (SD)sd.clone();
                    sdMax.filterAnd( ff.greaterOrEqual( exp, ff.literal( maximum ) ) );
                    setter.set( sdMax, ff.literal( maximum ) );
                    result.add( sdMax );
                    filters.add( ff.less( exp, ff.literal( maximum ) ) );
                }
                setter.set( sd, exp );
                sd.filterAnd( ff.and( filters ) );
                result.add( sd );
            }
        }
        return result;
    }
}