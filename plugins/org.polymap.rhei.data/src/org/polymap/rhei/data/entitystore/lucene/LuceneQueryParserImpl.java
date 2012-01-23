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
package org.polymap.rhei.data.entitystore.lucene;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.StateHolder.StateVisitor;
import org.qi4j.api.query.grammar.AssociationNullPredicate;
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.ComparisonPredicate;
import org.qi4j.api.query.grammar.Conjunction;
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.Disjunction;
import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.api.query.grammar.GreaterThanPredicate;
import org.qi4j.api.query.grammar.LessOrEqualPredicate;
import org.qi4j.api.query.grammar.LessThanPredicate;
import org.qi4j.api.query.grammar.ManyAssociationContainsPredicate;
import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.query.grammar.Negation;
import org.qi4j.api.query.grammar.NotEqualsPredicate;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.PropertyNullPredicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.ValueExpression;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.spi.property.PropertyType;

import com.google.common.base.Joiner;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.IRecordStore.ResultSet;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;
import org.polymap.core.runtime.recordstore.lucene.ValueCoders;

/**
 * Converts Qi4j queries into Lucene queries.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LuceneQueryParserImpl {

    private static Log log = LogFactory.getLog( LuceneQueryParserImpl.class );

    private static final Query          ALL = new MatchAllDocsQuery();

    private LuceneRecordStore           store;
    

    public LuceneQueryParserImpl( LuceneRecordStore store ) {
        this.store = store;
    }


    public Query createQuery( final String resultType, final BooleanExpression whereClause,
            final OrderBy[] orderBySegments ) {

        Query filterQuery = processFilter( whereClause, resultType );

        Query typeQuery = new TermQuery( new Term( "type", resultType ) );
        Query result = null;
        if (!filterQuery.equals( ALL )) {
            result = new BooleanQuery();
            ((BooleanQuery)result).add( filterQuery, BooleanClause.Occur.MUST );
            ((BooleanQuery)result).add( typeQuery, BooleanClause.Occur.MUST );
        }
        else {
            result = typeQuery;
        }

        if (orderBySegments != null) {
            throw new UnsupportedOperationException( "Not implemented yet: orderBySegments" );
        }

        log.info( "    LUCENE query: [" + result.toString() + "]" );
        return result;
    }


    protected Query processFilter( final BooleanExpression expression, String resultType ) {
        // start
        if (expression == null) {
            return ALL;
        }
        // AND
        else if (expression instanceof Conjunction) {
            final Conjunction conjunction = (Conjunction)expression;
            Query left = processFilter( conjunction.leftSideExpression(), resultType );
            Query right = processFilter( conjunction.rightSideExpression(), resultType );

            if (left.equals( ALL )) {
                log.warn( "Operant of conjunction is empty!" );
                return right;
            }
            else if (right.equals( ALL )) {
                log.warn( "Operant of conjunction is empty!" );
                return left;
            }
            else {
                BooleanQuery result = new BooleanQuery();
                result.add( left, BooleanClause.Occur.MUST );
                result.add( right, BooleanClause.Occur.MUST );
                return result;
            }
        }
        // OR
        else if (expression instanceof Disjunction) {
            Disjunction disjunction = (Disjunction)expression;
            Query left = processFilter( disjunction.leftSideExpression(), resultType );
            Query right = processFilter( disjunction.rightSideExpression(), resultType );

            if (left.equals( ALL )) {
                log.warn( "Operant of disjunction is empty!" );
                return right;
            }
            else if (right.equals( ALL )) {
                log.warn( "Operant of disjunction is empty!" );
                return left;
            }
            else {
                BooleanQuery result = new BooleanQuery();
                result.add( left, BooleanClause.Occur.SHOULD );
                result.add( right, BooleanClause.Occur.SHOULD );
                return result;
            }
        }
        // NOT
        else if (expression instanceof Negation) {
            Query arg = processFilter( ((Negation)expression).expression(), resultType );
            BooleanQuery result = new BooleanQuery();
            result.add( arg, BooleanClause.Occur.MUST_NOT );
            return result;
        }
        // comparison
        else if (expression instanceof ComparisonPredicate) {
            return processComparisonPredicate( (ComparisonPredicate)expression );
        }
        // MANY Assoc
        else if (expression instanceof ManyAssociationContainsPredicate) {
            throw new UnsupportedOperationException( "ManyAssociationContainsPredicate" );
        }
        // IS NULL
        else if (expression instanceof PropertyNullPredicate) {
            throw new UnsupportedOperationException( "PropertyNullPredicate" );
        }
        // Assoc
        else if (expression instanceof AssociationNullPredicate) {
            throw new UnsupportedOperationException( "AssociationNullPredicate" );
        }
        // contains
        else if (expression instanceof ContainsPredicate) {
            return processContainsPredicate( (ContainsPredicate)expression, resultType );
        }
        else {
            throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
        }
    }


    /**
     * Handle the contains predicate.
     * <p/>
     * Impl. note: This needs a patch in
     * org.qi4j.runtime.query.grammar.impl.PropertyReferenceImpl<T> to work with
     * Qi4j 1.0.
     */
    protected Query processContainsPredicate( ContainsPredicate predicate, String resultType ) {
        final ValueCoders valueCoders = store.getValueCoders();

        PropertyReference property = predicate.propertyReference();
        final String baseFieldname = property2Fieldname( property );
        SingleValueExpression valueExpression = (SingleValueExpression)predicate.valueExpression();

        //
        int maxElements = 10;
        try {
            Timer timer = new Timer();
            String lengthFieldname = baseFieldname + "__length";
            SimpleQuery query = new SimpleQuery().setMaxResults( 1 )
                    .eq( "type", resultType )
                    .sort( lengthFieldname, SimpleQuery.DESC, Integer.class );
            ResultSet lengthResult = store.find( query );
            IRecordState biggest = lengthResult.get( 0 );
            maxElements = biggest.get( lengthFieldname );
            log.info( "    LUCENE: maxLength query: result: " + maxElements + " (" + timer.elapsedTime() + "ms)" );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        
        //
        BooleanQuery result = new BooleanQuery();
        for (int i=0; i<maxElements; i++) {
            final BooleanQuery valueQuery = new BooleanQuery();

            final ValueComposite value = (ValueComposite)valueExpression.value();
            ValueModel valueModel = (ValueModel)ValueInstance.getValueInstance( value ).compositeModel();
            List<PropertyType> actualTypes = valueModel.valueType().types();
            //                    json.key( "_type" ).value( valueModel.valueType().type().name() );


            // all properties of the value
            final int index = i;
            value.state().visitProperties( new StateVisitor() {
                public void visitProperty( QualifiedName name, Object propValue ) {
                    if (propValue == null) {
                    }
                    else if (propValue.toString().equals( "-1" )) {
                        // FIXME hack to signal that this non-optional(!) value is not to be considered
                        log.warn( "Non-optional field ommitted: " + name.name() + ", value=" + propValue );
                    }
                    else {
                        String fieldname = Joiner.on( "" ).join( 
                                baseFieldname, "[", index, "]", 
                                LuceneEntityState.SEPARATOR_PROP, name.name() );
                        
                        //Property<Object> fieldProp = value.state().getProperty( name );

//                      // this might not be the semantics of contains predicate but it is useless
//                      // if one cannot do a search without (instead of just a strict match)
                        Query propQuery = propValue instanceof String
                                && !StringUtils.containsNone( (String)propValue, "*?")
                                ? valueCoders.searchQuery( new QueryExpression.Match( fieldname, propValue ) ) 
                                : valueCoders.searchQuery( new QueryExpression.Equal( fieldname, propValue ) ); 

                        valueQuery.add( propQuery, BooleanClause.Occur.MUST );
                    }
                }
            });

            result.add( valueQuery, BooleanClause.Occur.SHOULD );
        }
        return result;
    }


    protected Query processComparisonPredicate( ComparisonPredicate predicate ) {
        PropertyReference property = predicate.propertyReference();
        String fieldname = property2Fieldname( property );

        ValueExpression valueExpression = predicate.valueExpression();

        if (valueExpression instanceof SingleValueExpression) {
            
            Object value = ((SingleValueExpression)valueExpression).value();
            Class valueType = property.propertyType();
            ValueCoders valueCoders = store.getValueCoders();

            // eq
            if (predicate instanceof EqualsPredicate) {
                return valueCoders.searchQuery( new QueryExpression.Equal( fieldname, value ) ); 
            }
            // neq
            if (predicate instanceof NotEqualsPredicate) {
                Query query = valueCoders.searchQuery( new QueryExpression.Equal( fieldname, value ) ); 
                BooleanQuery result = new BooleanQuery();
                result.add( query, BooleanClause.Occur.MUST_NOT );
                return result;
            }
            // ge
            else if (predicate instanceof GreaterOrEqualPredicate) {
                return valueCoders.searchQuery( new QueryExpression.GreaterOrEqual( fieldname, value ) ); 
            }
            // gt
            else if (predicate instanceof GreaterThanPredicate) {
                return valueCoders.searchQuery( new QueryExpression.Greater( fieldname, value ) ); 
            }
            // le
            else if (predicate instanceof LessOrEqualPredicate) {
                return valueCoders.searchQuery( new QueryExpression.LessOrEqual( fieldname, value ) ); 
            }
            // lt
            else if (predicate instanceof LessThanPredicate) {
                return valueCoders.searchQuery( new QueryExpression.Less( fieldname, value ) ); 
            }
            // matches
            else if (predicate instanceof MatchesPredicate) {
                return valueCoders.searchQuery( new QueryExpression.Match( fieldname, value ) ); 
            }
            else {
                throw new UnsupportedOperationException( "Predicate type not supported in comparison: " + predicate );
            }
        }
        else {
            throw new UnsupportedOperationException( "Value expression type not supported:" + valueExpression );
        }
    }


    /**
     * Build the field name for the Lucene query.
     */
    protected String property2Fieldname( PropertyReference property ) {
//        Class type = property.propertyType();
//        Class declaringType = property.propertyDeclaringType();
//        Method accessor = property.propertyAccessor();

        String prefix = "";
        PropertyReference traversedProperty = property.traversedProperty();
        if (traversedProperty != null) {
            prefix = Joiner.on( "" ).join(
                    property2Fieldname( traversedProperty ),
                    LuceneEntityState.SEPARATOR_PROP );
        }
        AssociationReference traversedAssoc = property.traversedAssociation();
        if (traversedAssoc != null) {
            throw new UnsupportedOperationException( "Traversed association in query. (Property:" + property.propertyName() + ")" );
        }

        return Joiner.on( "" ).join( prefix, property.propertyName() );
    }

}
