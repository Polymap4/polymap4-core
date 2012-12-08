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

import java.util.Collection;
import java.util.Iterator;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.IRecordStore.ResultSet;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
final class SimpleQueryResultSet
        implements ResultSet {

    private static Log log = LogFactory.getLog( SimpleQueryResultSet.class );

    private LuceneRecordStore   store;
    
    private ScoreDoc[]          scoreDocs;
    
    private FieldSelector       idFieldSelector = new IdFieldSelector();
    

    SimpleQueryResultSet( LuceneRecordStore store, SimpleQuery query )
    throws IOException {
        this.store = store;
        
        // build query
        Query luceneQuery = null;
        Collection<QueryExpression> expressions = query.expressions();
        if (expressions.isEmpty()) {
            luceneQuery = new MatchAllDocsQuery();
        }
        else {
            luceneQuery = new BooleanQuery();
            for (QueryExpression exp : expressions) {
                ((BooleanQuery)luceneQuery).add( 
                        store.valueCoders.searchQuery( exp ), BooleanClause.Occur.MUST );
            }
        }
        log.debug( "Lucene Query: " + luceneQuery );          

        // execute Lucene query        
        if (query.getSortKey() != null) {
            int sortType = SortField.STRING;
            if (query.getSortType() == String.class) {
                sortType = SortField.STRING;
            }
            else if (query.getSortType() == Integer.class) {
                sortType = SortField.INT;
            }
            else if (query.getSortType() == Long.class) {
                sortType = SortField.LONG;
            }
            else if (query.getSortType() == Float.class) {
                sortType = SortField.FLOAT;
            }
            else if (query.getSortType() == Double.class) {
                sortType = SortField.DOUBLE;
            }
            Sort sort = new Sort( new SortField( query.getSortKey(), 
                    sortType, query.getSortOrder() == SimpleQuery.DESC ) );
            TopDocs topDocs = store.searcher.search( luceneQuery, query.getMaxResults(), sort );
            scoreDocs = topDocs.scoreDocs;
        }
        else {
            TopDocs topDocs = store.searcher.search( luceneQuery, query.getMaxResults() );
            scoreDocs = topDocs.scoreDocs;
        }
//        log.debug( "    Result: totalHits=" + topDocs.totalHits );          
    }

    
    public int count() {
        return scoreDocs.length;
    }


    public LuceneRecordState get( int index )
    throws Exception {
        assert index < scoreDocs.length;
        
        int doc = scoreDocs[index].doc;
        return store.get( doc );
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

    
    class IdFieldSelector
            implements FieldSelector {
        
        public FieldSelectorResult accept( String fieldName ) {
            return fieldName == LuceneRecordState.ID_FIELD 
                    || fieldName.equals( LuceneRecordState.ID_FIELD )
                    
                    ? FieldSelectorResult.LOAD
                    : FieldSelectorResult.NO_LOAD;
        }

    }
    
}
