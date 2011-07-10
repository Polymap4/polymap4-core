/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag.
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
package org.polymap.core.data.feature.lucene;

import java.util.ArrayList;
import java.util.List;

import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
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
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 * Creates Lucene {@link Query} instances out of OGC {@link Filter}s.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
class LuceneQueryParser {

    private static Log log = LogFactory.getLog( LuceneQueryParser.class );
    
    private static final Query      ALL = new MatchAllDocsQuery();
    
    private FeatureType             schema;
    
    private Query                   query;
    
    /** Filters that cannot be translated into Lucene query. */
    private List<Filter>            notQueryable;
    
    
    public LuceneQueryParser( FeatureType schema, Filter filter ) {
        super();
        this.schema = schema;

        query = processFilter( filter );
        log.info( "LUCENE query: [" + query.toString() + "]" );
        
        if (notQueryable != null) {
            throw new RuntimeException( "Deferred evaluation of not supported filters is not implemented yet." );
        }
    }


    protected Query getQuery() {
        return query;
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
            public Object visit( Function expression, Object data ) {
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

    
    protected Query processFilter( Filter filter ) {
        // start
        if (filter == null) {
            return ALL;
        }
        // AND
        else if (filter instanceof And) {
            BooleanQuery result = new BooleanQuery();
            for (Filter child : ((And)filter).getChildren()) {
                result.add( processFilter( child ), BooleanClause.Occur.MUST );
            }
            return result;
        }
        // OR
        else if (filter instanceof Or) {
            BooleanQuery result = new BooleanQuery();
            for (Filter child : ((Or)filter).getChildren()) {
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
            return new MatchAllDocsQuery();
        }
        // BBOX
        else if (filter instanceof BBOX) {
            return processBBOX( (BBOX)filter );
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
            throw new UnsupportedOperationException( "Expression " + filter + " is not supported" );
        }
    }


    @SuppressWarnings("deprecation")
    protected Query processBBOX( BBOX bbox ) {
//        return !(other.minx > maxx ||
//                other.maxx < minx ||
//                other.miny > maxy ||
//                other.maxy < miny);
//
//        -> !maxx < other.minx && !mixx > other.maxx
//
//        -> maxx > other.minx && minx < other.maxx
        
        BooleanQuery result = new BooleanQuery();
        String propName = bbox.getPropertyName().equals( "" )
                ? schema.getGeometryDescriptor().getLocalName() : bbox.getPropertyName();
        
        // maxx > bbox.getMinX
        result.add( NumericRangeQuery.newDoubleRange( 
                propName+LuceneCache.FIELD_MAXX, ValueCoder.PRECISION_STEP_64,
                bbox.getMinX(), null, false, false ), BooleanClause.Occur.MUST );
        // minx < bbox.getMaxX
        result.add( NumericRangeQuery.newDoubleRange( 
                propName+LuceneCache.FIELD_MINX, ValueCoder.PRECISION_STEP_64,
                null, bbox.getMaxX(), false, false ), BooleanClause.Occur.MUST );
        // maxy > bbox.getMinY
        result.add( NumericRangeQuery.newDoubleRange( 
                propName+LuceneCache.FIELD_MAXY, ValueCoder.PRECISION_STEP_64,
                bbox.getMinY(), null, false, false ), BooleanClause.Occur.MUST );
        // miny < bbox.getMaxY
        result.add( NumericRangeQuery.newDoubleRange( 
                propName+LuceneCache.FIELD_MINY, ValueCoder.PRECISION_STEP_64,
                null, bbox.getMaxY(), false, false ), BooleanClause.Occur.MUST );
        return result;
    }


    protected Query processComparison( BinaryComparisonOperator predicate ) {
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

        // value / type
        String fieldname = prop.getPropertyName();
        Class valueType = schema.getDescriptor( prop.getPropertyName() ).getType().getBinding();
        Fieldable field = ValueCoder.encode( fieldname, literal.getValue(), valueType, Field.Store.NO, true );

        // equals
        if (predicate instanceof PropertyIsEqualTo) {
            return new TermQuery( new Term( fieldname, field.stringValue() ) );
        }
        // ge
        else if (predicate instanceof PropertyIsGreaterThanOrEqualTo) {
            throw new RuntimeException( "Operator not supported: " + predicate );
//            return field instanceof NumericField
//                    ? NumericRangeQuery.newDoubleRange( fieldname, ValueCoder.DEFAULT_PRECISION, field., null, true, false )
//                    : new TermRangeQuery( fieldname, value, null, true, false );
        }
        // gt
        else if (predicate instanceof PropertyIsGreaterThan) {
            throw new RuntimeException( "Operator not supported: " + predicate );
//            return new TermRangeQuery( fieldname, value, null, false, false );
        }
        // le
        else if (predicate instanceof PropertyIsLessThanOrEqualTo) {
            throw new RuntimeException( "Operator not supported: " + predicate );
//            return new TermRangeQuery( fieldname, null, value, false, true );
        }
        // lt
        else if (predicate instanceof PropertyIsLessThan) {
            throw new RuntimeException( "Operator not supported: " + predicate );
//            return new TermRangeQuery( fieldname, null, value, false, false );
        }
        else {
            throw new UnsupportedOperationException( "Predicate type not supported in comparison: " + predicate );
        }
    }


    protected Query processIsLike( PropertyIsLike predicate ) {
        String literal = predicate.getLiteral();
        PropertyName prop = (PropertyName)predicate.getExpression();

        // value / type
        String fieldname = prop.getPropertyName();
        Class valueType = schema.getDescriptor( prop.getPropertyName() ).getType().getBinding();
        Fieldable field = ValueCoder.encode( fieldname, literal, valueType, Field.Store.NO, false );
        String value = field.stringValue();

        value = StringUtils.replace( value, predicate.getWildCard(), "*" );
        value = StringUtils.replace( value, predicate.getSingleChar(), "?" );

        if (value.endsWith( "*" ) 
                && StringUtils.countMatches( value, "*" ) == 1
                && StringUtils.countMatches( value, "?" ) == 0) {
            return new PrefixQuery( new Term( fieldname, value.substring( 0, value.length() - 1 ) ) );
        }
        else {
            return new WildcardQuery( new Term( fieldname, value ) );
        }
    }



//        /**
//         * Handle the contains predicate.
//         * <p/>
//         * Impl. note: This needs a patch in
//         * org.qi4j.runtime.query.grammar.impl.PropertyReferenceImpl<T> to work with
//         * Qi4j 1.0.
//         */
//        protected Query processContainsPredicate( ContainsPredicate predicate ) {
//            final int maxElements = 10;
//    
//            PropertyReference property = predicate.propertyReference();
//            final String baseFieldname = property2Fieldname( property );
//            SingleValueExpression valueExpression = (SingleValueExpression)predicate.valueExpression();
//    
//            BooleanQuery result = new BooleanQuery();
//            for (int i=0; i<maxElements; i++) {
//                final BooleanQuery valueQuery = new BooleanQuery();
//                
//                final ValueComposite value = (ValueComposite)valueExpression.value();
//                ValueModel valueModel = (ValueModel)ValueInstance.getValueInstance( value ).compositeModel();
//                List<PropertyType> actualTypes = valueModel.valueType().types();
//                //                    json.key( "_type" ).value( valueModel.valueType().type().name() );
//    
//    
//                // all properties of the value
//                final int index = i;
//                value.state().visitProperties( new StateVisitor() {
//                    public void visitProperty( QualifiedName name, Object propValue ) {
//                        if (propValue == null) {
//                        }
//                        else if (propValue.toString().equals( "-1" )) {
//                            // FIXME hack to signal that this non-optional(!) value is not to be considered
//                            log.warn( "Non-optional field ommitted: " + name.name() + ", value=" + propValue );
//                        }
//                        else {
//                            String fieldname = baseFieldname + "[" + index + "]" + LuceneEntityState.SEPARATOR_PROP + name.name();
//    
//                            Property<Object> fieldProperty = value.state().getProperty( name );
//                            String encodedValue = ValueCoder.encode( propValue, (Class)fieldProperty.type() );
//    
//                            // checking for wildcards in the value, like in the matches predicate;
//                            // this might not be the selmantics of contains predicate but it is useless
//                            // if one cannot do a search without (instead of just a strict match)
//                            Query propQuery = null;
//                            if (encodedValue.endsWith( "*" ) 
//                                    && StringUtils.countMatches( encodedValue, "*" ) == 1
//                                    && StringUtils.countMatches( encodedValue, "?" ) == 0) {
//                                propQuery = new PrefixQuery( new Term( fieldname, encodedValue.substring( 0, encodedValue.length()-1 ) ) );
//                            }
//                            else if (StringUtils.countMatches( encodedValue, "*" ) > 1
//                                    || StringUtils.countMatches( encodedValue, "?" ) > 0) {
//                                propQuery = new WildcardQuery( new Term( fieldname, encodedValue ) );
//                            }
//                            else {
//                                propQuery = new TermQuery( new Term( fieldname, encodedValue ) );
//                            }
//                            
//                            valueQuery.add( propQuery, BooleanClause.Occur.MUST );
//                        }
//                    }
//                });
//    
//                result.add( valueQuery, BooleanClause.Occur.SHOULD );
//            }
//            return result;
//        }
    
    
//    /**
//     * Build the field name for the Lucene query. 
//     */
//    protected String property2Fieldname( PropertyReference property ) {
////        Class type = property.propertyType();
////        Class declaringType = property.propertyDeclaringType();
////        Method accessor = property.propertyAccessor();
//
//        String prefix = "";
//        PropertyReference traversedProperty = property.traversedProperty();
//        if (traversedProperty != null) {
//            prefix = property2Fieldname( traversedProperty ) + LuceneEntityState.SEPARATOR_PROP;
//        }
//        AssociationReference traversedAssoc = property.traversedAssociation();
//        if (traversedAssoc != null) {
//            throw new UnsupportedOperationException( "Traversed association in query. (Property:" + property.propertyName() + ")" );
//        }
//        
//        return prefix + property.propertyName();
//    }

}
