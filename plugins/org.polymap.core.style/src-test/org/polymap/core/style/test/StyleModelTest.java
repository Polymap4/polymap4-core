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

import static org.junit.Assert.assertTrue;
import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.ConstantColor;
import org.polymap.core.style.model.ConstantFilter;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.FilterMappedNumbers;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.StyleRepository;

/**
 * 
 *
 * @author Falko Bräutigam
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
    public void test() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();
        
        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );
        assertTrue( point.activeIf.get() instanceof ConstantFilter );
        
        point.fillColor.createValue( ConstantColor.defaults( 0, 0, 0 ) );
        point.fillOpacity.createValue( ConstantNumber.defaults( 1.0 ) );
        point.strokeColor.createValue( ConstantColor.defaults( 100, 100, 100 ) );
        point.strokeWidth.createValue( ConstantNumber.defaults( 5.0 ) );
        point.strokeOpacity.createValue( FilterMappedNumbers.defaults() )
                .add( 0.1, ff.equals( ff.literal( 1 ), ff.literal( 1 ) ) )
                .add( 0.2, ff.equals( ff.literal( 2 ), ff.literal( 2 ) ) );
        
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );

        point.strokeOpacity.createValue( ConstantNumber.defaults( 1.0 ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }
    
}
