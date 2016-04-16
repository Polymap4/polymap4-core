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

import javax.xml.transform.TransformerException;

import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.ConstantBoolean;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.StyleRepository;
import org.polymap.core.style.serialize.FeatureStyleSerializer;
import org.polymap.core.style.serialize.sld.SLDSerializer;

import org.polymap.model2.runtime.UnitOfWork;

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
    
    private UnitOfWork          uow;

    @Before
    public void setUp() throws Exception {
        uow = repo.newUnitOfWork();
    }

    @After
    public void tearDown() throws Exception {
        uow.close();
    }


    @Test
    public void test() throws TransformerException {
        FeatureStyle fs = uow.createEntity( FeatureStyle.class, null, FeatureStyle.defaults );
        
        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );
        assertTrue( point.active.get() instanceof ConstantBoolean );
        
        point.strokeWidth.createValue( ConstantNumber.defaults( 5 ) );
        
        // SLD
        Style sld = new SLDSerializer().serialize( new FeatureStyleSerializer.Context() {
            @Override
            public FeatureStyle featureStyle() { return fs; }
        });
        SLDTransformer styleTransform = new SLDTransformer();
        styleTransform.setIndentation( 4 );
        styleTransform.setOmitXMLDeclaration( false );
        String xml = styleTransform.transform( sld );
        log.info( "SLD: " + xml );
    }
    
}
