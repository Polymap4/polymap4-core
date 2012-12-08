/*
 * polymap.org
 * Copyright 2010, 2012 Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.data.entityfeature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.data.Query;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.collections.ListUtils;

import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.NamedQueryExpression;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;

/**
 * Provides access to a certain entity type. {@link EntityType} interface for
 * use in {@link EntitySourceProcessor}. It provides factory and retrieval
 * methods. It can also be used to provide an 'feature view' for a given entity
 * type.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public interface EntityProvider<T extends Entity> {

    /**
     * Backend store dependent converter from OGC {@link Filter} into Qi4j specific
     * {@link FidsQueryExpression}.
     */
    public interface FidsQueryProvider {
        
        public FidsQueryExpression convert( Query input, FeatureType schema, EntityType entityType )
        throws Exception;
        
    }
    
    /**
     * Secial kind of query that is used to request a set of entities that was
     * already queried. This query contains the identities of those entities.
     * The {@link EntityProvider} should check this and straight load the
     * entities via {@link QiModule#findEntity(Class, String)}.
     */
    public class FidsQueryExpression
            implements NamedQueryExpression {

        private Set<String>     fids = new HashSet();
        
        /** Filters that cannot be translated by the {@link FidsQueryProvider}. */
        private List<Filter>    notQueryable;


        public FidsQueryExpression( Set<String> fids, List<Filter> notQueryable ) {
            this.fids = fids;
            this.notQueryable = notQueryable != null ? notQueryable : ListUtils.EMPTY_LIST;
        }

        
        public <E> Iterable<E> entities( final QiModule repo, final Class<E> type, int firstResult, int maxResults ) {
            // fids -> entities
            Iterable<E> result = Iterables.transform( fids, new Function<String,E>() {
                public E apply( String fid ) {
                    return repo.findEntity( type, fid );                
                }
            });
            // firstResult
            if (firstResult > 0) {
                result = Iterables.skip( result, firstResult );
            }
            // maxResults
            if (maxResults < Integer.MAX_VALUE) {
                result = Iterables.limit( result, maxResults );
            }
            return result;    
        }
        

        public int entitiesSize() {
            return fids.size();
        }

        
        /**
         * Called by {@link EntitySourceProcessor} after the features were build from this
         * query. This allows to filter {@link #notQueryable} filters 'by hand'.
         */
        public Feature deferredFilterFeature( Feature feature ) {
            for (Filter filter : notQueryable) {
                if (!filter.evaluate( feature )) {
                    return null;
                }
            }
            return feature;
        }

        public String name() {
            return "FidsQueryExpression";
        }

        /** Filters that cannot be translated by the {@link FidsQueryProvider}. */
        public List<Filter> notQueryable() {
            return notQueryable;
        }

        public boolean eval( Object target ) {
            throw new RuntimeException( "not yet implemented." );
        }

    }

    
    /**
     * The query provider, or null if no such provider was registered.
     */
    public FidsQueryProvider getQueryProvider();

    public Name getEntityName();

    public EntityType getEntityType();

    public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName );

    public String getDefaultGeometry();

    public ReferencedEnvelope getBounds();

    /**
     *
     * @param entityQuery The query to execute. This may also be a
     *        {@link FidsQueryExpression}.
     * @param firstResult
     * @param maxResults
     */
    public Iterable<T> entities( BooleanExpression entityQuery, int firstResult, int maxResults );

    public int entitiesSize( BooleanExpression entityQuery, int firstResult, int maxResults );

    /**
     *
     * @param creator
     * @return The newly created entity
     * @throws Exception If an exception occured while executing the creator.
     */
    public T newEntity( EntityCreator<T> creator )
    throws Exception;

    public T findEntity( String id );

}
