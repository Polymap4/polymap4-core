package org.polymap.core.model.event;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.polymap.core.model.Entity;

/**
 * 
 */
public class GlobalModelChangeEvent 
        extends EventObject {

    public enum EventType { CHANGE, COMMIT };
    
    private EventType       eventType;
    
    private Set<String>     ids = new HashSet();
    
    
    public GlobalModelChangeEvent( Object source, Set<String> ids, EventType eventType ) {
        super( source );
        this.ids = new HashSet<String>( ids );
        this.eventType = eventType;
    }

    public EventType getEventType() {
       return eventType;    
    }
    
    public boolean hasChanged( Entity entity ) {
        return ids.contains( entity.id() );
    }
    
    public void add( String id ) {
        ids.add( id );
    }
    
}