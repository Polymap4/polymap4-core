/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.model2.store.recordstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import org.polymap.core.model2.engine.TemplateProperty;
import org.polymap.core.model2.query.grammar.ComparisonPredicate;
import org.polymap.core.model2.query.grammar.PropertyEquals;
import org.polymap.core.model2.query.grammar.PropertyMatches;
import org.polymap.core.model2.query.grammar.PropertyNotEquals;
import org.polymap.core.runtime.recordstore.QueryExpression;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LuceneComparisonHandler
        extends LuceneExpressionHandler<ComparisonPredicate> {

    private static Log log = LogFactory.getLog( LuceneComparisonHandler.class );

    
    @Override
    public Query handle( ComparisonPredicate predicate ) {
        TemplateProperty prop = predicate.prop;
        Object value = predicate.value;
        if (value instanceof Enum) {
            value = value.toString();
        }
        String fieldname = LuceneQueryBuilder.fieldname( prop ).toString();

        // eq
        if (predicate instanceof PropertyEquals) {
            return builder.valueCoders.searchQuery( new QueryExpression.Equal( fieldname, value ) ); 
        }
        // notEq
        if (predicate instanceof PropertyNotEquals) {
            Query query = builder.valueCoders.searchQuery( new QueryExpression.Equal( fieldname, value ) ); 
            BooleanQuery result = new BooleanQuery();
            result.add( LuceneQueryBuilder.ALL, BooleanClause.Occur.SHOULD );
            result.add( query, BooleanClause.Occur.MUST_NOT );
            return result;
        }
//        // ge
//        else if (predicate instanceof GreaterOrEqualPredicate) {
//            return valueCoders.searchQuery( new QueryExpression.GreaterOrEqual( fieldname, value ) ); 
//        }
//        // gt
//        else if (predicate instanceof GreaterThanPredicate) {
//            return valueCoders.searchQuery( new QueryExpression.Greater( fieldname, value ) ); 
//        }
//        // le
//        else if (predicate instanceof LessOrEqualPredicate) {
//            return valueCoders.searchQuery( new QueryExpression.LessOrEqual( fieldname, value ) ); 
//        }
//        // lt
//        else if (predicate instanceof LessThanPredicate) {
//            return valueCoders.searchQuery( new QueryExpression.Less( fieldname, value ) ); 
//        }
        // matches
        else if (predicate instanceof PropertyMatches) {
            return builder.valueCoders.searchQuery( new QueryExpression.Match( fieldname, value ) ); 
        }
        else {
            throw new UnsupportedOperationException( "Predicate type not supported in comparison: " + predicate );
        }
    }

}
