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

import java.io.IOException;

import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;

import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.lucene.GeometryValueCoder;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class LuceneQueryDialect
        extends QueryDialect {

    private static Log log = LogFactory.getLog( LuceneQueryDialect.class );

    private static final MatchAllDocsQuery  ALL = new MatchAllDocsQuery();


    public void initStore( IRecordStore store ) {
        ((LuceneRecordStore)store).getValueCoders().addValueCoder( new GeometryValueCoder() );
    }


    public QueryCapabilities getQueryCapabilities() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public int getCount( RFeatureStore fs, Query query )
    throws IOException {
        RecordQuery rsQuery = transform( fs, query );
        try {
            ResultSet resultSet = fs.ds.getStore().find( rsQuery );
            return resultSet.count();
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e );
        }
    }


    public ReferencedEnvelope getBounds( RFeatureStore fs, Query query )
    throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public ResultSet getFeatureStates( RFeatureStore fs, Query query )
    throws IOException {
        RecordQuery rsQuery = transform( fs, query );
        try {
            return fs.ds.getStore().find( rsQuery );
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e );
        }
    }
    
    
    protected RecordQuery transform( RFeatureStore fs, final Query query ) {
        String typeName = fs.getSchema().getName().getLocalPart();
        
        // transform filter
        org.apache.lucene.search.Query filterQuery = transform( query.getFilter() );

        // add type/name query
        TermQuery typeQuery = new TermQuery( new Term( RFeature.TYPE_KEY, typeName ) );
        
        org.apache.lucene.search.Query luceneQuery = null;
        if (! filterQuery.equals( ALL )) {
            luceneQuery = new BooleanQuery();
            ((BooleanQuery)luceneQuery).add( filterQuery, BooleanClause.Occur.MUST );
            ((BooleanQuery)luceneQuery).add( typeQuery, BooleanClause.Occur.MUST );
        }
        else {
            luceneQuery = typeQuery;
        }

        // sort
        if (query.getSortBy() != null && query.getSortBy().length > 0) {
            throw new UnsupportedOperationException( "Not implemented yet: sortBy" );
//            for (SortBy sortby : query.getSortBy()) {
//                
//            }
        }
        log.debug( "LUCENE: " + luceneQuery );
        
        RecordQuery result = new LuceneRecordQuery( (LuceneRecordStore)fs.ds.store, luceneQuery );
        if (query.getStartIndex() != null) {
            result.setFirstResult( query.getStartIndex() );
        }
        result.setMaxResults( query.getMaxFeatures() );
        return result;
    }


    protected org.apache.lucene.search.Query transform( Filter filter ) {
        if (filter.equals( Filter.INCLUDE )) {
            return ALL;
        }
        throw new UnsupportedOperationException( "Unsupported filter: " + filter );
    }
    
}
