/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.model;

import java.beans.PropertyChangeListener;

import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

/**
 * Provides the API of one module of the domain model. A {@link Module} instance
 * is bound to one user session. A {@link Module} provides:
 * <ul>
 * <li>methods to register change listeners</li>
 * <li>methods to commit/rollbackregister change listeners</li>
 * <li>domain specific factory methods for entities
 * </ul>
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface Module {

//    public abstract long lastModified( Entity entity );
    
    /**
     * Creates a new change set for this module. All changes to the domain model
     * from all threads are recorded by this new changes set.
     */
    public abstract ModelChangeSet newChangeSet();
    
    /**
     * 
     * @throws AssertionError If the stack of change sets is empty.
     */
    public abstract ModelChangeSet currentChangeSet();
    
    public abstract Iterable<ModelChangeSet> changeSets();
    
    
    /**
     * Commit all changes of this module since last commit or rollback. This
     * will send all the changes down to the underlying entity stores. This
     * clears the list of nested change sets and start with a new change set.
     * 
     * @throws ConcurrentModificationException
     * @throws UnitOfWorkCompletionException
     */
    public abstract void commitChanges() 
            throws ConcurrentModificationException, CompletionException;
    
    /**
     * XXX What is the specification of this method?
     */
    public abstract void discardChanges();

    
    /**
     * {@link ModelProperty}s are fire right after a property of an entity
     * of the model has changed. Regardless if the operation is later completed
     * or just droped.
     */
    public abstract void addPropertyChangeListener( PropertyChangeListener l );

    public abstract void removePropertyChangeListener( PropertyChangeListener l );


    /**
     * {@link ModelChangeEvent}s are fired when the operation completes via
     * {@link #completeOperation()}. It delivers all the {@link ModelProperty}s
     * that has been fired during operation.
     */
    public void addModelChangeListener( ModelChangeListener l );
    
    public void removeModelChangeListener( ModelChangeListener l );


    /**
     * ...
     * <p>
     * The reference to th elistener is stored in a weak reference, so the caller
     * is responsible of keeping a reference to the listener as long as it should
     * receive events.
     */
    public void addGlobalModelChangeListener( GlobalModelChangeListener l );

    public void removeGlobalModelChangeListener( GlobalModelChangeListener l );

}
