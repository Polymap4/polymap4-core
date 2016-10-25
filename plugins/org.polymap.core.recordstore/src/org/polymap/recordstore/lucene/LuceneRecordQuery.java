/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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

import static org.apache.commons.lang3.StringUtils.abbreviate;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

import org.polymap.core.runtime.Timer;

import org.polymap.recordstore.IRecordFieldSelector;
import org.polymap.recordstore.IRecordState;
import org.polymap.recordstore.RecordQuery;
import org.polymap.recordstore.ResultSet;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneRecordQuery
        extends RecordQuery {

    private static Log log = LogFactory.getLog( LuceneRecordQuery.class );

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
        Timer timer = new Timer();
        
        // Lucene does not have a firstResult feature
        int requestedResults = getMaxResults() + getFirstResult();
                
        return store.readLocked( () -> {
            String sortKey = getSortKey();
            if (sortKey != null) {
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
                else if (getSortType() == Date.class) {
                    sortType = SortField.LONG;
                    sortKey = sortKey + DateValueCoder.SUFFIX;
                }
                Sort sort = new Sort( new SortField( sortKey, sortType, getSortOrder() == DESC ) );
                TopDocs topDocs = store.searcher.search( luceneQuery, requestedResults, sort );
                logResult( topDocs.scoreDocs.length, timer );
                return new LuceneResultSet( topDocs.scoreDocs );
            }
            else {
                TopDocs topDocs = store.searcher.search( luceneQuery, requestedResults );
                logResult( topDocs.scoreDocs.length, timer );
                return new LuceneResultSet( topDocs.scoreDocs );
            }
        });
    }

    
    protected void logResult( int length, Timer timer ) {
        log.info( "LUCENE: " 
                + abbreviate( luceneQuery.toString(), 256 ) 
                + "  --  results: " + length
                + " [" + getFirstResult() + "-" + getMaxResults() + "]"
                + " (" + timer.elapsedTime() + "ms)" );        
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
            assert scoreDocs != null;

            // skip getFirstResult(), Lucene does not provide this feature
            this.scoreDocs = scoreDocs;
            if (getFirstResult() > 0) {
                if (getFirstResult() >= scoreDocs.length) {
                    this.scoreDocs = new ScoreDoc[] {};
                }
                else {
                    this.scoreDocs = Arrays.copyOfRange( scoreDocs, getFirstResult(), scoreDocs.length );
                }
            }
            
            // build fieldSelector
            final IRecordFieldSelector sel = getFieldSelector();
            if (getFieldSelector() != null && sel != IRecordFieldSelector.ALL) {
                fieldSelector = new FieldSelector() {
                    public FieldSelectorResult accept( String fieldName ) {
                        if (fieldName.equals( LuceneRecordState.ID_FIELD )) {
                            return FieldSelectorResult.LOAD;
                        }
                        else if (sel.test( fieldName )) { 
                            return FieldSelectorResult.LOAD;
                        }
                        return FieldSelectorResult.NO_LOAD;
                    }
                };
            }
        }

        protected void checkOpen() {
            if (scoreDocs == null) {
                throw new IllegalStateException( "LucenResultSet is closed." );
            }
        }
        
        @Override
        public void close() {
            scoreDocs = null;
        }

        @Override
        public int count() {
            checkOpen();
            return scoreDocs.length;
        }

        @Override
        public LuceneRecordState get( int index ) throws Exception {
            checkOpen();
            assert index < scoreDocs.length;
            int doc = scoreDocs[index].doc;
            return store.get( doc, fieldSelector );
        }

        @Override
        public Iterator<IRecordState> iterator() {
            return new Iterator<IRecordState>() {
                private int         index;
                @Override
                public boolean hasNext() {
                    checkOpen();
                    return index < scoreDocs.length;
                }
                @Override
                public LuceneRecordState next() {
                    checkOpen();
                    try {
                        return get( index++ );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException( "Not supported." );
                }
            };
        }

        @Override
        public Stream<IRecordState> stream() {
            checkOpen();
            return StreamSupport.stream( spliterator(), false );
        }
    }

    
    /**
     * 
     */
    protected class IdFieldSelector
            implements FieldSelector {

        @Override
        public FieldSelectorResult accept( String fieldName ) {
            return fieldName == LuceneRecordState.ID_FIELD || fieldName.equals( LuceneRecordState.ID_FIELD )
                    ? FieldSelectorResult.LOAD
                    : FieldSelectorResult.NO_LOAD;
        }

    }

}
