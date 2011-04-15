/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
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
 * $Id: $
 */
package org.polymap.rhei.data.entityfeature;

import java.util.HashSet;
import java.util.Set;

import org.opengis.feature.type.Name;

import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.NamedQueryExpression;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule;

/**
 * Provides access to a certain entity type. {@link EntityType} interface for
 * use in {@link EntitySourceProcessor}. It provides factory and retrieval
 * methods. It can also be used to provide an 'feature view' for a given entity
 * type.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public interface EntityProvider<T extends Entity> {

    /**
     * Secial kind of query that is used to request a set of entities that was
     * already queried. This query contains the identities of those entities.
     * The {@link EntityProvider} should check this and straight load the
     * entities via {@link QiModule#findEntity(Class, String)}.
     */
    public class FidsQueryExpression
            implements NamedQueryExpression {

        private Set<String> fids = new HashSet();
        
        
        public FidsQueryExpression( Set<String> fids ) {
            this.fids = fids;
        }

        public String name() {
            return "FidsQueryExpression";
        }

        public Set<String> fids() {
            return fids;    
        }
        
        public boolean eval( Object target ) {
            throw new RuntimeException( "not yet implemented." );
        }
        
    }
    

    public Name getEntityName();
    
    public EntityType getEntityType();

    /**
     * 
     * @param entityQuery The query to execute. This may also be a
     *        {@link FidsQueryExpression}.
     * @param firstResult
     * @param maxResults
     */
    public Iterable<T> entities( BooleanExpression entityQuery, int firstResult, int maxResults );

    public int entitiesSize( BooleanExpression entityQuery, int firstResult, int maxResults );

    public T newEntity();

    public T findEntity( String id );

}
