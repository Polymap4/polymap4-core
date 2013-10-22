/* 
 * polymap.org
 * Copyright (C) 2011-2013, Falko Bräutigam. All rigths reserved.
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
package org.polymap.core.runtime.mp;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Provides the interface for executors that actually run an {@link ForEach} loop.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ForEachExecutor<S,T>
        extends Iterator<T> {
    
    void setChunkSize( int chunkSize );


    /**
     * 
     */
    interface Factory {
        public ForEachExecutor newExecutor( ForEach foreach );
    }
    
    
    /**
     * 
     */
    interface ProcessorContext {
        public void put( Chunk chunk );
    }

    
    /**
     * 
     */
    static class Chunk {
        
        int         chunkNum;
        
        ArrayList   elements;

        public Chunk( ArrayList elements, int chunkNum ) {
            this.elements = elements;
            this.chunkNum = chunkNum;
        }
        
    }

}