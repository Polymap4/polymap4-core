/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.feature;

import java.util.Iterator;
import java.util.Set;

import org.opengis.filter.identity.FeatureId;

import org.polymap.core.data.pipeline.ProcessorResponse;

/**
 * The response of {@link AddFeaturesRequest}, {@link ModifyFeaturesRequest} and
 * {@link RemoveFeaturesRequest}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ModifyFeaturesResponse
        implements ProcessorResponse, Iterable<FeatureId> {

    private Set<FeatureId>       ids;


    public ModifyFeaturesResponse( Set<FeatureId> ids ) {
        this.ids = ids;
    }

    public int count() {
       return ids.size();    
    }
    
    public Set<FeatureId> getFeatureIds() {
        return ids;
    }

    /**
     * 
     * @return An iterator over the elements of this feature chunk. In contrast
     *         to <code>FeatureIterator</code> this iterator does not need to be
     *         closed.
     */
    public Iterator<FeatureId> iterator() {
        return ids.iterator();
    }
    
}
