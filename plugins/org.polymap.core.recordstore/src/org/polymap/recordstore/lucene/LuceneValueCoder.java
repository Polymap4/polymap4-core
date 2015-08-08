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
package org.polymap.recordstore.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import org.polymap.recordstore.QueryExpression;

/**
 * A value coder is responsible for encode/decode a particular field value type.
 * The idea is to decouple type specific encode/decode code from the load/store
 * in order to make it easier to maintain code and in order to be able to plug in
 * coders for different types. 
 * <p/>
 * Implementations have to be thread-safe.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface LuceneValueCoder {

    /**
     * Encode the given value, removing any previously stored value.
     * 
     * @param doc
     * @param key
     * @param value
     * @param indexed
     * @return True, if the value was store, false if this coder does not handle the
     *         given value type.
     */
    public boolean encode( Document doc, String key, Object value, boolean indexed );
   
    public Object decode( Document doc, String key );
    
    public Query searchQuery( QueryExpression exp );
    
}
