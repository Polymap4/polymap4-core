/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.catalog;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class MetadataQuery
        extends Configurable {
    
    @DefaultInt( Integer.MAX_VALUE )
    public Config2<MetadataQuery,Integer>   maxResults;

    
    public abstract ResultSet execute() throws Exception;
    
    
    /**
     * The result set of a {@link MetadataQuery#execute()}.
     * <p/>
     * Sub-classes should override {@link #finalize()} and call {@link #close()}. 
     */
    public static interface ResultSet
            extends Iterable<IMetadata>, AutoCloseable {
    
        public int size();

        @Override
        public void close();
        
        @Override
        default Iterator<IMetadata> iterator() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        public default Stream<IMetadata> stream() {
            return StreamSupport.stream( spliterator(), false );
        }

    }
    
}
