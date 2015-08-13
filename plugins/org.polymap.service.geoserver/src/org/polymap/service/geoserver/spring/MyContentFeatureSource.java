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
import java.util.Map;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;


/**
 * TODO: currently only supports one feature
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 */
@SuppressWarnings("unchecked")
public class MyContentFeatureSource extends ContentFeatureSource {
    private final Map<Name,ContentEntry> entries;
    private final SimpleFeatureType simpleFeatureType;
    private final SimpleFeature simpleFeature;

    /**
     * @param entry
     * @param query
     * @param entries 
     */
    public MyContentFeatureSource( ContentEntry entry, Query query, Map<Name,ContentEntry> entries, SimpleFeatureType simpleFeatureType, SimpleFeature simpleFeature) {
        super( entry, query );
        this.entries = entries;
        this.simpleFeatureType = simpleFeatureType;
        this.simpleFeature = simpleFeature;
    }


    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        entries.put( simpleFeatureType.getName(), entry );
        return simpleFeatureType;
    }


    @Override
    protected ReferencedEnvelope getBoundsInternal( Query arg0 ) throws IOException {
        return new ReferencedEnvelope();
    }


    @Override
    protected int getCountInternal( Query arg0 ) throws IOException {
        return 1;
    }


    @Override
    protected FeatureReader<SimpleFeatureType,SimpleFeature> getReaderInternal( Query arg0 )
            throws IOException {
        return new MyFeatureReader(simpleFeatureType, simpleFeature);
    }
}
