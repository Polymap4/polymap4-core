/* 
 * polymap.org
 * Copyright 2011, 2012, Polymap GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

import org.polymap.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class LuceneRecordState
        implements IRecordState {

    private static Log log = LogFactory.getLog( LuceneRecordState.class );

    public static final String  ID_FIELD = "identity";
    
    private static AtomicInteger    idCount = new AtomicInteger( (int)System.currentTimeMillis() );
    
    
    // instance *******************************************
    
    private LuceneRecordStore   store;
    
    private Document            doc;
    
    private boolean             sharedDoc = false;
    
    private boolean             isNew = false;
    
    
    protected LuceneRecordState( LuceneRecordStore store, Document doc, boolean sharedDoc ) {
        this.store = store;
        this.doc = doc;
        this.sharedDoc = sharedDoc;
        this.isNew = doc.get( ID_FIELD ) == null;
    }

    
    Document getDocument() {
        return doc;
    }

    void setShared( boolean shared ) {
        this.sharedDoc = shared;
    }
    
    
    public String toString() {
        StringBuilder result = new StringBuilder( "LuceneRecordState[" );
        for (Entry<String,Object> entry : this) {
            result.append( entry.getKey() ).append( "=" ).append( entry.getValue() );
            result.append( ",\n    " );
        }
        result.append( "]" );
        return result.toString();
    }

    
    public boolean isNew() {
        return isNew;
    }
    
    
    public void setIsNew( boolean isNew ) {
        this.isNew = isNew;
    }


    public Object id() {
        return doc.get( ID_FIELD );
    }

    
    void createId( Object id ) {
        assert isNew;
        assert doc.getFieldable( ID_FIELD ) == null : "ID already set for this record";
        
        Field idField = id != null
                ? new Field( ID_FIELD, id.toString(), Store.YES, Index.NOT_ANALYZED )
                : new Field( ID_FIELD, String.valueOf( idCount.getAndIncrement() ), Store.YES, Index.NOT_ANALYZED );
        doc.add( idField );
    }
    
    
    protected void checkCopyOnWrite() {
        if (sharedDoc) {
            synchronized (this) {
                if (sharedDoc) {
                    
                    sharedDoc = false;
                    
                    try {
                        store.lock.readLock().lock();
                        TermDocs termDocs = store.reader.termDocs( new Term( LuceneRecordState.ID_FIELD, (String)id() ) );
                        try {
                            if (termDocs.next()) {
                                doc = store.reader.document( termDocs.doc() );
                            }
                            else {
                                throw new RuntimeException( "Unable to copy Lucene document on write." );
                            }
                        }
                        finally {
                            termDocs.close();
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException( "Unable to copy Lucene document on write." );
                    }
                    finally {
                        store.lock.readLock().unlock();
                    }
                }
            }
        }
    }


    public <T> LuceneRecordState put( String key, T value ) {
        assert key != null && key.length() > 0 : "Key must not be null or empty.";
        assert value != null : "Value must not be null.";
        
        checkCopyOnWrite();
        
        Fieldable old = doc.getFieldable( key );
        if (old != null) {
            // FIXME ValueCoder may have different/additional keys
            doc.removeField( key );
        }
        boolean indexed = store.getIndexFieldSelector().test( key );
        store.valueCoders.encode( doc, key, value, indexed );
        
        return this;
    }

    
    public LuceneRecordState add( String key, Object value ) {
        assert key != null;
        assert value != null : "Value must not be null.";

        checkCopyOnWrite();
        
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
        checkCopyOnWrite();

        // FIXME ValueCoder may have different/additional keys
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
                it.remove();
            }
        };
    }
    
}
