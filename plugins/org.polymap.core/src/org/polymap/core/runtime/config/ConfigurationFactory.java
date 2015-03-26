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
package org.polymap.core.runtime.config;

import java.lang.reflect.Field;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConfigurationFactory {

    /**
     * Creates a new configuration of the given type.
     */
    public static <T> T create( Class<T> cl ) throws ConfigurationException {
        try {
            T instance = cl.newInstance();
            return inject( instance );
        }
        catch (ReflectiveOperationException e) {
            throw new ConfigurationException( e );
        }
    }
    
    
    /**
     * Injects {@link Property} instances into the given object.
     */
    public static <T> T inject( T instance ) throws ConfigurationException {
        try {
            // init properties
            for (Field f : instance.getClass().getDeclaredFields()) {
                if (f.getType().isAssignableFrom( Property.class )) {
                    f.setAccessible( true );
                    Property prop = new Property2() {

                        private Object      value;

                        @Override
                        public Object fset( Object newValue ) {
                            this.value = newValue;
                            return instance;
                        }

                        @Override
                        public Object set( Object newValue ) {
                            Object previous = value;
                            value = newValue;
                            return previous;
                        }

                        @Override
                        public Object get() {
                            if (value == null && f.getAnnotation( Mandatory.class ) != null) {
                                throw new ConfigurationException( "Configuration property is @Mandatory: " + f.getName() );
                            }
                            DefaultValue defaultValue = f.getAnnotation( DefaultValue.class );
                            if (value == null && defaultValue != null) {
                                return defaultValue.value();
                            }
                            DefaultDouble defaultDouble = f.getAnnotation( DefaultDouble.class );
                            if (value == null && defaultDouble != null) {
                                return defaultDouble.value();
                            }
                            DefaultBoolean defaultBoolean = f.getAnnotation( DefaultBoolean.class );
                            if (value == null && defaultBoolean != null) {
                                return defaultBoolean.value();
                            }
                            return value;
                        }
                    };
                    f.set( instance, prop );
                }
            }
            return instance;
        }
        catch (Exception e) {
            throw new ConfigurationException( e );
        }
    }
    
}
