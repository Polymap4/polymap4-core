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

import org.polymap.core.model2.query.grammar.BooleanExpression;
import org.polymap.core.model2.query.grammar.Conjunction;
import org.polymap.core.model2.query.grammar.Disjunction;
import org.polymap.core.model2.query.grammar.Negation;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneJunctionHandler
        extends LuceneExpressionHandler<BooleanExpression> {

    private static Log log = LogFactory.getLog( LuceneJunctionHandler.class );

    
    @Override
    public Query handle( BooleanExpression expression ) {
        // AND
        if (expression instanceof Conjunction) {
            Conjunction conjunction = (Conjunction)expression;
            BooleanQuery result = new BooleanQuery();
            for (BooleanExpression child : conjunction.children) {
                Query left = builder.processExpression( child, resultType );
                result.add( left, BooleanClause.Occur.MUST );                
            }
            return result;
        }
        // OR
        else if (expression instanceof Disjunction) {
            Disjunction disjunction = (Disjunction)expression;
            BooleanQuery result = new BooleanQuery();
            for (BooleanExpression child : disjunction.children) {
                Query left = builder.processExpression( child, resultType );
                result.add( left, BooleanClause.Occur.SHOULD );                
            }
            return result;
        }
        // NOT
        else if (expression instanceof Negation) {
            Query arg = builder.processExpression( ((Negation)expression).children[0], resultType );
            BooleanQuery result = new BooleanQuery();
            result.add( arg, BooleanClause.Occur.MUST_NOT );
            return result;
        }
        return null;
    }

}
