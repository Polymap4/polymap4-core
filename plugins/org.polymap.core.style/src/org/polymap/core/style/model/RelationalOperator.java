/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.model;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.expression.Expression;

/**
 * Commonly used relational operators for the conditional stylers.
 * https://en.wikipedia.org/wiki/Relational_operator
 *
 * @author Steffen Stundzig
 */
public enum RelationalOperator {
    
    eq("="), neq("!="), gt(">"), lt("<"), ge(">="), le("<=");

    private String value;

    public static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );

    RelationalOperator( final String value ) {
        this.value = value;
    }


    public String value() {
        return value;
    }


    public static RelationalOperator forValue( String currentValue ) {
        for (RelationalOperator family : values()) {
            if (family.value.equals( currentValue )) {
                return family;
            }
        }
        return null;
    }


    public Filter asFilter( final Expression left, final Expression right ) {
        switch (this) {
            case le:
                return ff.lessOrEqual( left, right );
            case eq:
                return ff.equals( left, right );
            case ge:
                return ff.greaterOrEqual( left, right );
            case gt:
                return ff.greater( left, right );
            case lt:
                return ff.less( left, right );
            case neq:
                return ff.notEqual( left, right );
            default:
                throw new RuntimeException( "unknown operator " + this.name() );
        }
    }


    public static RelationalOperator forFilter( final BinaryComparisonOperator bco ) {
        // matcher
        if (bco instanceof PropertyIsEqualTo) {
            return RelationalOperator.eq;
        }
        if (bco instanceof PropertyIsGreaterThan) {
            return RelationalOperator.gt;
        }
        if (bco instanceof PropertyIsGreaterThanOrEqualTo) {
            return RelationalOperator.ge;
        }
        if (bco instanceof PropertyIsLessThan) {
            return RelationalOperator.lt;
        }
        if (bco instanceof PropertyIsLessThanOrEqualTo) {
            return RelationalOperator.le;
        }
        if (bco instanceof PropertyIsNotEqualTo) {
            return RelationalOperator.neq;
        }
        throw new RuntimeException( "unknown operator " + bco.getClass() );
    }
}
