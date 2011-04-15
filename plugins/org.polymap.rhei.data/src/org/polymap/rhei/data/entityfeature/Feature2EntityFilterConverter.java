/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * $Id$
 */
package org.polymap.rhei.data.entityfeature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.Or;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;

/**
 * 
 * <p/>
 * Impl. note: just a skeleton, work in progress...
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class Feature2EntityFilterConverter {

    private static final Log log = LogFactory.getLog( Feature2EntityFilterConverter.class );

    private EntityType      entityType; 
    
    
    
    public Feature2EntityFilterConverter( EntityType entityType ) {
        this.entityType = entityType;
    }


    public BooleanExpression convert( Filter filter ) {
        BooleanExpression result = null;
        
        // AND
        if (filter instanceof And) {
            result = new ListConverter( ((And)filter).getChildren() ) {
                BooleanExpression expression( BooleanExpression lhs, BooleanExpression rhs, BooleanExpression... opt ) {
                    return QueryExpressions.and( lhs, rhs, opt );
                }
            }.convert();
        }
        // OR
        else if (filter instanceof Or) {
            result = new ListConverter( ((Or)filter).getChildren() ) {
                BooleanExpression expression( BooleanExpression lhs, BooleanExpression rhs, BooleanExpression... opt ) {
                    return QueryExpressions.or( lhs, rhs, opt );
                }
            }.convert();
        }
        // Id
        else if (filter instanceof Id) {
            result = processFidFilter( (Id)filter );
        }

//        else if (filter instanceof PropertyIsEqualTo) {
//            QueryExpressions.e
//            (PropertyIsEqualTo)filter );
//        }
        log.info( "Entity query: " + result );
        return result;
    }

    
    protected BooleanExpression processFidFilter( Id filter ) {
        Set<String> fids = new HashSet();
        for (Identifier id : filter.getIdentifiers()) {
            fids.add( id.toString() );
        }
        return new EntityProvider.FidsQueryExpression( fids );
        
//        Entity template = QueryExpressions.templateFor( entityType.getType() );
//        
//        // build disjunction
//        Id fidFilter = (Id)filter;            
//        List<BooleanExpression> fidExpressions = new ArrayList<BooleanExpression>();
//        for (Identifier fid : fidFilter.getIdentifiers()) {
//            fidExpressions.add( QueryExpressions.eq( 
//                    ((Identity)template).identity(), (String)fid.getID() ) );
//        }
//        
//        switch (fidExpressions.size()) {
//            case 1 : {
//                return fidExpressions.get( 0 );
//            }
//            case 2 : {
//                return QueryExpressions.or( fidExpressions.get( 0 ), fidExpressions.get( 1 ) );
//            }
//            default : {
//                List<BooleanExpression> tail = fidExpressions.subList( 2, fidExpressions.size()-2 );
//                return QueryExpressions.or( 
//                        fidExpressions.get( 0 ),
//                        fidExpressions.get( 1 ),
//                        tail.toArray( new BooleanExpression[tail.size()] ) );
//            }
//        }
    }


    /**
     * Handles the spezial Qi4j query API with left + right + optional
     * arguments.
     */
    abstract class ListConverter {
        
        abstract BooleanExpression expression( BooleanExpression lhs, BooleanExpression rhs, BooleanExpression... opt);
 
        private List<Filter>        filters;
        
        
        public ListConverter( List<Filter> filters ) {
            this.filters = filters;    
        }
        
        public BooleanExpression convert() {
            Entity template = QueryExpressions.templateFor( entityType.getType() );
            
            // build children
            List<BooleanExpression> children = new ArrayList<BooleanExpression>();
            for (Filter filter : filters) {
                children.add( Feature2EntityFilterConverter.this.convert( filter ) );
            }
            
            switch (children.size()) {
                case 0 : {
                    return null;
                }
                case 1 : {
                    return children.get( 0 );
                }
                case 2 : {
                    return expression( children.get( 0 ), children.get( 1 ) );
                }
                default : {
                    List<BooleanExpression> tail = children.subList( 2, children.size()-2 );
                    return expression( 
                            children.get( 0 ),
                            children.get( 1 ),
                            tail.toArray( new BooleanExpression[tail.size()] ) );
                }
            }
        }
        
    }

}
