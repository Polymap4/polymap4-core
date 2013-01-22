/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class Event<S>
        extends EventObject {

    public enum Scope {
        Session,
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
