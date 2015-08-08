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
package org.polymap.recordstore;

import java.io.IOException;


/**
 * Basic query interace.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RecordQuery {

    /** The default value returned by {@link #getMaxResults()}. Defaults to 10. */
    public static final int     DEFAULT_MAX_RESULTS = 10;
    
    public static final char    DEFAULT_ANY_WILDCARD = '*';
    
    public static final char    DEFAULT_ONE_WILDCARD = '?';
    
    /** Sort order: ascending */
    public static final int     ASC = 0;
    
    public static final int     DESC = 1;
    
    private int                 maxResults = DEFAULT_MAX_RESULTS;
    
    private int                 firstResult;

    private String              sortKey;

    private int                 sortOrder;

    private Class               sortType;
    
    private IRecordFieldSelector fieldSelector = IRecordFieldSelector.ALL;
    
    /**
     * 
     *
     * @throws IOException
     */
    public abstract ResultSet execute() throws IOException;
    
    
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

    /**
     * Specify the sort order of the result.
     * 
     * @param key Key to sort by.
     * @param order {@link #ASC} or {@link #DESC}
     * @param type Optional type of the field to sort.
     */
    public RecordQuery sort( String key, int order, Class type ) {
        sortKey = key;
        sortOrder = order;
        sortType = type;
        return this;
    }
    
    public String getSortKey() {
        return sortKey;
    }

    public int getSortOrder() {
        return sortOrder;
    }
    
    public Class getSortType() {
        return sortType;
    }

    public IRecordFieldSelector getFieldSelector() {
        return fieldSelector;
    }
    
    public void setFieldSelector( IRecordFieldSelector fieldSelector ) {
        this.fieldSelector = fieldSelector;
    }

}
