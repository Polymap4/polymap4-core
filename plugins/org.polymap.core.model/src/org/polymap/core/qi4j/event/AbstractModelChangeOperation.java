/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.qi4j.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.beans.PropertyChangeEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

//import org.polymap.core.model.EntityType.Association;
//import org.polymap.core.model.EntityType.ManyAssociation;
//import org.polymap.core.model.EntityType.Property;
import org.polymap.core.model.Entity;
import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * Operations that change the domain model should extend this class. It provides:
 * <ul>
 * <li>simplified API</li>
 * <li>automatic operation bound demarcation and {@link ModelChangeEvent} handling</li>
 * <li>automatic revert of operation result</li>
 * </ul>
 * Reverting operation results is based on the {@link PropertyChangeEvent}s fired
 * during operation. It reverts changes from all entities from all modules of this
 * session.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public abstract class AbstractModelChangeOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( AbstractModelChangeOperation.class );

    private List<StoredPropertyChangeEvent>     events = new ArrayList( 32 );
    
    
    public AbstractModelChangeOperation( String label ) {
        super( label );
    }


    /**
     * Execute this operation.
     * 
     * @see #execute(IProgressMonitor, IAdaptable)
     */
    protected abstract IStatus doExecute(  IProgressMonitor monitor, IAdaptable info )
    throws Exception;
    
    
    /**
     * The default implementation of this method handles operation events and
     * calls {@link #doExecute(IProgressMonitor, IAdaptable)} for the business
     * logic.
     * <p/>
     * Clients should not overwrite.
     */
    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        try {
            log.debug( "Starting operation ... (" + hashCode() + ")" + " [" + Thread.currentThread().getId() + "]" );
            start();
            doExecute( monitor, info );
            end( true );
            log.debug( "    operation completed." );
            return Status.OK_STATUS;
        }
        catch (ExecutionException e) {
            end( false );
            log.debug( "    operation canceled." );
            throw e;
        }
        catch (Exception e) {
            end( false );
            log.debug( "    operation canceled." );
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
    }


    public boolean canUndo() {
        // the current implementation is way to weak to enable by default
        return false;
    }


    /**
     * The default implementation of this method reverts the changes based on
     * the {@link PropertyChangeEvent}s during the operation.
     * <p/>
     * Clients should not overwrite.
     */
    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        try {
            // revert
            List<StoredPropertyChangeEvent> revertEvents = revert();

            // fire ModelChangeEvent
            EventManager manager = EventManager.instance();
            manager.publish( new ModelChangeEvent( this, revertEvents ) );

            return Status.OK_STATUS;
        }
        catch (ExecutionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
    }

    
    public boolean canRedo() {
        return true;
    }


    /**
     * The default implementation of this method starts operation and event
     * collection again.
     * <p/>
     * Clients should not overwrite.
     */
    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        return execute( monitor, info );
    }

    
    protected void start() {
        EventManager.instance().subscribe( this );
    }

    
    protected void end( boolean fireEvent ) {
        EventManager manager = EventManager.instance();
        if (!manager.unsubscribe( this )) {
            throw new IllegalStateException( "Unable to remove property change listener" );
        }
        manager.publish( new ModelChangeEvent( this, events ) );
    }
    
    
    protected List<StoredPropertyChangeEvent> revert()
    throws Exception {
        List<StoredPropertyChangeEvent> revertEvents = new ArrayList( events.size() );
        
        for (int i = events.size()-1; i>=0; i--) {
            StoredPropertyChangeEvent ev = events.get( i );
            log.info( "revert(): " + ev );
            
            String propName = ev.getPropertyName();
            // created
            if (propName.equals( PropertyChangeSupport.PROP_ENTITY_CREATED )) {
                QiModule repo = (QiModule)ev.getOldValue();
                repo.removeEntity( (Entity)ev.getSource() );
            }
            // removed
            else if (propName.equals( PropertyChangeSupport.PROP_ENTITY_REMOVED )) {
                log.warn( "Not yet implemented: reverting entity deletion!" );
            }
            // changed
            else {
                // ManyAssociation
                if (ev.getManyAssociation() != null) {
                    ManyAssociation assoc = ev.getManyAssociation();
                    for (Object associated : assoc.toList()) {
                        assoc.remove( associated );
                    }
                    assert assoc.count() == 0;
                    Collection old = (Collection)ev.getOldValue();
                    for (Object associated : old) {
                        assoc.add( associated );
                    }
                    revertEvents.add( ev ); 
                }
                // Association
                else if (ev.getAssociation() != null) {
                    Association assoc = ev.getAssociation(); 
                    assoc.set( ev.getOldValue() );
                    revertEvents.add( ev ); 
                }
                // Property
                else if (ev.getProperty() != null) {
                    Property prop = ev.getProperty();
                    prop.set( ev.getOldValue() );
                    revertEvents.add( ev ); 
                }
                else {
                    throw new IllegalStateException( "Unknown property or association in event: " + ev );
                }
            }
        }
        return revertEvents;
    }
    
    
    /**
     * PropertyChangeListener.
     */
    @EventHandler
    public void propertyChange( PropertyChangeEvent ev ) {
        log.debug( "Property: " + ev );
        if (ev instanceof StoredPropertyChangeEvent) {
            events.add( (StoredPropertyChangeEvent)ev );
        }
    }
    
}
