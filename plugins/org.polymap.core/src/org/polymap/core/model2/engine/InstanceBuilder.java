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
import java.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class InstanceBuilder {

    private static Log log = LogFactory.getLog( InstanceBuilder.class );
    
    private EntityRuntimeContext    context;
    
    
    public InstanceBuilder( EntityRuntimeContext context ) {
        this.context = context;
    }
    
    
    public <T> T newMixin( Class<T> mixinClass ) 
    throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
    
    public <T extends Composite> T newComposite( CompositeState state, Class<T> entityClass ) { 
        try {
            // new instance
            Constructor<?> ctor = entityClass.getConstructor( new Class[] {} );
            T instance = (T)ctor.newInstance( new Object[] {} );
            
            // set context
            Field contextField = Composite.class.getDeclaredField( "context" );
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
            initProperties( instance, concerns, state );
            
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
    protected void initProperties( Composite instance, 
            List<PropertyConcern> concerns,
            CompositeState state ) 
            throws Exception {
        
        StoreSPI store = context.getRepository().getStore();
        CompositeInfo compositeInfo = context.getRepository().infoOf( instance.getClass() );
        
        Class superClass = instance.getClass();
        while (superClass != null) {
            for (Field field : superClass.getDeclaredFields()) {
                if (field.getType().isAssignableFrom( Property.class )) {
                    field.setAccessible( true );

                    PropertyInfo info = compositeInfo.getProperty( field.getName() );
                    StoreProperty storeProp = state.loadProperty( info );
                    
                    Class propType = info.getType();
                    Property prop = null;
                    // Collection
                    if (info.getMaxOccurs() > 1) {
                        throw new RuntimeException( "No Collection properties yet: " + propType );                        
                    }
                    // Composite
                    else if (Composite.class.isAssignableFrom( propType )) {
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
                    for (PropertyConcern concern : concerns) {
                        prop = new ConcernPropertyInterceptor( prop, concern );
                    }
                    
                    // init field
                    field.set( instance, prop );                    
                }
            }
            superClass = superClass.getSuperclass();
        }
    }
    
}
