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
import java.util.List;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Association;
import org.polymap.core.model.EntityType.ManyAssociation;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.PropertyEventFilter;
import org.polymap.core.qi4j.QiEntity;

/**
 * Operations that change the domain model should extend this class. It
 * provides:
 * <ul>
 * <li>simplified API</li>
 * <li>automatic operation bound demarcation and event handling</li>
 * <li>automatic revert of operation result</li>
 * </ul>
 * Reverting operation results is based on the {@link PropertyChangeEvent}s
 * fired during operation. It reverts changes from all entities from all modules
 * of this session.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AbstractModelChangeOperation
        extends AbstractOperation
        implements IUndoableOperation, PropertyChangeListener {

    private static Log log = LogFactory.getLog( AbstractModelChangeOperation.class );

    private List<PropertyChangeEvent>       events = new ArrayList( 32 );
    
    
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
            log.info( "Starting operation ... (" + hashCode() + ")" + " [" + Thread.currentThread().getId() + "]" );
            start();
            doExecute( monitor, info );
            end( true );
            log.info( "    operation completed." );
            return Status.OK_STATUS;
        }
        catch (ExecutionException e) {
            end( false );
            log.info( "    operation canceled." );
            throw e;
        }
        catch (Exception e) {
            end( false );
            log.info( "    operation canceled." );
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
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
            List<PropertyChangeEvent> revertEvents = revert();

            ModelChangeTracker tracker = ModelChangeTracker.instance();
            tracker.fireModelChangeEvent( new ModelChangeEvent( this, revertEvents ) );

            return Status.OK_STATUS;
        }
        catch (ExecutionException e) {
            end( false );
            throw e;
        }
        catch (Exception e) {
            end( false );
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
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
        ModelChangeTracker.instance().addPropertyChangeListener( this, PropertyEventFilter.ALL );
    }

    
    protected void end( boolean fireEvent ) {
        ModelChangeTracker tracker = ModelChangeTracker.instance();
        tracker.removePropertyChangeListener( this );
        tracker.fireModelChangeEvent( new ModelChangeEvent( this, events ) );
    }
    
    protected List<PropertyChangeEvent> revert()
    throws Exception {
        List<PropertyChangeEvent> revertEvents = new ArrayList( events.size() );
        
        for (int i = events.size()-1; i>=0; i--) {
            PropertyChangeEvent ev = events.get( i );
            log.info( "Undoing: prop=" + ev.getPropertyName() + ", old=" + ev.getOldValue() + ", new=" + ev.getNewValue() );
            
            String propName = ev.getPropertyName();
            // created
            if (propName.equals( PropertyChangeSupport.PROP_ENTITY_CREATED )) {
                log.warn( "Not yet implemented: reverting entity creation!" );
            }
            // removed
            else if (propName.equals( PropertyChangeSupport.PROP_ENTITY_REMOVED )) {
                log.warn( "Not yet implemented: reverting entity deletion!" );
            }
            // changed
            else {
                EntityType entityType = ((QiEntity)ev.getSource()).getEntityType();
                Property prop = entityType.getProperty( propName );

                if (prop instanceof ManyAssociation) {
                    throw new RuntimeException( "not yet implemented: undo of ManyAssocation" );
                }
                else if (prop instanceof Association) {
                    prop.setValue( (Entity)ev.getSource(), ev.getOldValue() );
                }
                else if (prop instanceof Property) {
                    log.info( "    prop: name=" + propName + ", value=" + ev.getOldValue() );
                    prop.setValue( (Entity)ev.getSource(), ev.getOldValue() );
                    revertEvents.add( new PropertyChangeEvent( ev.getSource(), propName, ev.getNewValue(), ev.getOldValue() ) ); 
                }
                else {
                    // no property: annotated method
                    log.info( "    No such property: " + propName + ", prop=" + prop );
                }
            }
        }
        return revertEvents;
    }
    
    
    /**
     * PropertyChangeListener.
     */
    public void propertyChange( PropertyChangeEvent ev ) {
        events.add( ev );
    }
    
}
