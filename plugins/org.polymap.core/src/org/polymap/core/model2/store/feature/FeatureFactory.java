/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.store.feature;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

/**
 * For mysterious (at least for me) reasons the {@link FeatureSource} interface does not
 * provide a way to create features inside the store :(  
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface FeatureFactory {

    /**
     * Factory for new, empty {@link Feature} instances. The newly created features
     * needs to be added via
     * {@link FeatureStore#addFeatures(org.geotools.feature.FeatureCollection)} in
     * order to get persistently stored.
     * 
     * @return Newly created, empty feature instance.
     */
    public Feature newFeature();
    
}
