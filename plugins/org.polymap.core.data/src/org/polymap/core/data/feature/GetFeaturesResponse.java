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
package org.polymap.core.data.feature;

import java.util.Iterator;
import java.util.List;

import org.opengis.feature.Feature;

import org.polymap.core.data.pipeline.ProcessorResponse;

/**
 * The response of the {@link GetFeaturesRequest}. Several chunks of features
 * are send in response of the request.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GetFeaturesResponse
        implements ProcessorResponse, Iterable<Feature> {

    private List<Feature>       features;


    public GetFeaturesResponse( List<Feature> features ) {
        this.features = features;
    }

    public int count() {
       return features.size();    
    }
    
    public List<Feature> getFeatures() {
        return features;
    }

    /**
     * 
     * @return An iterator over the elements of this feature chunk. In contrast
     *         to <code>FeatureIterator</code> this iterator does not need to be
     *         closed.
     */
    public Iterator<Feature> iterator() {
        return features.iterator();
    }
    
}
