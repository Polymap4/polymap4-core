/*
 * polymap.org Copyright 2011, Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.data.feature.buffer;

import java.util.Collection;
import java.util.List;

import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

/**
 * The SPI of a feature buffer. A buffer tracks changes of features. In contrast to
 * {@link IFeatureCache} a buffer must no loose any changes it holds until the buffer
 * is flushed.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFeatureBuffer {

    public void dispose()
    throws Exception;


    public void clear()
    throws Exception;


    public boolean isEmpty()
    throws Exception;


    public Iterable<FeatureBufferState> content();


    /**
     * Returns the feature state of the given identifier if this feature is held by
     * the buffer.
     * 
     * @param identifier
     * @return The buffered feature state, or null if the buffer does not contain
     *         this feature.
     */
    public FeatureBufferState contains( FeatureId identifier );


    /**
     * Register a feature and its original state with this buffer. Ensures that the
     * original state of the given features are stored in the buffer.
     * 
     * @param collection The original state of the features.
     */
    public void registerFeatures( Collection<Feature> collection );


    public boolean supports( Filter filter );


    public List<FeatureId> markAdded( Collection<Feature> features )
    throws Exception;
    
    
    public List<FeatureId> markModified( Filter filter, AttributeDescriptor[] type, Object[] value )
    throws Exception;


    public void markRemoved( Collection<Feature> features )
    throws Exception;


    /**
     * Adapt the given features according to the buffered modifyed and removed
     * features.
     * 
     * @param query
     * @param features The original set of features.
     * @return The adapted features.
     * @throws Exception
     */
    public List<Feature> blendFeatures( Query query, Iterable<Feature> features )
    throws Exception;

    
    public List<Feature> addedFeatures( Filter filter )
    throws Exception;

    
    public int featureSizeDifference( Query query )
    throws Exception;


    public void addFeatureChangeListener( IFeatureChangeListener l );
    
    public void removeFeatureChangeListener( IFeatureChangeListener l );



}
