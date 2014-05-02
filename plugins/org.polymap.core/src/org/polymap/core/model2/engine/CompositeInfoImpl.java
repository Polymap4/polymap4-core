/* 
 * polymap.org
 * Copyright (C) 2012-2013, Falko Bräutigam. All rights reserved.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Field;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Immutable;
import org.polymap.core.model2.Mixins;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.CompositeInfo;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class CompositeInfoImpl
        implements CompositeInfo {

    private Class<? extends Composite>      compositeClass;
    
    /** Maps property name into PropertyInfo. */
    private Map<String,PropertyInfo>        propertyInfos = new HashMap();
    
    private Lazy<Composite>                 template = new LockedLazyInit();
    
    
    public CompositeInfoImpl( Class<? extends Composite> compositeClass ) {
        this.compositeClass = compositeClass;
        try {
            initPropertyInfos();
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }

    @Override
    public String getName() {
        return compositeClass.getSimpleName();
    }

    @Override
    public String getNameInStore() {
        return compositeClass.getAnnotation( NameInStore.class ) != null
                ? compositeClass.getAnnotation( NameInStore.class ).value()
                : getName();
    }

    @Override
    public Class<? extends Composite> getType() {
        return compositeClass;
    }

//    @Override
//    public Composite getTemplate() {
//        return template.get( new Supplier<Composite>() {
//            public Composite get() {
//                CompositeState templateState = new CompositeState() {
//                    @Override
//                    public Object id() {
//                        throw new UnsupportedOperationException( "Method id() is nout allowed for Composite templates." );
//                    }
//                    @Override
//                    public StoreProperty loadProperty( PropertyInfo info ) {
//                        throw new RuntimeException( "Method loadProperty() is nout allowed for Composite templates." );
//                    }
//                    @Override
//                    public Object getUnderlying() {
//                        throw new RuntimeException( "Method getUnderlying() is nout allowed for Composite templates." );
//                    }
//                };
//                EntityRuntimeContextImpl entityContext = new EntityRuntimeContextImpl( 
//                        templateState, EntityStatus.CREATED );
//                InstanceBuilder builder = new InstanceBuilder( entityContext );
//                return builder.newComposite( templateState, compositeClass );
//
//            }
//        });
//    }

    @Override
    public Collection<Class<? extends Composite>> getMixins() {
        Mixins mixins = compositeClass.getAnnotation( Mixins.class );
        return mixins != null 
                ? Arrays.asList( mixins.value() )
                : Collections.EMPTY_LIST;
    }

    @Override
    public Collection<PropertyInfo> getProperties() {
        return Collections.unmodifiableCollection( propertyInfos.values() );
    }

    @Override
    public PropertyInfo getProperty( String name ) {
        return propertyInfos.get( name );
    }

    @Override
    public boolean isImmutable() {
        return compositeClass.getAnnotation( Immutable.class ) != null;
    }

    /**
     * Recursivly init {@link #propertyInfos} of the given instance and all complex
     * propertyInfos.
     */
    protected void initPropertyInfos() throws Exception {
        Class superClass = compositeClass;
        while (superClass != null) {
            for (Field field : superClass.getDeclaredFields()) {
                if (Property.class.isAssignableFrom( field.getType() )) {
                    
                    PropertyInfoImpl info = new PropertyInfoImpl( field );
                    propertyInfos.put( info.getName(), info );
                }
            }
            superClass = superClass.getSuperclass();
        }
    }

}
