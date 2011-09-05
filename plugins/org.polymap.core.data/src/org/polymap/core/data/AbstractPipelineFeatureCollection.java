/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.util.ProgressListener;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.collection.AbstractFeatureCollection;

/**
 * Provides {@link CollectionEven} and {@link FeatureListener} handling methods.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
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

    protected void fireChange( CollectionEvent ev ) {
        for (int i = 0, ii = listeners.size(); i < ii; i++) {
            ((CollectionListener)listeners.get(i)).collectionChanged( ev );
        }
    }

    protected void fireChange(SimpleFeature[] features, int type) {
        fireChange( new CollectionEvent( this, features, type ) );
    }

    protected void fireChange(SimpleFeature feature, int type) {
        fireChange( new SimpleFeature[] { feature }, type );
    }

    public void changed( FeatureEvent featureEvent ) {
        log.debug( "changed(): ev=" + featureEvent );
        int type = CollectionEvent.FEATURES_CHANGED;
        switch (featureEvent.getType()) {
            case ADDED:
                type = CollectionEvent.FEATURES_ADDED;
                break;
            case CHANGED:
                type = CollectionEvent.FEATURES_CHANGED;
                break;
            case REMOVED:
                type = CollectionEvent.FEATURES_REMOVED;
                break;
        }
        SimpleFeature[] features = new SimpleFeature[] {};
        fireChange( new CollectionEvent( this, features, type ) );
    }

}
