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
package org.polymap.core.style.serialize.sld.feature;

import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import java.util.Iterator;
import java.util.List;

import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import com.google.common.collect.Lists;

import org.polymap.core.style.model.feature.FilterMappedValues;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.FeatureStyleSerializer.OutputFormat;
import org.polymap.core.style.serialize.sld.StylePropertyValueHandler;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * @author Steffen Stundzig
 */
public class FilterMappedValuesHandler
        extends StylePropertyValueHandler<FilterMappedValues,Object> {

    @Override
    public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, FilterMappedValues spv, SD sd,
            Setter<SD> setter ) {
        List<SD> result = Lists.newArrayList();

        List<Filter> filters = spv.filters();
        if (filters.isEmpty()) {
            return result;
        }
        List<Object> values = spv.values();
        if (context.outputFormat.get().equals( OutputFormat.GEOSERVER )) {
            // try to optimize
            boolean recodeAllowed = true;
            boolean categorizeAllowed = true;
            String propertyName = null;
            List<String> literals = Lists.newArrayList();
            for (int i = 0; i < filters.size() && (recodeAllowed || categorizeAllowed); i++) {
                Filter filter = filters.get( i );
                if (filter instanceof BinaryComparisonOperator) {
                    BinaryComparisonOperator piet = (BinaryComparisonOperator)filter;
                    if (propertyName == null) {
                        propertyName = ((PropertyName)piet.getExpression1()).getPropertyName();
                    }
                    literals.add( ((Literal)piet.getExpression2()).getValue().toString() );
                }
                if (i == 0) {
                    if (!(filter instanceof PropertyIsEqualTo)) {
                        recodeAllowed = false;
                    }
                    if (!(filter instanceof PropertyIsLessThanOrEqualTo)) {
                        categorizeAllowed = false;
                    }
                }
                else if (i < filters.size() - 1) {
                    if (!(filter instanceof PropertyIsEqualTo)) {
                        recodeAllowed = false;
                    }
                    if (!(filter instanceof PropertyIsLessThan)) {
                        categorizeAllowed = false;
                    }
                }
                else if (i == filters.size() - 1) {
                    if (!(filter instanceof And)) {
                        recodeAllowed = false;
                    }
                    if (!(filter instanceof PropertyIsGreaterThanOrEqualTo)) {
                        categorizeAllowed = false;
                    }
                }
            }
            if (recodeAllowed) {
                result.add( createRecodeSD( context, sd, setter, literals, values, propertyName ) );
                return result;
            }
            if (categorizeAllowed) {
                // remove the last greater than
                literals.remove( literals.size() - 1 );
                result.add( createCategorizeSD( context, sd, setter, literals, values, propertyName ) );
                return result;
            }
        }

        Iterator<Object> valueIterator = values.iterator();
        Iterator<Filter> filterIterator = filters.iterator();
        while (filterIterator.hasNext()) {
            assert valueIterator.hasNext();

            SD clone = (SD)sd.clone();

            clone.filterAnd( filterIterator.next() );
            setter.set( clone, ff.literal( valueIterator.next() ) );
            result.add( clone );
        }

        return result;
    }


    private <SD extends SymbolizerDescriptor> SD createRecodeSD( Context context, SD sd,
            org.polymap.core.style.serialize.sld.StylePropertyValueHandler.Setter<SD> setter, List<String> literals,
            List<Object> values, String propertyName ) {
        List<Expression> recode = Lists.newArrayList();
        recode.add( ff.property( propertyName ) );
        Iterator<String> literalIterator = literals.iterator();
        Iterator<Object> valueIterator = values.iterator();
        while (literalIterator.hasNext()) {
            assert valueIterator.hasNext();
            String literal = literalIterator.next();
            Object value = valueIterator.next();
            recode.add( ff.literal( literal ) );
            recode.add( ff.literal( value ) );
        }
        // if one more value exists, this is the default
        if (valueIterator.hasNext()) {
            Object value = valueIterator.next();
            recode.add( ff.property( propertyName ) );
            recode.add( ff.literal( value ) );
        }
        setter.set( sd, ff.function( "recode", recode.toArray( new Expression[0] ) ) );
        return sd;
    }


    private <SD extends SymbolizerDescriptor> SD createCategorizeSD( Context context, SD sd,
            org.polymap.core.style.serialize.sld.StylePropertyValueHandler.Setter<SD> setter, List<String> literals,
            List<Object> values, String propertyName ) {
        List<Expression> categorize = Lists.newArrayList();
        categorize.add( ff.property( propertyName ) );
        Iterator<String> literalIterator = literals.iterator();
        Iterator<Object> valueIterator = values.iterator();
        while (literalIterator.hasNext()) {
            assert valueIterator.hasNext();
            String literal = literalIterator.next();
            Object value = valueIterator.next();
            categorize.add( ff.literal( value ) );
            categorize.add( ff.literal( literal ) );
        }
        Object value = valueIterator.next();
        categorize.add( ff.literal( value ) );
        setter.set( sd, ff.function( "categorize", categorize.toArray( new Expression[0] ) ) );
        return sd;
    }
}
