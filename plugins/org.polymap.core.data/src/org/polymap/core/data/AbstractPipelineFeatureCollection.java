/*
 * polymap.org
 * Copyright (C) 2009-2016, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.util.ProgressListener;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.feature.collection.AbstractFeatureCollection;

/**
 * Provides {@link CollectionEven} and {@link FeatureListener} handling methods.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class AbstractPipelineFeatureCollection
        extends AbstractFeatureCollection
        implements PipelineFeatureCollection<SimpleFeatureType, SimpleFeature>, FeatureListener {

    private static final Log log = LogFactory.getLog( AbstractPipelineFeatureCollection.class );

    protected ProgressListener        progressListener;


    protected AbstractPipelineFeatureCollection( SimpleFeatureType memberType ) {
        super( memberType );
    }

    
    public ProgressListener setProgressListener( ProgressListener l ) {
        ProgressListener old = progressListener;
        progressListener = l;
        return old;
    }

    
    @Override
    public void changed( FeatureEvent featureEvent ) {
    }

}
