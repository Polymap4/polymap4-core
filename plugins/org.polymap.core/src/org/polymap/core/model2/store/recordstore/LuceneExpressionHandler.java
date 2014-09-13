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

import org.apache.lucene.search.Query;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.TemplateProperty;
import org.polymap.core.model2.query.grammar.BooleanExpression;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class LuceneExpressionHandler<T extends BooleanExpression> {

    protected LuceneQueryBuilder        builder;
    
    protected Class<? extends Entity>   resultType;
    

    public abstract Query handle( T expression );
    
    
    /**
     * @see LuceneQueryBuilder#fieldname(org.polymap.core.model2.engine.TemplateProperty)
     */
    public static StringBuilder fieldname( TemplateProperty property ) {
        return LuceneQueryBuilder.fieldname( property );
    }

}
