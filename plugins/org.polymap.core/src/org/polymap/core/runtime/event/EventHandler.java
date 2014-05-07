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
 * 
 * @param scope ({@link Event.Scope#Session}) One of the {@link Event.Scope}
 *        constants. Defaults to {@link Event.Scope#Session}.
 * @param display (false) True specifies that the handler is to be executed inside
 *        the {@link Display} thread. Defaults to false.
 * @param delay (0) Specifies that the execution of this handler is to be delayed by
 *        the given amount of milliseconds. Delayed handler have to have a List of
 *        events as parameter. This list contains all the events that have been
 *        catched in the delay time. Delayed handlers are always executed inside a
 *        {@link Job}. Defaults to 0 (no delay).
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("javadoc")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    
    Event.Scope scope() default Event.Scope.Session;
    
    boolean display() default false;

    int delay() default 0;
}
