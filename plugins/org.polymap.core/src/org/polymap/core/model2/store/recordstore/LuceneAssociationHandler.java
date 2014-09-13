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
import org.apache.lucene.search.Query;

import org.polymap.core.model2.engine.TemplateProperty;
import org.polymap.core.model2.query.grammar.AssociationEquals;
import org.polymap.core.model2.query.grammar.IdPredicate;
import org.polymap.core.runtime.recordstore.QueryExpression;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneAssociationHandler
        extends LuceneExpressionHandler<AssociationEquals> {

    private static Log log = LogFactory.getLog( LuceneAssociationHandler.class );

    @Override
    public Query handle( AssociationEquals expression ) {
        // Id
        if (expression.children[0] instanceof IdPredicate) {
            Object id = ((IdPredicate)expression.children[0]).id;
            TemplateProperty assoc = expression.assoc;
            String fieldname = LuceneQueryBuilder.fieldname( assoc ).toString();
            return builder.valueCoders.searchQuery( new QueryExpression.Equal( fieldname, id ) ); 
        }
        // sub-expression
        else {
            throw new RuntimeException( "Sub-queries are not supported yet." );
        }
    }
    
}
