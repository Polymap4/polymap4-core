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
package org.polymap.core.style.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.ConstantFilter;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.ScaleRangeFilter;
import org.polymap.core.style.model.StyleRepository;
import org.polymap.core.style.serialize.sld.SLDSerializer;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * @author Steffen Stundzig
 */
public class FilterTest {

    private static Log log = LogFactory.getLog( FilterTest.class );

    private static StyleRepository repo;

    private static FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        repo = new StyleRepository( null );
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (repo != null) {
            repo.close();
        }
    }


    @Test
    public void serialization() throws Exception {

        Filter filter = ff.equals( ff.property( "prop" ), ff.literal( "literal" ) );
        String encoded = ConstantFilter.encode( filter );
        Filter decoded = ConstantFilter.decode( encoded );

        assertEquals( filter, decoded );
        assertEquals( "[ prop = literal ]", decoded.toString() );
    }


    @Test
    public void visiblePoint() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        assertTrue( point.visibleIf.get() instanceof ConstantFilter );

        point.visibleIf.createValue( initializeFilter( ff.equals( ff.property( "prop" ), ff.literal( "literal" ) ) ) );
        point.diameter.createValue( ConstantNumber.defaults( 23.0 ) );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        org.geotools.styling.Style style = repo.serializedFeatureStyle( fs.id(), org.geotools.styling.Style.class )
                .get();
        
        Rule rule = style.getFeatureTypeStyles()[0].getRules()[0];
        assertTrue(rule.getFilter() instanceof PropertyIsEqualTo);
        PropertyIsEqualTo filter = (PropertyIsEqualTo)rule.getFilter();
        assertTrue(filter.getExpression1() instanceof PropertyName);
        assertEquals("prop", ((PropertyName)filter.getExpression1()).getPropertyName());
        assertTrue(filter.getExpression2() instanceof Literal);
        assertEquals("literal", ((Literal)filter.getExpression2()).getValue());
        PointSymbolizer sym = (PointSymbolizer)rule.getSymbolizers()[0];
        assertEquals( SLDSerializer.ff.literal( 23.0 ), sym.getGraphic().getSize() );
    }

    @SuppressWarnings("deprecation")
    @Test
    public void pointWithScale() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        assertTrue( point.visibleIf.get() instanceof ConstantFilter );

        point.visibleIf.createValue( ScaleRangeFilter.defaults(10000, 500000) );
        point.diameter.createValue( ConstantNumber.defaults( 23.0 ) );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        org.geotools.styling.Style style = repo.serializedFeatureStyle( fs.id(), org.geotools.styling.Style.class )
                .get();
        
        Rule rule = style.getFeatureTypeStyles()[0].getRules()[0];
        assertEquals(10000.0d, rule.getMinScaleDenominator(), 0);
        assertEquals(500000.0d, rule.getMaxScaleDenominator(), 0);
        PointSymbolizer sym = (PointSymbolizer)rule.getSymbolizers()[0];
        assertEquals( SLDSerializer.ff.literal( 23.0 ), sym.getGraphic().getSize() );
    }


    private ValueInitializer<ConstantFilter> initializeFilter( Filter filter ) {
        return new ValueInitializer<ConstantFilter>() {

            @Override
            public ConstantFilter initialize( ConstantFilter proto ) throws Exception {
                proto.setFilter( filter );
                return proto;
            }
        };
    }
}
