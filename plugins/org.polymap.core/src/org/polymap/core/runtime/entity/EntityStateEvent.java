/* 
 * polymap.org
 * Copyright 2011-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.entity;

import java.util.HashSet;
import java.util.Set;

import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.event.Event;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityStateEvent<S>
        extends Event<S> {

    public enum EventType { CHANGE, COMMIT };
    
    private EventType           eventType;
    
    private Set<EntityHandle>   keys = new HashSet();
    
    /** The Tracker that has issued this event. */
    private SessionContext      srcContext;
    
    
    public EntityStateEvent( EntityStateEvent other ) {
        super( (S)other.getSource() );
        this.keys = other.keys;
        this.eventType = other.eventType;
        this.srcContext = other.srcContext;
    }
    
    public EntityStateEvent( SessionContext srcContext, S src, Set<EntityHandle> keys, EventType eventType ) {
        super( src );
        assert srcContext != null : "No SessionContext for this thread.";
        this.srcContext = srcContext;
        this.keys = new HashSet<EntityHandle>( keys );
        this.eventType = eventType;
    }

    /**
     * True if this event was triggered by the session of the caller.
     * Otherwise the event was triggered by a foreign session.
     */
    public boolean isMySession() {
        return srcContext.equals( SessionContext.current() );
    }
    
    public EventType getEventType() {
       return eventType;    
    }
    
    public boolean hasChanged( IEntityHandleable handleable ) {
        return keys.contains( handleable.handle() );
    }
    
    public boolean hasChanged( EntityHandle key ) {
        return keys.contains( key );
    }
    
    public boolean hasChanged( String id, String type ) {
        return keys.contains( EntityHandle.instance( id, type ) );
    }

    public String toString() {
        return "EntityStateEvent [eventType=" + eventType 
                + ", keys=" + keys 
                + ", isMySession=" + isMySession() 
                + "]";
    }
    
}