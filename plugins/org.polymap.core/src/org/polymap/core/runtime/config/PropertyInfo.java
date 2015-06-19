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

import java.lang.annotation.Annotation;

/**
 * 
 * @param <H> - The type of the host class.
 * @param <V> - The type of the value of this property. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface PropertyInfo<H,V> {

    public String getName();
    
    public Class<V> getType();
    
    public <T extends Annotation> T getAnnotation( Class<T> type );

    public H getHostObject();
    
}
