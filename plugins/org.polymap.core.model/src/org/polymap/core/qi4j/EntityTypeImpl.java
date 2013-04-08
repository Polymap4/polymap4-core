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
import java.lang.reflect.ParameterizedType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.value.ValueComposite;

import org.polymap.core.model.Composite;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;

/**
 * Default implementation of {@link EntityType} for {@link QiModule} based
 * modules.
 * <p/>
 * Type param T: {@link Entity} or {@link ValueComposite}
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
class EntityTypeImpl<T extends Composite>
        implements EntityType<T> {

    private static final Log log = LogFactory.getLog( EntityTypeImpl.class );

    private static Map<Class,EntityTypeImpl>        types = new HashMap();


    public static <T extends Composite> EntityType<T> forClass( Class<T> type ) {
        EntityTypeImpl<T> result = types.get( type );
        if (result == null) {
            synchronized (types) {
                result = new EntityTypeImpl( type );
                types.put( type, result );
                if (types.size() > 100) {
                    throw new RuntimeException( "The global map of entity types has grown over 100 entries!" );
                }
            }
        }
        return result;
    }


    // instance *******************************************

    private Class<T>                    type;

    private Property                    id;

    private Map<String,Property>        props;


    private EntityTypeImpl( Class<T> type ) {
        this.type = type;
    }

    public String getName() {
        return type.getName();
    }

    public Class<T> getType() {
        return type;
    }

    public Property getId() {
        return id;
    }

    public Collection<Property> getProperties() {
        return checkInitProps().values();
    }

    public Property getProperty( String name ) {
        assert name != null;
        return checkInitProps().get( name );
    }

    private Map<String,Property> checkInitProps() {
        if (props == null) {
            props = new HashMap();
            for (Method m : type.getMethods()) {
                // property
                if (org.qi4j.api.property.Property.class.isAssignableFrom(
                        m.getReturnType() ) ) {
                    PropertyImpl prop = new PropertyImpl( m );
                    // id
                    if (prop.getName().equals( "id" )) {
                        id = prop;
                    }
                    // skip identity and internal properties
                    else if (prop.getName().equals( "identity" )
                            || prop.getName().startsWith( "_" )) {
                    }
                    // skip internal and computed properties
                    else if (m.getDeclaringClass().equals( Entity.class )
                            || !prop.isValidType()) {
                    }
                    // complex
                    else if (ValueComposite.class.isAssignableFrom( prop.getType() )) {
                        CompositeProperty complexProperty = new CompositePropertyImpl( m );
                        props.put( complexProperty.getName(), complexProperty );
                    }
                    // collection
                    else if (Collection.class.isAssignableFrom( prop.getType() )) {
                        CollectionProperty collProperty = new CollectionPropertyImpl( m );
                        props.put( collProperty.getName(), collProperty );
                    }
                    // simple property
                    else {
                        props.put( prop.getName(), prop );
                    }
                }
                // association
                else if (org.qi4j.api.entity.association.Association.class.isAssignableFrom(
                        m.getReturnType() ) ) {
                    AssociationImpl assoc = new AssociationImpl( m );
                    props.put( assoc.getName(), assoc );
                }
                // many-association
                else if (org.qi4j.api.entity.association.ManyAssociation.class.isAssignableFrom(
                        m.getReturnType() ) ) {
                    ManyAssociationImpl assoc = new ManyAssociationImpl( m );
                    props.put( assoc.getName(), assoc );
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

        protected Method          m;


        PropertyImpl( Method m ) {
            this.m = m;
        }

        public String getName() {
            return m.getName();

        }

        public boolean isValidType() {
            try {
                getType();
                return true;
            }
            catch (Exception e) {
                log.info( "no valid type: " + e );
                return false;
            }
        }

        public Class getType() {
            ParameterizedType propType = (ParameterizedType)m.getGenericReturnType();
            return (Class)propType.getActualTypeArguments()[0];
        }

        public Object getValue( Composite entity )
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

        public void setValue( Composite entity, Object value )
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
    class CompositePropertyImpl
            extends PropertyImpl
            implements CompositeProperty {

        private EntityType       compositeType;


        CompositePropertyImpl( Method m ) {
            super( m );
        }

        public EntityType getCompositeType() {
            if (compositeType == null) {
                compositeType = new EntityTypeImpl( getType() );
            }
            return compositeType;
        }

    }


   /**
   *
   */
  class CollectionPropertyImpl
          extends PropertyImpl
          implements CollectionProperty {

      private EntityType complexType;


      CollectionPropertyImpl( Method m ) {
          super( m );
      }

      public EntityType getComplexType() {
          ParameterizedType collType = (ParameterizedType)m.getGenericReturnType();
          Class elmType = (Class)collType.getActualTypeArguments()[0];

          if (ValueComposite.class.isAssignableFrom( elmType )) {
              if (complexType == null) {
                  complexType = new EntityTypeImpl( elmType );
              }
              return complexType;
          }
          else {
              throw new IllegalStateException( "Collection property '" + getName() + "' has non-complex type: " + elmType );
          }
      }

  }


    /**
     *
     */
    class AssociationImpl
            extends PropertyImpl
            implements Association {

        AssociationImpl( Method m ) {
            super( m );
        }

        public Object getValue( Composite entity )
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

        public void setValue( Composite entity, Object value )
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


    /**
     *
     */
    class ManyAssociationImpl
            extends PropertyImpl
            implements ManyAssociation {

        ManyAssociationImpl( Method m ) {
            super( m );
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
