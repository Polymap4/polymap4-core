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

import java.util.List;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.runtime.UIJob;
import org.polymap.core.ui.UIUtils;

/**
 * Annotates methods that handle events published via {@link EventManager}.
 * <p/>
 * The method is executed inside the dispatcher thread of the {@link EventManager}.
 * It should return quickly. For long running task a {@link UIJob} should be created.
 * By specifying a {@link #delay()} the method is executed inside a new Job
 * automatically.
 * <p/>
 * For <b>delayed and display</b> handlers {@link UIUtils#activateCallback(String)}
 * is used to update the UI after events have been handled. It is important to specify
 * a good filter for such handlers in order to activate UI callback only when there
 * are actually events to be handled.
 * 
 * @param scope ({@link Event.Scope#Session}) One of the {@link Event.Scope}
 *        constants. Defaults to {@link Event.Scope#Session}.
 * @param display (false) True specifies that the handler is to be executed inside
 *        the {@link Display} thread. Defaults to false.
 * @param delay (0) Specifies that the execution of this handler is to be delayed by
 *        the given amount of milliseconds. Delayed handler have to have a {@link List} of
 *        events as parameter. This list contains all the events that have been
 *        catched in the delay time. Delayed handlers are always executed inside a
 *        {@link Job} (except if marked as {@link #display()}). Defaults to 0 (no delay).
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("javadoc")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    
    /**
     * One of the {@link Event.Scope} constants. Defaults to
     * {@link Event.Scope#Session}.
     */
    Event.Scope scope() default Event.Scope.Session;
    
    
    /**
     * True specifies that the handler is to be executed asynchronously inside the
     * {@link Display} thread. Defaults to false.
     */
    boolean display() default false;

    /**
     * Specifies that the execution of this handler is to be delayed by the given
     * amount of milliseconds. Delayed handler have to have a {@link List} of events
     * as parameter. This list contains all the events that have been catched in the
     * delay time. Delayed handlers are always executed inside a {@link Job} (if not
     * marked as {@link #display()}). Defaults to 0 (no delay).
     */
    int delay() default 0;
}
