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
package org.polymap.core.model2.engine;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.runtime.Query;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class QueryImpl<T extends Entity>
        implements Query<T> {

    public Class<T>             resultType;

    public Object               expression;

    public int                  firstResult = 0;

    public int                  maxResults = Integer.MAX_VALUE;

    
    public QueryImpl( Class<T> resultType, Object expression ) {
        this.resultType = resultType;
        this.expression = expression;
    }

    @Override
    @SuppressWarnings("hiding")
    public Query<T> firstResult( int firstResult ) {
        this.firstResult = firstResult;
        return this;
    }

    @Override
    @SuppressWarnings("hiding")
    public Query<T> maxResults( int maxResults ) {
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public Class<T> resultType() {
        return resultType;
    }

}
