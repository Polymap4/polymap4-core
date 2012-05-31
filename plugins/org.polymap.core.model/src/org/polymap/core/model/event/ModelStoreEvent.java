package org.polymap.core.model.event;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.polymap.core.runtime.SessionContext;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ModelStoreEvent 
        extends EventObject {

    public enum EventType { CHANGE, COMMIT };
    
    private EventType           eventType;
    
    private Set<ModelHandle>    keys = new HashSet();
    
    /** The Tracker that has issued this event. */
    private SessionContext      srcContext;
    
    
    public ModelStoreEvent( ModelStoreEvent other ) {
        super( other.getSource() );
        this.keys = other.keys;
        this.eventType = other.eventType;
        this.srcContext = other.srcContext;
    }
    
    public ModelStoreEvent( SessionContext srcContext, Object src, Set<ModelHandle> keys, EventType eventType ) {
        super( src );
        assert srcContext != null : "No SessionContext for this thread.";
        this.srcContext = srcContext;
        this.keys = new HashSet<ModelHandle>( keys );
        this.eventType = eventType;
    }

    /**
     * True if this event was triggered by the session of the caller.
     * Otherwise the event was triggered by a foreign session.
     */
    public boolean isMySession() {
        return srcContext == SessionContext.current();
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