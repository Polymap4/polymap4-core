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
package org.polymap.core.mapeditor.tooling;

import java.util.EventListener;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ToolingListener
        extends EventListener {

    public void toolingChanged( ToolingEvent ev );
    
    
    // filters ********************************************

    /**
     * {@link ToolingListener} filter that checks the event type for a given value.
     */
    public class TypeFilter
            implements ToolingListener {

        private ToolingListener     delegate;
        
        private String              eventType;
        
        
        public TypeFilter( String eventType, ToolingListener delegate ) {
            assert eventType != null;
            assert delegate != null;
            this.eventType = eventType;
            this.delegate = delegate;
        }


        @Override
        public void toolingChanged( ToolingEvent ev ) {
            if (ev.getType().equals( eventType )) {
                delegate.toolingChanged( ev );
            }
        }
        
    }
    
}
