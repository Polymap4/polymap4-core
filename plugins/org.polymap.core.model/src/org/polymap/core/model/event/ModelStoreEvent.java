package org.polymap.core.model.event;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 */
public class ModelStoreEvent 
        extends EventObject {

    public enum EventType { CHANGE, COMMIT };
    
    private EventType           eventType;
    
    private Set<ModelHandle>    keys = new HashSet();
    
    
    public ModelStoreEvent( ModelStoreEvent other ) {
        super( other.getSource() );
        this.keys = other.keys;
        this.eventType = other.eventType;
    }
    
    public ModelStoreEvent( Object source, Set<ModelHandle> keys, EventType eventType ) {
        super( source );
        this.keys = new HashSet<ModelHandle>( keys );
        this.eventType = eventType;
    }

    public EventType getEventType() {
       return eventType;    
    }
    
    public boolean hasChanged( IModelHandleable handleable ) {
        return keys.contains( handleable.handle() );
    }
    
    public boolean hasChanged( ModelHandle key ) {
        return keys.contains( key );
    }
    
    public boolean hasChanged( String id, String type ) {
        return keys.contains( ModelHandle.instance( id, type ) );
    }

    public String toString() {
        return "ModelStoreEvent [eventType=" + eventType + ", keys=" + keys + "]";
    }
    
}