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
package org.polymap.core.runtime.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates methods that handle events published via {@link EventManager}.
 * <p>
 * <b>Parameters:</b>
 * <ul>
 * <li><b>scope</b> : (defaults to {@link Event.Scope#Session})</li>
 * <li><b>display</b> : (default to false)</li>
 * <li><b>delay</b> : (default to 0)</li>
 * </ul>
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    
    Event.Scope scope() default Event.Scope.Session;
    
    boolean display() default false;

    int delay() default 0;
}
