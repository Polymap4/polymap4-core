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
import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import org.geotools.styling.PointSymbolizer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.ConstantColor;
import org.polymap.core.style.model.ConstantFilter;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.ConstantString;
import org.polymap.core.style.model.ConstantStrokeCapStyle;
import org.polymap.core.style.model.ConstantStrokeDashStyle;
import org.polymap.core.style.model.ConstantStrokeJoinStyle;
import org.polymap.core.style.model.PropertyString;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.FilterMappedNumbers;
import org.polymap.core.style.model.LineStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.PolygonStyle;
import org.polymap.core.style.model.StyleRepository;
import org.polymap.core.style.model.TextStyle;
import org.polymap.core.style.serialize.sld.SLDSerializer;

/**
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class StyleModelTest {

    private static Log log = LogFactory.getLog( StyleModelTest.class );
    
    private static StyleRepository  repo;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        repo = new StyleRepository( null );
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (repo != null) { repo.close(); }
    }


    // instance *******************************************
    
    @Test
    public void testPoint() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();
        
        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );
        assertTrue( point.visibleIf.get() instanceof ConstantFilter );
        
        point.diameter.createValue( ConstantNumber.defaults( 10.0 ) );
        point.fill.get().color.createValue( ConstantColor.defaults( 0, 0, 0 ) );
        point.fill.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        point.stroke.get().color.createValue( ConstantColor.defaults( 100, 100, 100 ) );
        point.stroke.get().width.createValue( ConstantNumber.defaults( 5.0 ) );
        point.stroke.get().opacity.createValue( FilterMappedNumbers.defaults() )
                .add( 0.1, ff.equals( ff.literal( 1 ), ff.literal( 1 ) ) )
                .add( 0.2, ff.equals( ff.literal( 2 ), ff.literal( 2 ) ) );
        
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );

        point.stroke.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }

    @Test
    public void testFeatureBasedPoint() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();
        
        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );
        assertTrue( point.visibleIf.get() instanceof ConstantFilter );
        
        point.diameter.createValue( ConstantNumber.defaults( 23.0 ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        org.geotools.styling.Style style = repo.serializedFeatureStyle( fs.id(), org.geotools.styling.Style.class ).get();
        PointSymbolizer sym = (PointSymbolizer)style.getFeatureTypeStyles()[0].getRules()[0].getSymbolizers()[0];
        assertEquals( SLDSerializer.ff.literal( 23.0 ), sym.getGraphic().getSize());
        
//        point.diameter.createValue( FeaturePropertyBasedNumber.defaults("foo") );
//        fs.store();
//        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
//        sym = (PointSymbolizer)style.getFeatureTypeStyles()[0].getRules()[0].getSymbolizers()[0];
//        assertEquals( SLDSerializer.ff.property( "foo" ), sym.getGraphic().getSize());
    }
    
    @Test
    public void testPolygon() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();
        
        // point
        PolygonStyle polygon = fs.members().createElement( PolygonStyle.defaults );
        assertTrue( polygon.visibleIf.get() instanceof ConstantFilter );
        
        polygon.fill.get().color.createValue( ConstantColor.defaults( 1, 2, 3 ) );
        polygon.stroke.get().color.createValue( ConstantColor.defaults( 100, 100, 100 ) );
        polygon.stroke.get().width.createValue( ConstantNumber.defaults( 5.0 ) );
        polygon.stroke.get().opacity.createValue( FilterMappedNumbers.defaults() )
                .add( 0.1, ff.equals( ff.literal( 1 ), ff.literal( 1 ) ) )
                .add( 0.2, ff.equals( ff.literal( 2 ), ff.literal( 2 ) ) );
        polygon.stroke.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        polygon.stroke.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        polygon.stroke.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );
        
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }
    
    
    @Test
    public void testText() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();
        
        // point
        TextStyle text = fs.members().createElement( TextStyle.defaults );
        
        text.property.createValue( ConstantString.defaults( "constant" )  );
        // text.halo.createValue( Halo.defaults );
        text.halo.get().color.createValue( ConstantColor.defaults( 1, 2, 3 ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );

        text.property.createValue( PropertyString.defaults( "featureproperty" )  );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }

    @Test
    public void testLine() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        LineStyle line = fs.members().createElement( LineStyle.defaults );

        line.fill.get().color.createValue( ConstantColor.defaults( 0, 0, 100 ) );
        line.fill.get().width.createValue( ConstantNumber.defaults( 5.0 ) );
        line.fill.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        line.fill.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        line.fill.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        line.stroke.get().color.createValue( ConstantColor.defaults( 100, 0, 0 ) );
        line.stroke.get().width.createValue( ConstantNumber.defaults( 2.0 ) );
        line.stroke.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        line.stroke.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        line.stroke.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }
}
