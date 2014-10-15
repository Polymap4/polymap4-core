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

import org.polymap.core.model2.query.grammar.IdPredicate;
import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LuceneIdHandler
        extends LuceneExpressionHandler<IdPredicate> {

    private static Log log = LogFactory.getLog( LuceneIdHandler.class );

    
    @Override
    public Query handle( IdPredicate predicate ) {
//        if (fidFilter.getIdentifiers().size() > BooleanQuery.getMaxClauseCount()) {
//            BooleanQuery.setMaxClauseCount( fidFilter.getIdentifiers().size() );
//        }

        BooleanQuery result = new BooleanQuery();
        org.apache.lucene.search.Query fidQuery = builder.valueCoders.searchQuery( 
                new QueryExpression.Equal( LuceneRecordState.ID_FIELD, predicate.id ) );
        result.add( fidQuery, BooleanClause.Occur.SHOULD );
        return result;
    }

}
