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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.DefaultValue;

/**
 * Provides runtime support for property default values. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultValues {

    private static Log log = LogFactory.getLog( DefaultValues.class );


    /**
     * Creates a default value for the given field. The default value can be defined
     * via the {@link DefaultValue} annotation. 
     * 
     * @param field
     * @return The default value for the given field, or null if no default value was
     *         defined via {@link DefaultValue} annotation
     */
    public static Object valueOf( Field field ) {
        DefaultValue annotation = field.getAnnotation( DefaultValue.class );
        if (annotation == null) {
            return null;
        }
        
        Class<?> type = (Class)((ParameterizedType)field.getGenericType())
                .getActualTypeArguments()[0];

        if (type.equals( String.class )) {
            return annotation.value();
        }
        else if (type.equals( Integer.class )) {
            return Integer.parseInt( annotation.value() );
        }
        else {
            throw new UnsupportedOperationException( "Default values of this type are not supported yet: " + type );
        }
    }
    
}
