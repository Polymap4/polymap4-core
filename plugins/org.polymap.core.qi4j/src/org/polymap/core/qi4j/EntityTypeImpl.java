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
package org.polymap.core.qi4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;

/**
 * Default implementation of {@link EntityType}. This class can be
 * instantiated directly or subclassed. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
class EntityTypeImpl
        implements EntityType {

    private Class<? extends Entity>     type;
    
    private Property                    id;
    
    private Map<String,Property>        props;
    
    private Map<String,Association>     assocs;
    

    EntityTypeImpl( Class<? extends Entity> type ) {
        this.type = type;
    }

    public String getName() {
        return type.getName();
    }

    public Class getType() {
        return type;
    }

    public Property getId() {
        return id;
    }

    public Collection<Property> getProperties() {
        return checkInitProps().values();
    }

    public Property getProperty( String name ) {
        return checkInitProps().get( name );
    }

    protected Map<String,Property> checkInitProps() {
        if (props == null) {
            props = new HashMap();
            assocs = new HashMap();
            for (Method m : type.getDeclaredMethods()) {
                // property
                if (org.qi4j.api.property.Property.class.isAssignableFrom(
                        m.getReturnType() ) ) {
                    PropertyImpl prop = new PropertyImpl( m );
                    // id
                    if (prop.getName().equals( "id" )) {
                        id = prop;
                    }
                    // ommit internal computed properties
                    else if (!m.getDeclaringClass().equals( Entity.class )) {
                        props.put( prop.getName(), prop );
                    }
                }
                // association
                else if (org.qi4j.api.entity.association.Association.class.isAssignableFrom(
                        m.getReturnType() ) ) {
                    AssociationImpl assoc = new AssociationImpl( m );
                    assocs.put( assoc.getName(), assoc );
                }
            }
        }
        return props;
    }

    
    /**
     * 
     */
    class PropertyImpl
            implements Property {

        private Method          m;
        
        
        PropertyImpl( Method m ) {
            this.m = m;
        }

        public String getName() {
            return m.getName();
            
        }

        public Type getType() {
            return m.getGenericReturnType();
        }
        
        public Object getValue( Entity entity ) 
        throws Exception {
            try {
                org.qi4j.api.property.Property prop = 
                        (org.qi4j.api.property.Property)m.invoke( entity );
                return prop.get();
            }
            catch (InvocationTargetException e) {
                Throwable ee = e.getTargetException();
                if (ee instanceof Exception) {
                    throw (Exception)ee;
                }
                else {
                    throw (Error)ee;
                }
            }
            catch (Exception e) {
                throw e;
            }
        }
        
        public void setValue( Entity entity, Object value )
        throws Exception {
            try {
                org.qi4j.api.property.Property prop = 
                        (org.qi4j.api.property.Property)m.invoke( entity );
                prop.set( value );
            }
            catch (InvocationTargetException e) {
                Throwable ee = e.getTargetException();
                if (ee instanceof Exception) {
                    throw (Exception)ee;
                }
                else {
                    throw (Error)ee;
                }
            }
            catch (Exception e) {
                throw e;
            }
        }
        
    }

    
    /**
     * 
     */
    class AssociationImpl
            implements Association {

        private Method          m;
        
        
        AssociationImpl( Method m ) {
            this.m = m;
        }

        public String getName() {
            return m.getName();
            
        }

        public Type getType() {
            return m.getGenericReturnType();
        }
        
        public Object getValue( Entity entity ) 
        throws Exception {
            try {
                org.qi4j.api.entity.association.Association assoc = 
                        (org.qi4j.api.entity.association.Association)m.invoke( entity );
                return assoc.get();
            }
            catch (InvocationTargetException e) {
                Throwable ee = e.getTargetException();
                if (ee instanceof Exception) {
                    throw (Exception)ee;
                }
                else {
                    throw (Error)ee;
                }
            }
            catch (Exception e) {
                throw e;
            }
        }
        
        public void setValue( Entity entity, Object value )
        throws Exception {
            try {
                org.qi4j.api.entity.association.Association assoc = 
                        (org.qi4j.api.entity.association.Association)m.invoke( entity );
                assoc.set( value );
            }
            catch (InvocationTargetException e) {
                Throwable ee = e.getTargetException();
                if (ee instanceof Exception) {
                    throw (Exception)ee;
                }
                else {
                    throw (Error)ee;
                }
            }
            catch (Exception e) {
                throw e;
            }
        }
        
    }
    
}
