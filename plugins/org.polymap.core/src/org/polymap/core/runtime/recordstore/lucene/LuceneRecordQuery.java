/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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

import java.util.Iterator;

import java.io.IOException;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

import org.polymap.core.runtime.recordstore.IRecordFieldSelector;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class LuceneRecordQuery
        extends RecordQuery {

    /** Max size of a {@link ResultSet}. */
    public static final int     BIG_BUT_NOT_MAX_VALUE = 1000000;
    
    private LuceneRecordStore   store;

    private Query               luceneQuery;


    /**
     * Creates a new query instance.
     * <p/>
     * Wildcards default to '*' and '?'. Max results default to 10 ({@link #DEFAULT_MAX_RESULTS})!
     * 
     * @param store
     * @param luceneQuery
     */
    public LuceneRecordQuery( LuceneRecordStore store, Query luceneQuery ) {
        this.store = store;
        this.luceneQuery = luceneQuery;
    }
    
    
    public RecordQuery setMaxResults( int maxResults ) {
        // Lucene does not like Integer.MAX_VALUE here
        return super.setMaxResults( Math.min( BIG_BUT_NOT_MAX_VALUE, maxResults ) );
    }


    public ResultSet execute() throws IOException {
        if (getSortKey() != null) {
            int sortType = SortField.STRING;
            if (getSortType() == String.class) {
                sortType = SortField.STRING;
            }
            else if (getSortType() == Integer.class) {
                sortType = SortField.INT;
            }
            else if (getSortType() == Long.class) {
                sortType = SortField.LONG;
            }
            else if (getSortType() == Float.class) {
                sortType = SortField.FLOAT;
            }
            else if (getSortType() == Double.class) {
                sortType = SortField.DOUBLE;
            }
            Sort sort = new Sort( new SortField( getSortKey(), sortType, getSortOrder() == DESC ) );
            TopDocs topDocs = store.searcher.search( luceneQuery, getMaxResults(), sort );
            return new LuceneResultSet( topDocs.scoreDocs );
        }
        else {
            TopDocs topDocs = store.searcher.search( luceneQuery, getMaxResults() );
            return new LuceneResultSet( topDocs.scoreDocs );
        }
    }

    
    /**
     * 
     */
    protected class LuceneResultSet
            implements ResultSet {

        protected ScoreDoc[]          scoreDocs;

        protected FieldSelector       idFieldSelector = new IdFieldSelector();
        
        protected FieldSelector       fieldSelector;


        protected LuceneResultSet( ScoreDoc[] scoreDocs ) {
            this.scoreDocs = scoreDocs;
            
            // build fieldSelector
            final IRecordFieldSelector sel = getFieldSelector();
            if (getFieldSelector() != null && sel != IRecordFieldSelector.ALL) {
                fieldSelector = new FieldSelector() {
                    public FieldSelectorResult accept( String fieldName ) {
                        if (fieldName.equals( LuceneRecordState.ID_FIELD )) {
                            return FieldSelectorResult.LOAD;
                        }
                        else if (sel.accept( fieldName )) { 
                            return FieldSelectorResult.LOAD;
                        }
                        return FieldSelectorResult.NO_LOAD;
                    }
                };
            }
        }

        public void close() {
            scoreDocs = null;
        }

        public int count() {
            return scoreDocs.length;
        }

        public LuceneRecordState get( int index )
        throws Exception {
            assert index < scoreDocs.length;
            int doc = scoreDocs[index].doc;
            return store.get( doc, fieldSelector );
        }

        public Iterator<IRecordState> iterator() {
            return new Iterator<IRecordState>() {

                private int         index;

                public boolean hasNext() {
                    return index < scoreDocs.length;
                }

                public LuceneRecordState next() {
                    try {
                        return get( index++ );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException( "Not supported." );
                }
            };
        }
    }

    
    /**
     * 
     */
    protected class IdFieldSelector
            implements FieldSelector {

        public FieldSelectorResult accept( String fieldName ) {
            return fieldName == LuceneRecordState.ID_FIELD || fieldName.equals( LuceneRecordState.ID_FIELD )
                    ? FieldSelectorResult.LOAD
                    : FieldSelectorResult.NO_LOAD;
        }

    }

}
