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
package org.polymap.core.model2.runtime;

import java.util.Collection;

import org.polymap.core.model2.Entity;

/**
 * Represents a query for the given {@link Entity} type.  
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Query<T extends Entity> {

    public Collection<T> execute();
    
//    /**
//     * Set the ordering rules. If many segments are used for ordering then they will
//     * be applied in order.
//     * 
//     * @param segments the segments to order by
//     * @return this
//     */
//    Query<T> orderBy( OrderBy... segments );

    /**
     * Set the index of the first result. Default is 0 (zero).
     *
     * @return this
     */
    public Query<T> firstResult( int firstResult );

    
    /**
     * Set how many results should be returned. Default is that there is no limit
     * set.
     * 
     * @return this
     */
    public Query<T> maxResults( int maxResults );

//    /**
//     * Set the value of a named variable.
//     *
//     * @return this
//     */
//    public Query<T> setVariable( String name, Object value );
//
//    /**
//     * Get the value of a named variable.
//     *
//     * @return value of the variable
//     */
//    public <V> V getVariable( String name );

    /**
     * Get the result type of this Query
     *
     * @return the result type
     */
    public Class<T> resultType();

}
