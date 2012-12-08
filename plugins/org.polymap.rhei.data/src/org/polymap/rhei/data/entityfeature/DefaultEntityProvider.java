/*
 * polymap.org
 * Copyright 2011, Falko Br�utiga, and individual contributors as
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
 *
 * $Id$
 */
package org.polymap.rhei.data.entityfeature;

import org.opengis.feature.type.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public abstract class DefaultEntityProvider<T extends Entity>
        implements EntityProvider<T> {

    private static Log log = LogFactory.getLog( DefaultEntityProvider.class );

    protected QiModule              repo;

    protected EntityType<T>         type;

    protected Name                  name;

    private FidsQueryProvider       queryProvider;


    public DefaultEntityProvider( QiModule repo, Class<T> entityClass, Name entityName,
            FidsQueryProvider queryProvider ) {
        this.repo = repo;
        this.type = repo.entityType( entityClass );
        this.name = entityName;
        this.queryProvider = queryProvider;
    }


    public FidsQueryProvider getQueryProvider() {
        return queryProvider;
    }


    public Name getEntityName() {
        return name;
    }


    public EntityType getEntityType() {
        return type;
    }


    public Iterable<T> entities( BooleanExpression query, int firstResult, int maxResults ) {
        // special FidsQueryExpression
        if (query instanceof FidsQueryExpression) {
            return ((FidsQueryExpression)query).entities( repo, type.getType(), firstResult, maxResults );
        }
        // regular query
        else {
            return repo.findEntities( type.getType(), query, firstResult, maxResults );
        }
    }


    public int entitiesSize( BooleanExpression query, int firstResult, int maxResults ) {
        // FidsQueryExpression
        if (query instanceof FidsQueryExpression) {
            // assuming that the fids exist
            return Math.min( maxResults - firstResult,
                    ((FidsQueryExpression)query).entitiesSize() );
        }
        // regular query
        else {
            // XXX cache result for subsequent entities() call?
            Query result = repo.findEntities( type.getType(), query, firstResult, maxResults );
            return (int)result.count();
        }
    }


    public T newEntity( EntityCreator<T> creator )
    throws Exception {
        // FIXME: operation bounds are handled by AntragOperationConcern !?
        return repo.newEntity( type.getType(), null, creator );
    }


    public T findEntity( String id ) {
        return repo.findEntity( type.getType(), id );
    }

}
