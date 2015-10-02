/* 
 * polymap.org
 * Copyright 2012-2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.recordstore;

import java.io.IOException;

import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;

import org.polymap.recordstore.IRecordState;
import org.polymap.recordstore.IRecordStore;

/**
 * Defines the SPI of a query dialect. A query dialect is reponsibly of handling the
 * database/backend specific query functions of a {@link RFeatureStore}.
 * <p/>
 * Implementations must be thread-safe. As there is just one instance per
 * {@link RDataStore} implementation should be stateless in order to handle reentrant
 * calls efficiently.
 * 
 * @see RFeatureStore
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class QueryDialect {

    public abstract QueryCapabilities getQueryCapabilities();

    public abstract void initStore( IRecordStore store );
    
    public abstract int getCount( RFeatureStore fs, Query query ) throws IOException;

    public abstract ReferencedEnvelope getBounds( RFeatureStore fs, Query query ) throws IOException;
    
    public abstract PostProcessResultSet getFeatureStates( RFeatureStore fs, Query query ) throws IOException;

    /**
     * 
     */
    public interface PostProcessResultSet
            extends Iterable<IRecordState> {
        
        public boolean postProcess( Feature feature );
        
        public boolean hasPostProcessing();
        
        /**
         * The size of the {@link #iterator()} results - without post-processing!
         */
        public int size();
        
    }
    
}
