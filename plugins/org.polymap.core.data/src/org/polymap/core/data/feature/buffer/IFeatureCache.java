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
import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFeatureCache {

    public void dispose()
    throws Exception;


    public boolean isEmpty()
    throws Exception;


    public boolean supports( Filter filter );


    public Iterable<Feature> features( final Query query )
    throws Exception;


    public void putFeatures( Collection<Feature> features )
    throws Exception;

    
    public void addFeatures( Collection<Feature> features )
    throws Exception;
    
    
    public void modifyFeatures( Collection<Feature> features )
    throws Exception;
    
}