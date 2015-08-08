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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import org.polymap.recordstore.QueryExpression;

/**
 * Provides common base methods.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class ValueCoders {
    
    public static final LuceneValueCoder[] DEFAULT_CODERS = new LuceneValueCoder[] {
            new NumericValueCoder(),
            new BooleanValueCoder(),
            new BinaryValueCoder(),
            new DateValueCoder(),
            new StringValueCoder()
    };

    // instance *******************************************
    
    private LuceneRecordStore           store;
    
    private LuceneValueCoder[]          valueCoders = DEFAULT_CODERS;
    
    private Map<String,LuceneValueCoder> keyCoderMap = new HashMap( 64 );
    

    protected ValueCoders( LuceneRecordStore store ) {
        this.store = store;
    }


    public void addValueCoder( LuceneValueCoder valueCoder ) {
        // add first, keep StringValueCoder last
        valueCoders = ArrayUtils.add( valueCoders, 0, valueCoder );
    }

    
    public void removeValueCoder( LuceneValueCoder valueCoder ) {
        valueCoders = ArrayUtils.removeElement( valueCoders, valueCoder );
    }

    
    public void clear() {
        valueCoders = new LuceneValueCoder[0];    
    }
    
    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        LuceneValueCoder valueCoder = keyCoderMap.get( key );
        if (valueCoder != null) {
            valueCoder.encode( doc, key, value, indexed );
            return true;
        }
        for (LuceneValueCoder candidate : valueCoders) {
            if (candidate.encode( doc, key, value, indexed )) {
                keyCoderMap.put( key, candidate );
                return true;
            }
        }
        throw new RuntimeException( "No LuceneValueCoder found for value type: " + value.getClass() );
    }
    
    
    public final <T> T decode( Document doc, String key ) {
        if (key == null) {
            return null;
        }
        LuceneValueCoder valueCoder = keyCoderMap.get( key );
        if (valueCoder != null) {
            return (T)valueCoder.decode( doc, key );
        }
        for (LuceneValueCoder candidate : valueCoders) {
            T result = (T)candidate.decode( doc, key );
            if (result != null) {
                keyCoderMap.put( key, candidate );
                return result;
            }
        }
        //throw new RuntimeException( "No LuceneValueCoder found for field: " + key );
        return null;
    }
    

    public final Query searchQuery( QueryExpression exp ) {
        assert exp != null;
        
        for (LuceneValueCoder valueCoder : valueCoders) {
            Query result = valueCoder.searchQuery( exp );
            if (result != null) {
                return result;
            }
        }
        throw new RuntimeException( "No LuceneValueCoder found for query expression: " + exp );
    }
    
}
