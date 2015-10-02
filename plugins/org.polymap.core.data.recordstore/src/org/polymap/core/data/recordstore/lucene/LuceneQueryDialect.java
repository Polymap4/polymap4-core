/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.data.recordstore.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.io.IOException;

import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
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
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.recordstore.QueryDialect;
import org.polymap.core.data.recordstore.RDataStore;
import org.polymap.core.data.recordstore.RFeature;
import org.polymap.core.data.recordstore.RFeatureStore;
import org.polymap.core.runtime.Timer;

import org.polymap.recordstore.IRecordFieldSelector;
import org.polymap.recordstore.IRecordState;
import org.polymap.recordstore.IRecordStore;
import org.polymap.recordstore.QueryExpression;
import org.polymap.recordstore.RecordQuery;
import org.polymap.recordstore.ResultSet;
import org.polymap.recordstore.lucene.LuceneRecordQuery;
import org.polymap.recordstore.lucene.LuceneRecordState;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * Transformation from GeoTools {@link Query} to {@link LuceneRecordQuery}.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
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

    
    protected IRecordStore rs( RFeatureStore fs ) {
        return ((RDataStore)fs.getDataStore()).getStore();
    }

    
    @Override
    public QueryCapabilities getQueryCapabilities() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public int getCount( RFeatureStore fs, Query query ) throws IOException {
        // XXX handle postProcess
        Transformer transformer = new Transformer();
        RecordQuery rsQuery = transformer.transform( fs, query );
        try {
            ResultSet resultSet = rs( fs ).find( rsQuery );
            return resultSet.count();
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e );
        }
    }


    @Override
    public ReferencedEnvelope getBounds( RFeatureStore fs, Query query ) throws IOException {
        Timer timer = new Timer();
        FeatureType schema = fs.getSchema();
//        String typeName = schema.getName().getLocalPart();
        String geomName = schema.getGeometryDescriptor().getLocalName();

        // type/name query
        // XXX handle postProcess
        Transformer transformer = new Transformer();
        RecordQuery rsQuery = transformer.transform( fs, query );
        rsQuery.setMaxResults( 1 );

        try {
            // MinX
            String fieldName = geomName+GeometryValueCoder.FIELD_MINX;
            rsQuery.sort( fieldName, RecordQuery.ASC, Double.class );
            ResultSet resultSet = rs( fs ).find( rsQuery );
            if (resultSet.count() == 0) {
                return ReferencedEnvelope.EVERYTHING;
            }
            double minX = resultSet.get( 0 ).get( fieldName );

            // MaxX
            fieldName = geomName+GeometryValueCoder.FIELD_MAXX;
            rsQuery.sort( fieldName, RecordQuery.DESC, Double.class );
            resultSet = rs( fs ).find( rsQuery );
            double maxX = resultSet.get( 0 ).get( fieldName );

            // MinY
            fieldName = geomName+GeometryValueCoder.FIELD_MINY;
            rsQuery.sort( fieldName, RecordQuery.ASC, Double.class );
            resultSet = rs( fs ).find( rsQuery );
            double minY = resultSet.get( 0 ).get( fieldName );

            // MaxX
            fieldName = geomName+GeometryValueCoder.FIELD_MAXY;
            rsQuery.sort( fieldName, RecordQuery.DESC, Double.class );
            resultSet = rs( fs ).find( rsQuery );
            double maxY = resultSet.get( 0 ).get( fieldName );

            log.debug( "Bounds: ... (" + timer.elapsedTime() + "ms)" );
            
            return new ReferencedEnvelope( minX, maxX, minY, maxY, schema.getCoordinateReferenceSystem() );
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e );
        }        
    }


    @Override
    public PostProcessResultSet getFeatureStates( RFeatureStore fs, final Query query ) throws IOException {
        try {
            Timer timer = new Timer();
            
            // transform query
            final Transformer transformer = new Transformer();
            RecordQuery rsQuery = transformer.transform( fs, query );
            
            // field selector
            final String[] propNames = query.getPropertyNames();
            if (propNames != null) {
                rsQuery.setFieldSelector( new IRecordFieldSelector() {
                    private Map<String,Boolean> keys = new HashMap( 64 );
                    
                    @Override
                    public boolean test( String key ) {
                        Boolean accepted = keys.get( key );
                        if (accepted == null) {
                            keys.put( key, accepted = Boolean.FALSE );

                            for (String propName : propNames) {
                                // XXX real field names and additional fields are not known here
                                if (key.startsWith( propName )) {
                                    keys.put( key, accepted = Boolean.TRUE );
                                    break;
                                }
                            }
                        }
                        return accepted;
                    }
                });
            }
            
            final ResultSet results = rs( fs ).find( rsQuery );
            log.debug( "    non-processed results: " + results.count() + " ( " + timer.elapsedTime() + "ms)" );
            
            return new PostProcessResultSet() {
                private boolean     hasProcessing = !transformer.postProcess.isEmpty();
                private Filter      filter = query.getFilter();
                @Override
                public Iterator<IRecordState> iterator() {
                    return results.iterator();
                }
                @Override
                public boolean hasPostProcessing() {
                    return hasProcessing;
                }
                @Override
                public boolean postProcess( Feature feature ) {
                    return hasProcessing ? filter.evaluate( feature ) : true;
                }
                @Override
                public int size() {
                    return results.count();
                }
            };
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e );
        }
    }

    
    /**
     * 
     */
    protected class Transformer {
        
        private List<Filter>        postProcess = new ArrayList();
        
        private FeatureType         schema;
        
        public RecordQuery transform( RFeatureStore fs, final Query query ) {
            schema = fs.getSchema();
            String typeName = fs.getSchema().getName().getLocalPart();

            // transform filter
            org.apache.lucene.search.Query filterQuery = processFilter( query.getFilter() );

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
            //log.debug( "LUCENE: " + luceneQuery );

            RecordQuery result = new LuceneRecordQuery( (LuceneRecordStore)rs( fs ), luceneQuery );
            if (query.getStartIndex() != null && query.getStartIndex() > 0) {
                result.setFirstResult( query.getStartIndex() );
            }
            result.setMaxResults( query.getMaxFeatures() );
            return result;
        }


        protected org.apache.lucene.search.Query processFilter( Filter filter ) {
            // start
            if (filter == null || filter.equals( Filter.INCLUDE )) {
                return ALL;
            }
            // AND
            else if (filter instanceof And) {
                BooleanQuery result = new BooleanQuery();
                for (Filter child : ((And)filter).getChildren()) {
                    if (child instanceof Not) {
                        result.add( processFilter( ((Not)child).getFilter()), BooleanClause.Occur.MUST_NOT );                        
                    }
                    else {
                        result.add( processFilter( child ), BooleanClause.Occur.MUST );
                    }
                }
                return result;
            }
            // OR
            else if (filter instanceof Or) {
                BooleanQuery result = new BooleanQuery();
                for (Filter child : ((Or)filter).getChildren()) {
                    // XXX child == Not?
                    result.add( processFilter( child ), BooleanClause.Occur.SHOULD );
                }
                return result;
            }
            // NOT
            else if (filter instanceof Not) {
                BooleanQuery result = new BooleanQuery();
                Filter child = ((Not)filter).getFilter();
                result.add( processFilter( child ), BooleanClause.Occur.MUST_NOT );
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
                return processBBOX( (BBOX)filter );
            }
            else if (filter instanceof BinarySpatialOperator) {
                return processBinarySpatial( (BinarySpatialOperator)filter );
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
            else {
                throw new UnsupportedOperationException( "Unsupported filter type: " + filter.getClass() );
            }
        }


        @SuppressWarnings("deprecation")
        protected org.apache.lucene.search.Query processBBOX( final BBOX bbox ) {
            String propName = bbox.getPropertyName();
            //assert !propName.equals( "" ) : "Empty propName not supported for BBOX filter.";
            final String fieldName = propName.equals( "" ) ? schema.getGeometryDescriptor().getLocalName() : propName;

//            if (schema.getDescriptor( fieldName ).getType().getBinding() != Point.class) {
//                postProcess.add( new Predicate<IRecordState>() {
//                    public boolean apply( IRecordState input ) {
//                        Geometry geom = input.get( fieldName );
//                        Polygon bounds = JTS.toGeometry( new Envelope( bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY() ) );
//                        return geom != null ? geom.intersects( bounds ) : false;
//                    }
//                });
//            }

            return store.getValueCoders().searchQuery( 
                    new QueryExpression.BBox( fieldName, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() ) );
        }


        protected org.apache.lucene.search.Query processBinarySpatial( BinarySpatialOperator filter ) {
            PropertyName prop = (PropertyName)filter.getExpression1();
            Literal literal = (Literal)filter.getExpression2();
            
            // fieldName
            final String fieldName = prop.getPropertyName().equals( "" ) 
                    ? schema.getGeometryDescriptor().getLocalName() 
                    : prop.getPropertyName();

            // query bbox
            Envelope bounds = null;
            if (literal.getValue() instanceof Geometry) {
                bounds = ((Geometry)literal.getValue()).getEnvelopeInternal();
            }
            else {
                throw new IllegalArgumentException( "Geometry type not supported: " + literal.getValue() );
            }
            // and post-process
            postProcess.add( filter );
            return store.getValueCoders().searchQuery( 
                    new QueryExpression.BBox( fieldName, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY() ) );
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

            // fieldname and value/type
            // Literals have correkt type, or are Strings in case of SLD
            String fieldname = prop.getPropertyName();
            Object value = literal.getValue();
            Class<?> binding = schema.getDescriptor( fieldname ).getType().getBinding();
            if (binding == Integer.class && value instanceof String) {
                value = Integer.valueOf( (String)value );
            }
            else if (binding == Long.class && value instanceof String) {
                value = Long.valueOf( (String)value );
            }
            else if (binding == Float.class && value instanceof String) {
                value = Float.valueOf( (String)value );
            }
            else if (binding == Double.class && value instanceof String) {
                value = Double.valueOf( (String)value );
            }
            // check actual value type and binding
            if (!binding.isAssignableFrom( value.getClass() )) {
                throw new RuntimeException( "Unsupported literal/binding: " + value.getClass().getSimpleName() + "/" + binding.getSimpleName() );
            }
            
            // equals
            if (predicate instanceof PropertyIsEqualTo) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.Equal( fieldname, value ) );
            }
            // not equals
            if (predicate instanceof PropertyIsNotEqualTo) {
                org.apache.lucene.search.Query arg = store.getValueCoders().searchQuery( 
                        new QueryExpression.Equal( fieldname, value ) );
                BooleanQuery result = new BooleanQuery();
                result.add( arg, BooleanClause.Occur.MUST_NOT );
                return result;
            }
            // ge
            else if (predicate instanceof PropertyIsGreaterThanOrEqualTo) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.GreaterOrEqual( fieldname, value ) );
            }
            // gt
            else if (predicate instanceof PropertyIsGreaterThan) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.Greater( fieldname, value ) );
            }
            // le
            else if (predicate instanceof PropertyIsLessThanOrEqualTo) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.LessOrEqual( fieldname, value ) );
            }
            // lt
            else if (predicate instanceof PropertyIsLessThan) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.Less( fieldname, value ) );
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
