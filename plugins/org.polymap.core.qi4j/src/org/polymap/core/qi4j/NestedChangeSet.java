/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.qi4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.runtime.types.ValueTypeFactory;
import org.qi4j.spi.property.ValueType;

import org.polymap.core.model.Entity;
import org.polymap.core.model.ModelChangeSet;

/**
 * Provides the nested {@link UnitOfWork} semantics.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class NestedChangeSet
        implements ModelChangeSet {

    private static Log log = LogFactory.getLog( NestedChangeSet.class );

    private static LinkedList<NestedChangeSet>  stack = new LinkedList();
    
    
    public static NestedChangeSet newInstance( UnitOfWork uow ) {
        stack.addLast( new NestedChangeSet( uow ) );
        return instance();
    }
    
    public static NestedChangeSet instance() {
        return stack.getLast();
    }

    
    // instance ***
    
    /** Maps composite Id into composite state. */
    private Map<String,CompositeState>  updated = new HashMap();
    
    private Map<String,CompositeState>  created = new HashMap();
    
    private Map<String,CompositeState>  removed = new HashMap();
    
    private boolean                     rollingBack;
    
    private UnitOfWork                  uow;
    
    
    public NestedChangeSet( UnitOfWork uow ) {
        super();
        this.uow = uow;
    }

    
    public Map<String,Entity> entities() {
        Map<String,Entity> result = new HashMap();
        // updated
        for (CompositeState state : updated.values()) {
            try {
                Entity entity = (Entity)uow.get( state.ctype, state.id );
                result.put( state.id, entity );
            }
            catch (NoSuchEntityException e) {
                // the entity may have changed and later removed
                log.info( "Entity was changed and later removed: " + state.id );
            }
        }
        
        // created
        for (CompositeState state : created.values()) {
            try {
                Object obj = uow.get( state.ctype, state.id );
                Entity entity = (Entity)obj;
                result.put( state.id, entity );
            }
            catch (NoSuchEntityException e) {
                // the entity may have created and later removed
                log.info( "Entity was created and later removed: " + state.id );
            }
        }
        
        // removed
        if (!removed.isEmpty()) {
            log.info( "XXX ChangeSet: removed entities: " + removed.size() );
        }
//        for (CompositeState state : removed.values()) {
//            //Entity entity = (Entity)uow.get( state.ctype, state.id );
//            result.put( state.id, entity );
//        }
        return result;   
    }

    
    public Set<String> ids() {
        Set<String> result = new HashSet();
        result.addAll( updated.keySet() );
        result.addAll( created.keySet() );
        result.addAll( removed.keySet() );
        return result;   
    }

    
    public synchronized boolean hasChanges( String id ) {
        return updated.containsKey( id ) || removed.containsKey( id );
    }
    
    
    public synchronized void compositeUpdate( Identity composite ) {
        if (rollingBack) {
            log.debug( "Rolling back. skipping updates." );
            return;
        }
        log.debug( "Update composite: " + composite.getClass().getName() );
        String id = composite.identity().get();

        if (!updated.containsKey( id ) && !created.containsKey( id )) {
            updated.put( id, new CompositeState( id, composite ) );
        }
    }

    public synchronized void compositeCreate( Identity composite ) {
        if (rollingBack) {
            log.debug( "Rolling back. skipping updates." );
            return;
        }
        log.debug( "Create composite: " + composite.getClass().getName() );
        String id = composite.identity().get();
        created.put( id, new CompositeState( id, composite ) );
    }

    public synchronized void compositeRemove( Identity composite ) {
        if (rollingBack) {
            log.debug( "Rolling back. skipping updates." );
            return;
        }
        log.debug( "Remove composite: " + composite.getClass().getName() );
        String id = composite.identity().get();
        if (created.containsKey( id )) {
            log.info( "Entity was created in this changeSet -> skipping." );
        }
        else {
            removed.put( id, new CompositeState( id, composite ) );
        }
    }

    
    public synchronized void discard() {
        try {
            rollingBack = true;
            // recreate removed entities first
            for (CompositeState state : removed.values()) {
                log.debug( "Rollback removed entity: " + state.id );
                uow.newEntity( state.ctype, state.id );
                state.rollback( uow );
            }
            // updates
            for (CompositeState state : updated.values()) {
                log.debug( "Rollback changed entity: " + state.id );
                state.rollback( uow );
            }
            // last remove newly created entities
            for (CompositeState state : created.values()) {
                log.debug( "Rollback created entity: " + state.id );
                Object entity = uow.get( state.ctype, state.id );
                uow.remove( entity );
            }
        }
        finally {
            rollingBack = false;
        }
    }
    
    
    /**
     * 
     */
    static class CompositeState {
    
        private static final String NO_SUCH_ENTITY = "__NO_SUCH_ENTITY__";
        
        String                      id;
        
        Class                       ctype;
        
        /** Maps property name into into json object. */
        private Map<String,Object>  properties = new HashMap();
        
        
        public CompositeState( String id, Object composite ) {
            this.id = id;
            this.ctype = ((EntityComposite)composite).type();
            
            for (Method m : composite.getClass().getMethods()) {
                if (m.getName().equals( "identity" ) ) {
                    continue;
                }
                try {
                    // ManyAssociation
                    if (ManyAssociation.class.isAssignableFrom( m.getReturnType() )) {
                        ManyAssociation assoc = (ManyAssociation)m.invoke( composite, ArrayUtils.EMPTY_OBJECT_ARRAY );
                        String name = assoc.qualifiedName().name();
                        
                        JSONArray array = new JSONArray();
                        int i = 0;
                        for (Iterator it=assoc.iterator(); it.hasNext(); i++) {
                            try {
                                Identity entity = (Identity)it.next();
                                String entityId = entity.identity().get();
                                array.put( i, entityId );
                            }
                            catch (NoSuchEntityException e) {
                                // the entity might no longer exists, this is an error in the model
                                // but it must not cause an exception here
                                array.put( i, NO_SUCH_ENTITY );
                            }
                        }

                        log.debug( "    many-association: name=" + name + ", json=" + array );                        
                        properties.put( name, array );
                    }
                    // Association
                    else if (Association.class.isAssignableFrom( m.getReturnType() )) {
                        Association assoc = (Association)m.invoke( composite, ArrayUtils.EMPTY_OBJECT_ARRAY );
                        String name = assoc.qualifiedName().name();
                        Object associated = assoc.get();
                        
                        Object jsonValue = (associated == null)
                                ? JSONObject.NULL
                                : ((Identity)associated).identity().get();

                        log.debug( "    association: name=" + name + ", json=" + jsonValue );                        
                        properties.put( name, jsonValue );
                    }
                    // Property
                    else if (Property.class.isAssignableFrom( m.getReturnType() )) {
                        Property prop = (Property)m.invoke( composite, ArrayUtils.EMPTY_OBJECT_ARRAY );
                        String name = prop.qualifiedName().name();
                        Object value = prop.get();
        
                        Object jsonValue;
                        if (value == null) {
                            jsonValue = JSONObject.NULL;
                        }
                        else {
                            ValueType valueType = ValueTypeFactory.instance()
                                    .newValueType( prop.type(), null, null );
                            jsonValue = valueType.toJSON( value );
                        }
                        log.debug( "    property: name=" + name + ", value=" + value + ", json=" + jsonValue );                        
                        properties.put( name, jsonValue );
                    }
                }
                catch (Exception e) {
                    log.error( "", e );
                    throw new RuntimeException();
                }
            }
        }
        
        public void rollback( UnitOfWork uow ) {
            Object entity = uow.get( ctype, id );
            
            for (Method m : entity.getClass().getMethods()) {
                if (m.getName().equals( "identity" ) ) {
                    continue;
                }
                try {
                    // ManyAssociation
                    if (ManyAssociation.class.isAssignableFrom( m.getReturnType() )) {
                        ManyAssociation assoc = (ManyAssociation)m.invoke( entity, ArrayUtils.EMPTY_OBJECT_ARRAY );
                        String name = assoc.qualifiedName().name();
                        
                        Set entities = assoc.toSet();
                        for (Object elm : entities) {
                            assoc.remove( elm );
                        }
                        
//                        for (Iterator it=assoc.iterator() ; it.hasNext(); ) {
//                            it.next();
//                            it.remove();
//                        }

                        JSONArray array = (JSONArray)properties.get( name );
                        log.debug( "    many-association: name=" + name + ", json=" + array );
                        for (int i=0; i<array.length(); i++) {
                            String entityId = array.getString( i );
                            if (entityId.equals( NO_SUCH_ENTITY )) {
                                // nothing to add here; the original model was invalid, no way to
                                // restore this, so we cure it and leave to wrong association
                                log.warn( "**** Invalid entity association cannot be restored. ***" );
                            }
                            else {
                                Object associated = uow.get( (Class)assoc.type(), entityId );
                                assoc.add( associated );
                            }
                        }
                    }
                    // Association
                    if (Association.class.isAssignableFrom( m.getReturnType() )) {
                        Association assoc = (Association)m.invoke( entity, ArrayUtils.EMPTY_OBJECT_ARRAY );
                        String name = assoc.qualifiedName().name();
                        
                        Object jsonValue = properties.get( name );
                        log.debug( "    association: name=" + name + ", json=" + jsonValue );
                        if (jsonValue.equals( JSONObject.NULL )) {
                            assoc.set( null );
                        }
                        else {
                            Object associated = uow.get( (Class)assoc.type(), (String)jsonValue );
                            assoc.set( associated );
                        }
                    }
                    // Property
                    if (Property.class.isAssignableFrom( m.getReturnType() )) {
                        Property prop = (Property)m.invoke( entity, ArrayUtils.EMPTY_OBJECT_ARRAY );
                        String name = prop.qualifiedName().name();
                        Type type = prop.type();
        
                        Object jsonValue = properties.get( name );
                        if (jsonValue != null) {
                            Object value;
                            if (jsonValue.equals( JSONObject.NULL )) {
                                value = null;
                            }
                            else {
                                ValueType valueType = ValueTypeFactory.instance().newValueType( type, null, null );
                                value = valueType.fromJSON( jsonValue, null );
                            }
                            log.debug( "    property: name=" + name + ", value=" + value + ", json=" + jsonValue );
                            prop.set( value );
                        }
                    }
                }
                catch (Exception e) {
                    log.error( "", e );
                    throw new RuntimeException( e );
                }
            }
            
        }
    
    }

}
