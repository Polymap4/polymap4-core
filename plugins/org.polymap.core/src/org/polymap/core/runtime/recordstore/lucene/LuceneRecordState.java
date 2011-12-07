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
package org.polymap.core.runtime.recordstore.lucene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class LuceneRecordState
        implements IRecordState {

    private static Log log = LogFactory.getLog( LuceneRecordState.class );

    public static final String  ID_FIELD = "identity";
    
    private static long         idCount = System.currentTimeMillis();
    
    
    // instance *******************************************
    
    private LuceneRecordStore   store;
    
    private Document            doc;
    
    
    protected LuceneRecordState( LuceneRecordStore store, Document doc ) {
        this.store = store;
        this.doc = doc;
    }

    
    Document getDocument() {
        return doc;
    }

    
    public String toString() {
        StringBuilder result = new StringBuilder( "LuceneRecordState{" );
        for (Entry<String,Object> entry : this) {
            result.append( entry.getKey() ).append( '=' ).append( entry.getValue() );
            result.append( ", " );
        }
        result.append( "}" );
        return result.toString();
    }

    
    public Object id() {
        return doc.get( ID_FIELD );
    }

    
    void createId() {
        assert doc.getFieldable( ID_FIELD ) == null : "ID already set for this record";
        
        Field idField = new Field( ID_FIELD, String.valueOf( idCount++ ), Store.YES, Index.NOT_ANALYZED );
        doc.add( idField );
    }
    
    
    public <T> LuceneRecordState put( String key, T value ) {
        assert key != null;
        assert value != null : "Value must not be null.";
        
        Fieldable old = doc.getFieldable( key );
        if (old != null) {
            doc.removeField( key );
        }
        boolean indexed = store.getIndexFieldSelector().accept( key );
        store.valueCoders.encode( doc, key, value, indexed );
        
        return this;
    }

    
    public LuceneRecordState add( String key, Object value ) {
        assert key != null;
        assert value != null : "Value must not be null.";

        Field lengthField = (Field)doc.getFieldable( key + "_length" );
        int length = -1;
        
        if (lengthField == null) {
            length = 1;
            doc.add( new Field( key + "_length", "1", Store.YES, Index.NO ) );
        }
        else {
            length = Integer.parseInt( lengthField.stringValue() ) + 1;
            lengthField.setValue( String.valueOf( length ) );
        }
        
        StringBuilder arrayKey = new StringBuilder( 32 )
                .append( key ).append( '[' ).append( length-1 ).append( ']' );
        
        put( arrayKey.toString(), value );
        
        return this;
    }

    
    public <T> T get( String key ) {
        return (T)store.valueCoders.decode( doc, key );
    }

    
    public <T> List<T> getList( String key ) {
        // XXX try a lazy facade!?
        List<T> result = new ArrayList<T>();
        
        String lengthString = doc.get( key + "_length" );
        int length = lengthString != null ? Integer.parseInt( lengthString ) : 0;
        for (int i=0; i<length; i++) {
            StringBuilder arrayKey = new StringBuilder( 32 )
                    .append( key ).append( '[' ).append( i ).append( ']' );
            result.add( (T)get( arrayKey.toString() ) );
        }
        return result;
    }


    public LuceneRecordState remove( String key ) {
        doc.removeField( key );
        return this;
    }

    
    public Iterator<Entry<String, Object>> iterator() {
        return new Iterator<Entry<String, Object>>() {

            private Iterator<Fieldable>     it = doc.getFields().iterator();

            public boolean hasNext() {
                return it.hasNext();
            }

            public Entry<String, Object> next() {
                return new Entry<String,Object>() {
                    
                    private Fieldable   field = it.next();
                    
                    public String getKey() {
                        return field.name();
                    }

                    public Object getValue() {
                        return store.valueCoders.decode( doc, getKey() );
                    }

                    public Object setValue( Object value ) {
                        Object old = getValue();
                        LuceneRecordState.this.put( getKey(), value );
                        return old;
                    }
                };
            }

            public void remove() {
                throw new UnsupportedOperationException( "remove()" );
                //LuceneRecordState.this.remove( field.name() );
            }
        };
    }
    
}
