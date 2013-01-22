/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.feature.recordstore;

import org.polymap.core.runtime.recordstore.IRecordStore;

/**
 * A generator of field keys in a {@link IRecordStore}. The keys can
 * represent complex properties and collections. Complex keys are build like
 * XPath expressions: <code>base/coll[1]/prop</code>.
 * <p/>
 * Impl. note: Not sure if the impl is good but to use this interface allows to
 * come up with a better one later without the need to change other code.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class StoreKey {
    
    public static final char    PROP_SEPARATOR = '/';
    public static final char    OPEN_INDEX_SEPARATOR = '[';
    public static final char    CLOSE_INDEX_SEPARATOR = ']';
    public static final String  LENGTH_SUFFIX = "__length";
    
    private StringBuilder       data;

    
    public StoreKey() {
        this.data = new StringBuilder( 128 );
    }
    
    
    private StoreKey( StringBuilder data ) {
        this.data = data;
    }


    public StoreKey appendProperty( String name ) {
        StringBuilder newData = new StringBuilder( data );
        if (newData.length() > 0) {
            newData.append( PROP_SEPARATOR );
        }
        newData.append( name );
        return new StoreKey( newData );
    }

    
    public StoreKey appendCollectionIndex( int index ) {
        return new StoreKey( new StringBuilder( data )
                .append( OPEN_INDEX_SEPARATOR )
                .append( index )    
                .append( CLOSE_INDEX_SEPARATOR ) );
    }


    public StoreKey appendCollectionLength() {
        return new StoreKey( new StringBuilder( data )
                .append( LENGTH_SUFFIX ) );
    }

    
    public int length() {
        return data.length();
    }
    
    
    public String toString() {
        return data.toString();
    }
    
}
