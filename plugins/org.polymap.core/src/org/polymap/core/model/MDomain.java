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

/**
 * A model domain contains model objects and their relationships. The domain
 * handles notification/events and persistence. This interface provides the API
 * for clients and for classes that implement {@link MObject}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>02.11.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class MDomain {

    public abstract void init( MObjectFactory factory );


    /**
     * Starts an update/change operation in the current thread. An operation
     * MUST be finish by calling {@link #endOperation()} or
     * {@link #dropOperation()}. All changes/updates to the domain model has to
     * be performed inside an operation.
     * <p>
     * Example code:
     * <pre>
     * MDomain domain = ; 
     * try {
     *     domain.startOperation();
     *     // create domain object, change attributes...
     *     domain.endOperation();
     * }
     * catch (Throwable e) {
     *     domain.dropOperation();
     * }
     * </pre>
     * 
     * @throws IllegalStateException If the thread already has an operation
     *         registered.
     */
    public abstract void startOperation();
    
    /**
     * End the operation registered for this thread and notify listeners about
     * all changes of this operation.
     * 
     * @throws IllegalStateException If the current thread has no operation
     *         registered.
     */
    public abstract void endOperation();
    
    /**
     * End the operation registered for this thread and notify listeners about
     * all changes of this operation.
     * 
     * @throws IllegalStateException If the current thread has no operation
     *         registered.
     */
    public abstract void dropOperation();
    

    /**
     * Listen to structural changes and attribute changes inside an operation.
     * <p>
     * The listeners are usually called outside the display UI thread. So
     * changes to the UI have to be handled properly.
     * 
     * @see #startOperation()
     * @param The listener to add. The implementation does not check the
     *        listener. If a listener is added twice, then it is called twice.
     */
    public abstract void addDomainChangeListener( MDomainChangeListener l );
    
    public abstract void removeDomainChangeListener( MDomainChangeListener l );
    
    
    /**
     * Listen to structural changes and attribute changes.
     * <p>
     * The listeners are usually called outside the display UI thread. So
     * changes to the UI have to be handled properly.
     * 
     * @param The listener to add. The implementation does not check the
     *        listener. If a listener is added twice, then it is called twice.
     */
    public abstract void addPropertyChangeListener( PropertyChangeListener l );
    
    public abstract void removePropertyChangeListener( PropertyChangeListener l );
    
    
    /**
     * API for {@link MObject} objects. SIgnal changes on transient fields if nessecary.
     * 
     * @param obj
     * @param featureName
     * @param oldValue
     * @param newValue
     */
    protected abstract void fireChangeEvent( MObject obj, String featureName, Object oldValue, Object newValue );


    public abstract MObjectClass getObjectClass( Class cl);
    
    public abstract Iterable<MObject> objects();


    public abstract MObject getObject( MId id );


    /**
     * Objects may have a name. Named objects are the entry points to the object
     * graph of the domain.
     *
     * @see #createObject(Class, String)
     * @param name The name previously set via
     *        {@link #createObject(Class, String)}.
     * @return Null if no such named object exists.
     */
    public abstract MObject getObjectByName( String name );

    
    public abstract MSerializerContext createSerializerContext();


    /**
     * 
     * @param mObjectInterface The class of the {@link MObject} to be created.
     * @param name Objects may have a name. Named objects are the entry points
     *        to the object graph of the domain. Null if object should not have
     *        a name.
     * @return The newly created object.
     * @throws ModelRuntimeException
     */
    public abstract MObject createObject( Class mObjectInterface, String name )
            throws ModelRuntimeException;


    public abstract void removeObject( MObject obj );


    public abstract Object getFeatureValue( MObject obj, String featureName )
            throws ModelRuntimeException;

    
    public abstract void setFeatureValue( MObject obj, String featureName, Object value )
            throws ModelRuntimeException;


    /**
     * Returns the 'many' side of the given object relationship.
     * 
     * @param obj
     * @param relName
     * @return
     * @throws ModelRuntimeException
     */
    public abstract MList getRelatedObjects( MObject obj, String featureName, boolean isLeft )
            throws ModelRuntimeException;


    /**
     * Returns the 'one' side of the given object relationship.
     *
     * @param obj
     * @param relName
     * @return
     * @throws ModelRuntimeException
     */
    public abstract MObject getRelatedObject( MObject obj, String featureName, boolean isLeft )
            throws ModelRuntimeException;


    /**
     * 
     * @param obj1
     * @param obj2
     * @param isLeft 
     * @throws ModelRuntimeException
     */
    public abstract void createRelation( MObject obj1, MObject obj2, String featureName, boolean isLeft )
            throws ModelRuntimeException;


    /**
     * 
     * @param obj1
     * @param obj2
     * @throws ModelRuntimeException
     */
    public abstract void removeRelation( MObject obj1, MObject obj2, String featureName, boolean isLeft )
            throws ModelRuntimeException;


}
