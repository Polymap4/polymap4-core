/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.service.geoserver.spring;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * TODO: currently only supports one feature
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class MyFeatureReader implements FeatureReader<SimpleFeatureType,SimpleFeature> {
    private final SimpleFeatureType simpleFeatureType;
    private final SimpleFeature simpleFeature;
    
    public MyFeatureReader(SimpleFeatureType simpleFeatureType, SimpleFeature simpleFeature) {
        this.simpleFeatureType = simpleFeatureType;
        this.simpleFeature = simpleFeature;
    }

    private boolean first = true;


    @Override
    public void close() throws IOException {
    }


    @Override
    public SimpleFeatureType getFeatureType() {
        return simpleFeatureType;
    }


    @Override
    public boolean hasNext() throws IOException {
        return first;
    }


    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        first = false;
        return simpleFeature;
    }
}
