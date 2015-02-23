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

import java.util.EventObject;

import org.polymap.core.runtime.session.SessionContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Event<S>
        extends EventObject {

    /**
     * Event scope is specified via {@link EventHandler} annotation.
     */
    public enum Scope {
        /**
         * Event scope: specifies that a handler receives events published in the
         * {@link SessionContext#current() current session}.
         */
        Session,
        /**
         * Event scope: specifies that the handler receives all events published in
         * the this JVM.
         */
        JVM
    }
    
    public Event( S source ) {
        super( source );
    }

    @Override
    public S getSource() {
        return (S)super.getSource();
    }

}
