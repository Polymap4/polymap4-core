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
import java.util.List;

import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import com.google.common.collect.Lists;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class MyContentDataStore
        extends ContentDataStore {
    private final SimpleFeatureType simpleFeatureType;
    private final SimpleFeature simpleFeature;
    
    public MyContentDataStore(SimpleFeatureType simpleFeatureType, SimpleFeature simpleFeature) {
        this.simpleFeatureType = simpleFeatureType;
        this.simpleFeature = simpleFeature;
    }
    @Override
    protected List<Name> createTypeNames() throws IOException {
        return Lists.newArrayList( simpleFeatureType.getName() );
    }


    @Override
    protected ContentFeatureSource createFeatureSource( ContentEntry entry ) throws IOException {
        return new MyContentFeatureSource( entry, null, entries, simpleFeatureType, simpleFeature );
    }
}
