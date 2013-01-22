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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FilteringListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( FilteringListener.class );

    private EventFilter[]           filters;
    
    
    public FilteringListener( EventListener delegate, EventFilter... filters ) {
        super( delegate );
        assert filters != null;
        this.filters = filters;
    }


    @Override
    public void handleEvent( EventObject ev ) throws Exception {
        for (EventFilter filter : filters) {
            if (!filter.apply( ev )) {
                //log.debug( "Offending filter: " + filter + "\n           event: " + ev + ")" );
                return;
            }
        }
        delegate.handleEvent( ev );
    }
    
}
