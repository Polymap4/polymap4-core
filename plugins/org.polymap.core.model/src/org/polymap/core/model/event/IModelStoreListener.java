package org.polymap.core.model.event;

import java.util.EventListener;

import org.polymap.core.model.Module;

/**
 * 
 */
public interface IModelStoreListener
        extends EventListener {

    /**
     * Checks if the session of this listener is still valid. If false then
     * this listener is removed. As the listeners are stored globally, this
     * is a potential memory leak.
     */
    public boolean isValid();
    
    /**
     * Called when:
     * <ul>
     * <li>any global operation has been completed (pending changes in a {@link ModelChangeSet}</li>
     * <li>any global operation has been undone (discard of a {@link ModelChangeSet}</li>
     * <li>any global {@link Module} has been commited</li>
     * </ul>
     * This method must never block and return quickly. Implementations have to be thread save
     * and aware of reentrance of the method. 
     */
    public void modelChanged( ModelStoreEvent ev );
}