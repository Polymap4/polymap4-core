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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import com.google.common.collect.Lists;

import org.polymap.core.style.model.ExpressionMappedValues;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.FeatureStyleSerializer.OutputFormat;

/**
 * @author Steffen Stundzig
 */
public class ExpressionMappedValuesHandler
        extends StylePropertyValueHandler<ExpressionMappedValues,Object> {

    @Override
    public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ExpressionMappedValues spv, SD sd,
            StylePropertyValueHandler.Setter<SD> setter ) {
        List<SD> result = Lists.newArrayList();

        Expression propertyName = ff.property( (String)spv.propertyName.get() );
        Object defaultValue = spv.defaultValue();
        Collection<Expression> expressions = spv.expressions();
        Iterator<Expression> expressionsIterator = expressions.iterator();
        Iterator<Object> values = spv.values().iterator();
        boolean simpleExpressions = true;

        while (expressionsIterator.hasNext()) {
            Expression next = expressionsIterator.next();
            if (!(next instanceof Literal || next instanceof PropertyName)) {
                simpleExpressions = false;
                break;
            }
        }
        if (context.outputFormat.get().equals( OutputFormat.GEOSERVER )) {

            expressionsIterator = expressions.iterator();
            if (simpleExpressions) {
                List<Expression> recode = Lists.newArrayList();
                recode.add( propertyName );
                while (expressionsIterator.hasNext()) {
                    assert values.hasNext();
                    Expression expression = expressionsIterator.next();
                    Object value = values.next();
                    // ife = ff.function( "if_then_else", ff.function( "equalTo",
                    // property, expression ), ff.literal( value ),
                    // ife );
                    recode.add( expression );
                    recode.add( ff.literal( value ) );
                }
                if (defaultValue != null) {
                    // finally, the property matches itself
                    // workaround since no default value could be specified in recode
                    recode.add( propertyName );
                    recode.add( ff.literal( defaultValue ) );
                }
                Expression ife = ff.function( "recode", recode.toArray( new Expression[0] ) );
                setter.set( sd, ife );
                result.add( sd );
            }
            else {
                Expression ife = defaultValue != null ? ff.literal( defaultValue ) : Expression.NIL;
                while (expressionsIterator.hasNext()) {
                    assert values.hasNext();
                    Expression expression = expressionsIterator.next();
                    Object value = values.next();
                    ife = ff.function( "if_then_else", expression, ff.literal( value ), ife );
                }
                setter.set( sd, ife );
                result.add( sd );
            }
        }
        else {
            if (simpleExpressions) {
            List<Filter> filters = Lists.newArrayList();
            while (expressionsIterator.hasNext()) {
                assert values.hasNext();
                Expression expression = ff.literal( expressionsIterator.next() );
                Object value = values.next();

                SD sdNew = (SD)sd.clone();
                sdNew.filterAnd( ff.equals( propertyName, expression ) );
                setter.set( sdNew, ff.literal( value ) );
                result.add( sdNew );
                filters.add( ff.notEqual( propertyName, expression ) );
            }
            if (defaultValue != null) {
                sd.filterAnd( ff.and( filters ) );
                setter.set( sd, ff.literal( defaultValue ) );
                result.add( sd );
            }
            } else {
                throw new RuntimeException("complex expressions not supported");
            }
        }
        return result;
    }
}