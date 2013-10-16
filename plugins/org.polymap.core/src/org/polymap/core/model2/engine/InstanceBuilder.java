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
import java.util.Arrays;
import java.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Concerns;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyConcern;
import org.polymap.core.model2.engine.EntityRepositoryImpl.EntityRuntimeContextImpl;
import org.polymap.core.model2.runtime.CompositeInfo;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreProperty;
import org.polymap.core.model2.store.StoreSPI;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheLoader;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class InstanceBuilder {

    private static Log log = LogFactory.getLog( InstanceBuilder.class );

    protected static Field                      contextField;
    
    protected static Cache<Field,List<Class>>   concerns = CacheConfig.DEFAULT.create();
    
    static {
        try {
            contextField = Composite.class.getDeclaredField( "context" );
            contextField.setAccessible( true );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    // instance *******************************************
    
    private EntityRuntimeContext    context;
    
    
    public InstanceBuilder( EntityRuntimeContext context ) {
        this.context = context;
    }
    
    
    public <T extends Composite> T newComposite( CompositeState state, Class<T> entityClass ) { 
        try {
            // new instance
            Constructor<?> ctor = entityClass.getConstructor( new Class[] {} );
            T instance = (T)ctor.newInstance( new Object[] {} );
            
            // set context
            contextField.set( instance, context );
            
//            // init concerns
//            List<PropertyConcern> concerns = new ArrayList();
//            Concerns concernsAnnotation = entityClass.getAnnotation( Concerns.class );
//            if (concernsAnnotation != null) {
//                for (Class<? extends PropertyConcern> concernClass : concernsAnnotation.value()) {
//                    concerns.add( concernClass.newInstance() );
//                }
//            }

            // init properties
            initProperties( instance, state );
            
            return instance;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }
    
    
    /**
     * Initializes all properties of the given Composite, including all super classes.
     * Composite properties are init with {@link CompositePropertyImpl} which comes back to 
     * {@link InstanceBuilder} when the value is accessed.
     */
    protected void initProperties( Composite instance, CompositeState state ) throws Exception {
        StoreSPI store = context.getRepository().getStore();
        CompositeInfo compositeInfo = context.getRepository().infoOf( instance.getClass() );
        
        Class superClass = instance.getClass();
        while (superClass != null) {
            // XXX cache fields
            for (Field field : superClass.getDeclaredFields()) {
                if (Property.class.isAssignableFrom( field.getType() )) {
                    field.setAccessible( true );

                    PropertyInfo info = compositeInfo.getProperty( field.getName() );
                    StoreProperty storeProp = state.loadProperty( info );
                    
                    Class propType = info.getType();
                    Property prop = null;
                    
                    // Collection
                    if (info.getMaxOccurs() > 1) {
                        prop = new CollectionPropertyImpl( storeProp );

                        PropertyInfo propInfo = storeProp.getInfo();
                        if (propInfo.isImmutable()) {
                            throw new RuntimeException( "Concerns and constraints are not yet supported for collection properties." );
                        }
                    }
                    else {
                        // Composite
                        if (Composite.class.isAssignableFrom( propType )) {
                            prop = new CompositePropertyImpl( context, storeProp );
                        }
                        // primitive type
                        else {
                            prop = new PropertyImpl( storeProp );
                        }

                        // always check modifications;
                        // default value, immutable, nullable
                        prop = new ConstraintsPropertyInterceptor( prop, (EntityRuntimeContextImpl)context );

                        // concerns
                        for (PropertyConcern concern : fieldConcerns( field )) {
                            prop = new ConcernPropertyInterceptor( prop, concern );
                        }
                    }
                    
                    // init field
                    field.set( instance, prop );                    
                }
            }
            superClass = superClass.getSuperclass();
        }
    }


    protected Iterable<PropertyConcern> fieldConcerns( final Field field ) throws Exception {
        List<Class> types = concerns.get( field, new CacheLoader<Field,List<Class>,Exception>() {
            @Override
            public List<Class> load( Field key ) throws Exception {
                List<Class> result = new ArrayList();
                // Field concerns
                Concerns fa = field.getDeclaringClass().getAnnotation( Concerns.class );
                if (fa != null) {
                    result.addAll( Arrays.asList( fa.value() ) );
                }
                // Class concerns
                Concerns ca = field.getAnnotation( Concerns.class );
                if (ca != null) {
                    result.addAll( Arrays.asList( ca.value() ) );
                }
                return result;
            }
            @Override
            public int size() throws Exception {
                return 1024;
            }
        } );
        
        return Iterables.transform( types, new Function<Class,PropertyConcern>() {
            public PropertyConcern apply( Class type ) {
                try {
                    return (PropertyConcern)type.newInstance();
                }
                catch (Exception e) {
                    throw new ModelRuntimeException( "Error while initializing concern: " + type, e );
                }
            }
        });
    }
    
}
