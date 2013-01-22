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

import java.util.Date;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.DefaultValue;
import org.polymap.core.model2.Defaults;

/**
 * Provides runtime support for property default values. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultValues {

    private static Log log = LogFactory.getLog( DefaultValues.class );

    public static final String      DEFAULT_STRING = "";
    public static final Integer     DEFAULT_INTEGER = new Integer( 0 );
    public static final Date        DEFAULT_DATE = new Date( 0 );

    /**
     * Creates a default value for the given field. The default value can be defined
     * via the {@link DefaultValue} annotation. 
     * 
     * @param field
     * @return The default value for the given field, or null if no default value was
     *         defined via {@link DefaultValue} annotation
     */
    public static Object valueOf( Field field ) {
        Class<?> type = (Class)((ParameterizedType)field.getGenericType())
                .getActualTypeArguments()[0];
        
        // @DefaultValue
        DefaultValue defaultValue = field.getAnnotation( DefaultValue.class );
        if (defaultValue != null) {
            if (type.equals( String.class )) {
                return defaultValue.value();
            }
            else if (type.equals( Integer.class )) {
                return Integer.parseInt( defaultValue.value() );
            }
            // XXX
            else {
                throw new UnsupportedOperationException( "Default values of this type are not supported yet: " + type );
            }
        }
        
        // @Defaults
        Defaults defaults = field.getAnnotation( Defaults.class );
        if (defaults != null) {
            if (type.equals( String.class )) {
                return DEFAULT_STRING;
            }
            else if (type.equals( Integer.class )) {
                return DEFAULT_INTEGER;
            }
            else if (type.equals( Date.class )) {
                return DEFAULT_DATE;
            }
            // XXX
            else {
                throw new UnsupportedOperationException( "Default values of this type are not supported yet: " + type );
            }
        }
        return null;
    }
    
}
