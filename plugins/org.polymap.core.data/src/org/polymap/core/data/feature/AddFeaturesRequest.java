/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.feature;

import java.util.Collection;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import org.polymap.core.data.pipeline.ProcessorRequest;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class AddFeaturesRequest
        implements ProcessorRequest {

    private FeatureType             type;
    
    private Collection<Feature>     features;


    public AddFeaturesRequest( FeatureType type, Collection<Feature> features ) {
        this.type = type;
        this.features = features;
    }

    /**
     * 
     * <p/>
     * The returned Collection may (or may not) fetch features directly from the
     * source of the features. So requesting the size and/or an iterator for the
     * collection might be an extensive operation.
     * 
     * @return Collection of features to add.
     */
    public Collection<Feature> getFeatures() {
        return features;
    }
    
}
