/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.PipelineFeatureSource.FeatureResponseHandler;
import org.polymap.core.runtime.session.SessionContext;

/**
 * Not yet tested. 
 *
 * @author Falko Bräutigam
 */
public class PipelineSyncFeatureReader<T extends FeatureType, F extends Feature>
        implements FeatureReader<T,F> {

    private static final Log log = LogFactory.getLog( PipelineSyncFeatureReader.class );

    protected PipelineFeatureSource fs;
    
    protected Query                 query;

    private Iterator<Feature>       it;

    
    public PipelineSyncFeatureReader( PipelineFeatureSource fs, Query query, SessionContext sessionContext ) throws Exception {
        this.fs = fs;
        this.query = query;

        final List<Feature> buf = new ArrayList();
        fs.fetchFeatures( query, new FeatureResponseHandler() {
            @Override
            public void handle( List<Feature> features ) throws Exception {
                buf.addAll( features );
            }
            @Override
            public void endOfResponse() throws Exception {
            }
        });
        it = buf.iterator();
    }
    

    @Override
    public T getFeatureType() {
        return (T)fs.getSchema();
    }

    
    @Override
    public boolean hasNext() throws IOException {
        return it.hasNext();
    }


    @Override
    public F next() throws IOException, IllegalArgumentException, NoSuchElementException {
        return (F)it.next();
    }

    
    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }
    
}
