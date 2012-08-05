/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Concerns;
import org.polymap.core.model2.DefaultValue;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyConcern;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.store.PropertyDescriptor;
import org.polymap.core.model2.store.StoreSPI;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class InstanceBuilder {

    private static Log log = LogFactory.getLog( InstanceBuilder.class );
    
    private EntityRuntimeContext    context;

    private StoreSPI                store;

    
    public InstanceBuilder( EntityRuntimeContext context, StoreSPI store ) {
        this.context = context;
        this.store = store;
    }
    
    
    public <T> T newMixin( Class<T> mixinClass ) 
    throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
    
    public <T extends Entity> T newEntity( Class<T> entityClass ) 
    throws Exception {
        // new instance
        Entity instance = null;
        if (Entity.class.isAssignableFrom( entityClass )) {
            Constructor<?> ctor = entityClass.getConstructor( new Class[] {} );
            instance = (Entity)ctor.newInstance( new Object[] {} );
        }
        else {
            throw new RuntimeException();
        }
        
        // set context
        Field contextField = Entity.class.getDeclaredField( "context" );
        contextField.setAccessible( true );
        contextField.set( instance, context );
        
        // init concerns
        List<PropertyConcern> concerns = new ArrayList();
        Concerns concernsAnnotation = entityClass.getAnnotation( Concerns.class );
        if (concernsAnnotation != null) {
            for (Class<? extends PropertyConcern> concernClass : concernsAnnotation.value()) {
                PropertyConcern concern = concernClass.newInstance();
                concerns.add( concern );
            }
        }

        // init properties
        initProperties( instance, concerns, null );
        
        return (T)instance;
    }
    
    /**
     * Recursivly init properties of the given instance and all complex
     * properties.
     */
    protected void initProperties( Composite instance, 
            List<PropertyConcern> concerns,
            PropertyDescriptor parent ) 
            throws Exception {
        
        Class superClass = instance.getClass();
        while (superClass != null) {
            for (Field field : superClass.getDeclaredFields()) {
                if (field.getType().isAssignableFrom( Property.class )) {
                    field.setAccessible( true );

                    PropertyDescriptorImpl descriptor = new PropertyDescriptorImpl( context, field, parent );
                    Property prop = store.createProperty( descriptor );
                    
                    // always check modifications
                    prop = new ModificationPropertyInterceptor( prop, context );
                    
                    // default value
                    if (field.getAnnotation( DefaultValue.class ) != null) {
                        prop = new DefaultValuePropertyInterceptor( prop, field );
                    }
                    // concerns
                    for (PropertyConcern concern : concerns) {
                        prop = new ConcernPropertyInterceptor( prop, concern );
                    }
                    // init field
                    field.set( instance, prop );
                    
                    // complex property?
                    Class propType = descriptor.getPropertyType();
                    if (Composite.class.isAssignableFrom( propType )) {
                        initProperties( instance, concerns, descriptor );
                    }
                    else if (Collection.class.isAssignableFrom( propType )) {
                        // XXX no collections yet
                        throw new RuntimeException( "Type of property is not supported yet: " + propType );
                    }
                }
            }
            superClass = superClass.getSuperclass();
        }
    }
    
}
