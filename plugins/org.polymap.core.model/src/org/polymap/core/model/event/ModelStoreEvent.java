package org.polymap.core.model.event;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ModelStoreEvent 
        extends EventObject {

    public enum EventType { CHANGE, COMMIT };
    
    private EventType           eventType;
    
    private Set<ModelHandle>    keys = new HashSet();
    
    /** The Tracker that has issed this event. */
    private ModelChangeTracker  tracker;
    
    
    public ModelStoreEvent( ModelStoreEvent other ) {
        super( other.getSource() );
        this.keys = other.keys;
        this.eventType = other.eventType;
        this.tracker = other.tracker;
    }
    
    public ModelStoreEvent( ModelChangeTracker tracker, Object src, Set<ModelHandle> keys, EventType eventType ) {
        super( src );
        this.tracker = tracker;
        this.keys = new HashSet<ModelHandle>( keys );
        this.eventType = eventType;
    }

    /**
     * True if this event was triggered by the session of the caller.
     * Otherwise the event was triggered by a foreign session.
     */
    public boolean isMySession() {
        return ModelChangeTracker.instance() == tracker;
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
        return "ModelStoreEvent [eventType=" + eventType 
                + ", keys=" + keys 
                + ", isMySession=" + isMySession() 
                + "]";
    }
    
}