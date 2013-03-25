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

import java.util.EventObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ToolingEvent
        extends EventObject {

    public enum EventType {
        TOOL_ACTIVATED,
        TOOL_DEACTIVATED, 
        TOOL_ACTIVATING, 
        TOOL_DEACTIVATING
    }
    
    private EventType           type;
    
    private Object              value;
    

    public ToolingEvent( IEditorTool source, EventType type, Object value ) {
        super( source );
        this.type = type;
        this.value = value;
    }

    @Override
    public IEditorTool getSource() {
        return (IEditorTool)super.getSource();
    }

    public EventType getType() {
        return type;
    }
    
    public Object getValue() {
        return value;
    }

}
