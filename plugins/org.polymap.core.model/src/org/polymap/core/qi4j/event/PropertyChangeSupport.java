/* 
 * polymap.org
 * Copyright 2009-2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.qi4j.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.entity.EntityInstance;

import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.TransientProperty;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

/**
 * Adds support for {@link PropertyChangeEvent}s to an entity. The {@link Mixin}
 * and the {@link Concern} must be applied to the entity composite.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public interface PropertyChangeSupport
        extends QiEntity {
    
    public static final String              PROP_ENTITY_CREATED = "_entity_created_";
    public static final String              PROP_ENTITY_REMOVED = "_entity_removed_";


    /**
     * Registeres the given {@link EventHandler annotated} handler as event listener.
     * <p/>
     * Listeners are weakly referenced by the EventManager. A listener is reclaimed
     * by the GC and removed from the EventManager as soon as there is no strong
     * reference to it. An anonymous inner class can not be used as event listener.
     * 
     * @see EventManager#subscribe(Object, EventFilter...)
     * @param handler
     * @param filters
     * @throws IllegalStateException If the handler is subscribed already.
     */
    public void addPropertyChangeListener( Object handler, EventFilter... filters );

    public boolean removePropertyChangeListener( Object handler );

    public void fireEvent( QualifiedName name, @Optional Object newValue, @Optional Object oldValue, @Optional Object propOrAssoc );
    

    /**
     * 
     */
    abstract static class Mixin
            implements PropertyChangeSupport, Lifecycle {

        private static final Log log = LogFactory.getLog( PropertyChangeSupport.class );

        @This
        private PropertyChangeSupport                   composite;
        
//        @State
//        private EntityStateHolder                       entityState;

        
        public void create()
        throws LifecycleException {
            log.debug( "Entity created: " + composite.toString() );
            if (Polymap.getSessionDisplay() != null) {
                QiModule repo = Qi4jPlugin.Session.instance().resolveModule( composite );
                QualifiedName qname = QualifiedName.fromClass( composite.getCompositeType(), PROP_ENTITY_CREATED );
                fireEvent( qname, composite, repo, composite );
            }
        }


        public void remove()
        throws LifecycleException {
            log.debug( "Entity removed: " + composite.toString() );
            // FIXME save entity state
            QiModule repo = Qi4jPlugin.Session.instance().resolveModule( composite );
            QualifiedName qname = QualifiedName.fromClass( composite.getCompositeType(), PROP_ENTITY_REMOVED );
            fireEvent( qname, composite, repo, composite );
        }

        
        public void addPropertyChangeListener( Object handler, EventFilter... filters ) {
            EventManager.instance().subscribe( handler, new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent ev ) {
                    return ev.getSource() == composite;
                }
                public String toString() {
                    return "PropertyChangeSupport.Filter [composite=" + composite + "]";
                }
            });
        }

        public boolean removePropertyChangeListener( Object handler ) {
            return EventManager.instance().unsubscribe( handler );
        }


        public void fireEvent( QualifiedName name, Object newValue, Object oldValue, Object propOrAssoc ) {
            PropertyChangeEvent ev = propOrAssoc != null
                    ? new StoredPropertyChangeEvent( composite, name.name(), oldValue, newValue, propOrAssoc )
                    : new PropertyChangeEvent( composite, name.name(), oldValue, newValue );

            EventManager.instance().publish( ev );
        }
    }

    
    /**
     * 
     */
    public static class Concern
            extends GenericConcern {

        private static final Log log = LogFactory.getLog( PropertyChangeSupport.class );

        @This
        protected PropertyChangeSupport     composite;
            

        public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable {
            
//            // skip my own methods
//            if (method.getName().equals( "fireEvent" )
//                    || method.getName().equals( "addPropertyChangeListener" )
//                    || method.getName().equals( "removePropertyChangeListener" )) {
//                return null;
//            }
            
            // call underlying
            Object result = next.invoke( proxy, method, args );
            
            // check method annotations
            TransientProperty a2 = method.getAnnotation( TransientProperty.class );
            ModelProperty a = method.getAnnotation( ModelProperty.class );
            if (a2 != null || a != null) {
                if (!Qi4jPlugin.isInitialized()) {
                    log.debug( "Qi4JPlugin still about to initialize. Skipping this modification." );
                }
                else if (Polymap.getSessionDisplay() == null) {
                    log.debug( "!!! No session when modifying entity !!!" );
                }
                else {
                    String propName = a2 != null ? a2.value() : a.value();
                    QualifiedName qname = QualifiedName.fromClass( method.getDeclaringClass(), propName );
                    composite.fireEvent( qname, args[0], null, null );
                }
            }
            
            // using a reference to composite in wrapper does not work;
            // the proxy lost its InvocationHandler when set() is called
            EntityInstance entityInstance = (EntityInstance)Proxy.getInvocationHandler( proxy );

            // XXX creating a wrapper for every property method invocation may
            // create a lot of objects, should we cache them?

            if (result instanceof Property) {
                return new PropertyWrapper( (Property)result, entityInstance );
            }
            else if (result instanceof Association) {
                return new AssociationWrapper( (Association)result, entityInstance );
            }
            else if (result instanceof ManyAssociation) {
                return new ManyAssociationWrapper( (ManyAssociation)result, entityInstance );
            }
            else {
                return result;
            }
        }
    }

    
    /**
     * 
     */
    static final class PropertyWrapper
            implements Property {
        
        private static final Log log = LogFactory.getLog( PropertyWrapper.class );

        private Property                delegate;
        
        private EntityInstance          entityInstance;


        /**
         * Create a new wrapper.
         * <p/>
         * Using a reference to composite in wrapper does not work; the proxy
         * lost its InvocationHandler when set() is called
         */
        protected PropertyWrapper( Property delegate, EntityInstance entityInstance ) {
            assert delegate != null;
            assert entityInstance != null;
            this.delegate = delegate;
            this.entityInstance = entityInstance;
        }

        public Object get() {
            Object value = delegate.get();
            if (value instanceof Collection) {
                log.debug( "Collection values are not tracked for property changes!" );
            }
            return value;
        }

        public void set( Object newValue )
                throws IllegalArgumentException, IllegalStateException {
            Object oldValue = delegate.get();
            
            // make a copy so that the collection can be changed afterwards
            if (oldValue instanceof Collection) {
                oldValue = new ArrayList( (Collection)oldValue );
            }
            delegate.set( newValue );
            entityInstance.<PropertyChangeSupport>proxy().fireEvent( qualifiedName(), newValue, oldValue, this );
        }

        public boolean isComputed() {
            return delegate.isComputed();
        }

        public boolean isImmutable() {
            return delegate.isImmutable();
        }

        public <T> T metaInfo( Class<T> infoType ) {
            return delegate.metaInfo( infoType );
        }

        public QualifiedName qualifiedName() {
            return delegate.qualifiedName();
        }

        public Type type() {
            return delegate.type();
        }
    }
    
    
    /**
     * 
     */
    static final class AssociationWrapper
            implements Association {
        
        private Association             delegate;
        
        private EntityInstance          entityInstance;

        
        protected AssociationWrapper( Association delegate, EntityInstance entityInstance ) {
            this.delegate = delegate;
            this.entityInstance = entityInstance;
        }

        public Object get() {
            return delegate.get();
        }

        public void set( Object associated )
                throws IllegalArgumentException {
            Object oldValue = delegate.get();
            delegate.set( associated );
            entityInstance.<PropertyChangeSupport>proxy().fireEvent( qualifiedName(), associated, oldValue, this );
        }

        public boolean isAggregated() {
            return delegate.isAggregated();
        }

        public boolean isImmutable() {
            return delegate.isImmutable();
        }

        public <T> T metaInfo( Class<T> infoType ) {
            return delegate.metaInfo( infoType );
        }

        public QualifiedName qualifiedName() {
            return delegate.qualifiedName();
        }

        public Type type() {
            return delegate.type();
        }

    }

    
    /**
     * 
     */
    static final class ManyAssociationWrapper
            implements ManyAssociation {
        
        private ManyAssociation         delegate;
        
        private EntityInstance          entityInstance;

        
        protected ManyAssociationWrapper( ManyAssociation delegate, EntityInstance entityInstance ) {
            this.delegate = delegate;
            this.entityInstance = entityInstance;
        }

        public boolean add( int i, Object entity ) {
            List oldValue = toList();
            boolean result = delegate.add( i, entity );
            if (result) {
                entityInstance.<PropertyChangeSupport>proxy().fireEvent( qualifiedName(), oldValue, toList(), this );
            }
            return result;
        }

        public boolean add( Object entity ) {
            List oldValue = toList();
            boolean result = delegate.add( entity );
            if (result) {
                entityInstance.<PropertyChangeSupport>proxy().fireEvent( qualifiedName(), oldValue, toList(), this );
            }
            return result;
        }

        public boolean remove( Object entity ) {
            List oldValue = toList();
            boolean result = delegate.remove( entity );
            if (result) {
                entityInstance.<PropertyChangeSupport>proxy().fireEvent( qualifiedName(), oldValue, toList(), this );
            }
            return result;
        }

        public boolean contains( Object entity ) {
            return delegate.contains( entity );
        }

        public int count() {
            return delegate.count();
        }

        public Object get( int i ) {
            return delegate.get( i );
        }

        public boolean isAggregated() {
            return delegate.isAggregated();
        }

        public boolean isImmutable() {
            return delegate.isImmutable();
        }

        public Iterator iterator() {
            return delegate.iterator();
        }

        public <T> T metaInfo( Class<T> infoType ) {
            return delegate.metaInfo( infoType );
        }

        public QualifiedName qualifiedName() {
            return delegate.qualifiedName();
        }

        public List toList() {
            return delegate.toList();
        }

        public Set toSet() {
            return delegate.toSet();
        }

        public Type type() {
            return delegate.type();
        }


    }

}
