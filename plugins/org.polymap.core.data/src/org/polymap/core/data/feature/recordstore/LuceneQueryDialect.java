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

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.lucene.GeometryValueCoder;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordState;
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

    private LuceneRecordStore               store;


    public void initStore( IRecordStore _store ) {
        assert this.store == null;
        this.store = (LuceneRecordStore)_store;
        (store).getValueCoders().addValueCoder( new GeometryValueCoder() );
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
        try {
            Timer timer = new Timer();
            RecordQuery rsQuery = transform( fs, query );
            ResultSet result = fs.ds.getStore().find( rsQuery );
            log.debug( "    results: " + result.count() + " ( " + timer.elapsedTime() + "ms)" );
            return result;
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
        org.apache.lucene.search.Query filterQuery = processFilter( query.getFilter(), fs.getSchema() );

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


    protected org.apache.lucene.search.Query processFilter( Filter filter, FeatureType schema ) {
        // start
        if (filter == null || filter.equals( Filter.INCLUDE )) {
            return ALL;
        }
        // AND
        else if (filter instanceof And) {
            BooleanQuery result = new BooleanQuery();
            for (Filter child : ((And)filter).getChildren()) {
                if (child instanceof Not) {
                    result.add( processFilter( ((Not)child).getFilter(), schema ), BooleanClause.Occur.MUST_NOT );                        
                }
                else {
                    result.add( processFilter( child, schema ), BooleanClause.Occur.MUST );
                }
            }
            return result;
        }
        // OR
        else if (filter instanceof Or) {
            BooleanQuery result = new BooleanQuery();
            for (Filter child : ((Or)filter).getChildren()) {
                // XXX child == Not?
                result.add( processFilter( child, schema ), BooleanClause.Occur.SHOULD );
            }
            return result;
        }
        // NOT
        else if (filter instanceof Not) {
            BooleanQuery result = new BooleanQuery();
            Filter child = ((Not)filter).getFilter();
            result.add( processFilter( child, schema ), BooleanClause.Occur.MUST_NOT );
            return result;
        }
        // INCLUDE
        else if (filter instanceof IncludeFilter) {
            return ALL;
        }
        // EXCLUDE
        else if (filter instanceof ExcludeFilter) {
            // XXX any better way to express?
            return new TermQuery( new Term( "__does_not_exist__", "true") );
        }
        // BBOX
        else if (filter instanceof BBOX) {
            return processBBOX( (BBOX)filter, schema );
        }
        // FID
        else if (filter instanceof Id) {
            Id fidFilter = (Id)filter;
            if (fidFilter.getIdentifiers().size() > BooleanQuery.getMaxClauseCount()) {
                BooleanQuery.setMaxClauseCount( fidFilter.getIdentifiers().size() );
            }
            BooleanQuery result = new BooleanQuery();
            for (Identifier fid : fidFilter.getIdentifiers()) {
                org.apache.lucene.search.Query fidQuery = store.getValueCoders().searchQuery( 
                        new QueryExpression.Equal( LuceneRecordState.ID_FIELD, fid.getID() ) );
                result.add( fidQuery, BooleanClause.Occur.SHOULD );
            }
            return result;
        }
        // comparison
        else if (filter instanceof BinaryComparisonOperator) {
            return processComparison( (BinaryComparisonOperator)filter );
        }
        // isLike
        else if (filter instanceof PropertyIsLike) {
            return processIsLike( (PropertyIsLike)filter );
        }
        // isNull
        else if (filter instanceof PropertyIsNull) {
            throw new UnsupportedOperationException( "PropertyIsNull" );
        }
        // between
        else if (filter instanceof PropertyIsBetween) {
            throw new UnsupportedOperationException( "PropertyIsBetween" );
        }
        //        // MANY Assoc
        //        else if (filter instanceof ManyAssociationContainsPredicate) {
        //            throw new UnsupportedOperationException( "ManyAssociationContainsPredicate" );
        //        }
        //        // Assoc
        //        else if (filter instanceof AssociationNullPredicate) {
        //            throw new UnsupportedOperationException( "AssociationNullPredicate" );
        //        }
        //        // contains
        //        else if (filter instanceof ContainsPredicate) {
        //            return processContainsPredicate( (ContainsPredicate)filter );
        //        }
        else {
            throw new UnsupportedOperationException( "Unsupported filter type: " + filter.getClass() );
        }
    }


    @SuppressWarnings("deprecation")
    protected org.apache.lucene.search.Query processBBOX( BBOX bbox, FeatureType schema ) {
        String propName = bbox.getPropertyName();
        //assert !propName.equals( "" ) : "Empty propName not supported for BBOX filter.";
        String fieldName = propName.equals( "" ) ? schema.getGeometryDescriptor().getLocalName() : propName;

        return store.getValueCoders().searchQuery( 
                new QueryExpression.BBox( fieldName, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() ) );
    }


    protected org.apache.lucene.search.Query processComparison( BinaryComparisonOperator predicate ) {
        Expression expression1 = predicate.getExpression1();
        Expression expression2 = predicate.getExpression2();

        Literal literal = null;
        PropertyName prop = null;

        // expression1
        if (expression1 instanceof Literal) {
            literal = (Literal)expression1;
        }
        else if (expression1 instanceof PropertyName) {
            prop = (PropertyName)expression1;
        }
        else {
            throw new RuntimeException( "Expression type not supported: " + expression1 );
        }

        // expression2
        if (expression2 instanceof Literal) {
            literal = (Literal)expression2;
        }
        else if (expression2 instanceof PropertyName) {
            prop = (PropertyName)expression2;
        }
        else {
            throw new RuntimeException( "Expression type not supported: " + expression2 );
        }

        if (literal == null || prop == null) {
            throw new RuntimeException( "Comparison not supported: " + expression1 + " - " + expression2 );
        }

        String fieldname = prop.getPropertyName();

        // equals
        if (predicate instanceof PropertyIsEqualTo) {
            return store.getValueCoders().searchQuery( 
                    new QueryExpression.Equal( fieldname, literal.getValue() ) );
        }
        // not equals
        if (predicate instanceof PropertyIsNotEqualTo) {
            org.apache.lucene.search.Query arg = store.getValueCoders().searchQuery( 
                    new QueryExpression.Equal( fieldname, literal.getValue() ) );
            BooleanQuery result = new BooleanQuery();
            result.add( arg, BooleanClause.Occur.MUST_NOT );
            return result;
        }
        // ge
        else if (predicate instanceof PropertyIsGreaterThanOrEqualTo) {
            return store.getValueCoders().searchQuery( 
                    new QueryExpression.GreaterOrEqual( fieldname, literal.getValue() ) );
        }
        // gt
        else if (predicate instanceof PropertyIsGreaterThan) {
            return store.getValueCoders().searchQuery( 
                    new QueryExpression.Greater( fieldname, literal.getValue() ) );
        }
        // le
        else if (predicate instanceof PropertyIsLessThanOrEqualTo) {
            return store.getValueCoders().searchQuery( 
                    new QueryExpression.LessOrEqual( fieldname, literal.getValue() ) );
        }
        // lt
        else if (predicate instanceof PropertyIsLessThan) {
            return store.getValueCoders().searchQuery( 
                    new QueryExpression.Less( fieldname, literal.getValue() ) );
        }
        else {
            throw new UnsupportedOperationException( "Predicate type not supported in comparison: " + predicate.getClass() );
        }
    }


    protected org.apache.lucene.search.Query processIsLike( PropertyIsLike predicate ) {
        String value = predicate.getLiteral();
        PropertyName prop = (PropertyName)predicate.getExpression();
        String fieldname = prop.getPropertyName();

        // assuming that QueryExpression.Match use *,?
        value = StringUtils.replace( value, predicate.getWildCard(), "*" );
        value = StringUtils.replace( value, predicate.getSingleChar(), "?" );

        return store.getValueCoders().searchQuery( 
                new QueryExpression.Match( fieldname, value ) );
    }


    public static boolean supports( Filter _filter ) {
        final List notSupported = new ArrayList();
        _filter.accept( new DefaultFilterVisitor() {
            public Object visit( Beyond filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Contains filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Crosses filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Disjoint filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Divide expression, Object data ) {
                notSupported.add( expression );
                return super.visit( expression, data );
            }
            public Object visit( DWithin filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( org.opengis.filter.expression.Function expression, Object data ) {
                notSupported.add( expression );
                return super.visit( expression, data );
            }
            public Object visit( Intersects filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Multiply expression, Object data ) {
                notSupported.add( expression );
                return super.visit( expression, data );
            }
            public Object visit( Overlaps filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Subtract expression, Object data ) {
                notSupported.add( expression );
                return super.visit( expression, data );
            }
            public Object visit( Touches filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Within filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
        }, notSupported );

        return notSupported.isEmpty();
    }

}
