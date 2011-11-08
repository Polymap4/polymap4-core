/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.recordstore;

/**
 * Basic query interace.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RecordQuery {

    public static final int DEFAULT_MAX_RESULTS = 10;
    
    private int             maxResults = DEFAULT_MAX_RESULTS;
    
    private int             firstResult;

    
    public int getMaxResults() {
        return maxResults;
    }

    public RecordQuery setMaxResults( int maxResults ) {
        this.maxResults = maxResults;
        return this;
    }
    
    public int getFirstResult() {
        return firstResult;
    }
    
    public RecordQuery setFirstResult( int firstResult ) {
        this.firstResult = firstResult;
        return this;
    }
    
}
