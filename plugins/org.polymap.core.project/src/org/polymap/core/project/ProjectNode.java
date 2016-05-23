/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.project;

import static org.polymap.model2.query.Expressions.and;
import static org.polymap.model2.query.Expressions.eq;
import static org.polymap.model2.query.Expressions.is;

import java.util.EventObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.security.SecurityContext;

import org.polymap.model2.Association;
import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Concerns;
import org.polymap.model2.DefaultValue;
import org.polymap.model2.Defaults;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.PropertyConcern;
import org.polymap.model2.PropertyConcernAdapter;
import org.polymap.model2.Queryable;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.ResultSet;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.Lifecycle;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.event.PropertyChangeSupport;

/**
 * Provides a mixin for {@link ILayer} and {@link IMap} defining them as part of a
 * hierarchy of maps.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ProjectNode
        extends Entity
        implements Lifecycle {

    private static Log log = LogFactory.getLog( ProjectNode.class );

    // instance *******************************************
    
    @Queryable
    public Property<String>             label;

    @Defaults
    @Queryable
    public CollectionProperty<String>   keywords;

    public Association<IMap>            parentMap;

    
    @Override
    public String id() {
        return (String)super.id();
    }


    public UnitOfWork belongsTo() {
        return context.getUnitOfWork();
    }
    
    
    @Override
    public void onLifecycleChange( State state ) {
        if (state == State.AFTER_COMMIT) {
            EventManager.instance().publish( new ProjectNodeCommittedEvent( this ) );
        }
    }

    
    /**
     * 
     */
    protected <N extends ProjectNode,U extends UserSettings> 
            U findUserSettings( Class<U> userSettingsType, Association<N> backAssoc ) {
        
        String username = SecurityContext.instance().getUser().getName();
        
        // give every instance its own UnitOfWork; so modifications can be
        // AutoCommit without interfering with the main UnitOfWork
        EntityRepository repo = context.getRepository();
        UnitOfWork uow = repo.newUnitOfWork();
        
        U template = Expressions.template( userSettingsType, repo );
        ResultSet<U> rs = uow.query( userSettingsType )
                .where( and(
                        is( backAssoc, (N)ProjectNode.this ),
                        eq( template.username, username ) ) )
                .maxResults( 2 )
                .execute();
        assert rs.size() >= 0 || rs.size() <= 1 : "Illegal result set size: " + rs.size();
        
        // not found
        if (rs.size() == 0) {
            U result = uow.createEntity( userSettingsType, null, (U proto) -> {
                ProjectNode localNode = uow.entity( ProjectNode.this );
                ((Association)backAssoc.info().get( proto )).set( localNode );
                proto.username.set( username );
                return proto;
            });
            uow.commit();
            return uow.entity( result );
        }
        // found
        else {
            return rs.stream().findAny().get();
        }
    }
    
    
    /**
     * 
     */
    public static class UserSettings
            extends Entity {

        protected Property<String>       username;
        
        /**
         * True if the layer is visible in the map. Setting this property usually triggers
         * some map refresh in the application.
         */
        @DefaultValue( "true" )
        @Concerns( {PropertyChangeSupport.class, AutoCommit.class} )
        public Property<Boolean>         visible;
    }

    
    /**
     * 
     */
    public static class AutoCommit
            extends PropertyConcernAdapter
            implements PropertyConcern {

        @Override
        public void set( Object value ) {
            super.set( value );            
            // every instance has its own UnitOfWork; see ProjectNode#userSetting()
            context.getUnitOfWork().commit();
        }
    }


    /**
     * 
     */
    public static class ProjectNodeCommittedEvent
            extends EventObject {

        public ProjectNodeCommittedEvent( ProjectNode source ) {
            super( source );
        }
        
        /**
         * The {@link ProjectNode} on which the event <b>initially</b> occured. In
         * most cases this Entity is now <b>detached</b> and must not be used any
         * longer.
         * <p/>
         * Use {@link #getEntity()} to get an instance of the target
         * {@link UnitOfWork}.
         * 
         * @see #getEntity(UnitOfWork)
         */
        @Override
        public ProjectNode getSource() {
            return (ProjectNode)super.getSource();
        }
        
        /**
         * Returns the {@link #getSource() source} Entity of the event
         * {@link UnitOfWork#entity(Entity) belonging to} the given target
         * {@link UnitOfWork}.
         *
         * @param uow The target {@link UnitOfWork}.
         */
        public <T extends ProjectNode> T getEntity( UnitOfWork uow ) {
            assert uow != null;
            return (T)uow.entity( getSource() );
        }
    }
    
}
