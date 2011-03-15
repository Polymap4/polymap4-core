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

package org.polymap.core.model.plain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import org.polymap.core.model.MDomain;
import org.polymap.core.model.MDomainChangeEvent;
import org.polymap.core.model.MDomainChangeListener;
import org.polymap.core.model.MId;
import org.polymap.core.model.MList;
import org.polymap.core.model.MObject;
import org.polymap.core.model.MObjectClass;
import org.polymap.core.model.MObjectFactory;
import org.polymap.core.model.MSerializerContext;
import org.polymap.core.model.ModelRuntimeException;

/**
 * A simple and straight forward domain implementation based on plain
 * java objects.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class PlainMDomain
        extends MDomain {
 
    private static long                 idCount = System.currentTimeMillis();
    
    private MObjectFactory              factory;
    
    private Map<MId,MObject>            objects = new HashMap();
    
    private BidiMap                     namedObjects = new DualHashBidiMap();
    
    private Map<MId,Features>           features = new HashMap();
    
    private Set<Relation>               relations = new HashSet();
    
    private List<PropertyChangeListener> listeners = new ArrayList();

    private List<MDomainChangeListener> listeners2 = new ArrayList();
    
    private Map<Thread,MDomainChangeEvent> operationEvents = new HashMap();
    
    
    public PlainMDomain( MObjectFactory factory ) {
        super();
        this.factory = factory;
    }


    public void init( MObjectFactory _factory ) {
        this.factory = _factory;
    }
    
    
    public void addDomainChangeListener( MDomainChangeListener l ) {
        listeners2.add( l );
    }


    public void removeDomainChangeListener( MDomainChangeListener l ) {
        listeners2.remove( l );
    }


    public synchronized void startOperation() {
        if (operationEvents.containsKey( Thread.currentThread() )) {
            throw new IllegalStateException( "Thread has started an operation already." );
        }
        MDomainChangeEvent event = new MDomainChangeEvent( this );
        operationEvents.put( Thread.currentThread(), event );
    }


    public synchronized void dropOperation() {
        MDomainChangeEvent event = operationEvents.remove( Thread.currentThread() );
//        if (event == null) {
//            throw new IllegalStateException( "Thread has no operation associated." );
//        }
    }


    public synchronized void endOperation() {
        MDomainChangeEvent event = operationEvents.remove( Thread.currentThread() );
        if (event == null) {
            throw new IllegalStateException( "Thread has no operation associated." );
        }
        for (MDomainChangeListener l : listeners2) {
            l.domainChanged( event );
        }
    }


    public void addPropertyChangeListener( PropertyChangeListener l ) {
        listeners.add( l );
    }


    public void removePropertyChangeListener( PropertyChangeListener l ) {
        listeners.remove( l );
    }

    
    protected void fireChangeEvent( MObject obj, String featureName, Object oldValue, Object newValue ) {
        // property change
        PropertyChangeEvent event = new PropertyChangeEvent( obj, featureName, oldValue, newValue ); 
        for (PropertyChangeListener l : listeners) {
            l.propertyChange( event );
        }
        // model change
        MDomainChangeEvent domainEvent = operationEvents.get( Thread.currentThread() );
        if (domainEvent == null) {
            // XXX
            //throw new IllegalStateException( "" );
        }
        else {
            domainEvent.addEvent( event );
        }
    }

    
    public MObjectClass getObjectClass( Class cl) {
        // XXX cache result
        return new PlainMObjectClass( cl );
    }

    
    public Iterable<MObject> objects() {
        return objects.values();
    }

    
    public MObject getObject( MId id ) {
        return objects.get( id );
    }

    
    public MSerializerContext createSerializerContext() {
        return new PlainSerializerContext();
    }
    
    
    public MObject getObjectByName( String name ) {
        return (MObject)namedObjects.get( name );
    }


    public MObject createObject( Class mObjectInterface, String name )
            throws ModelRuntimeException {
        MObject obj = (MObject)factory.newObject( mObjectInterface );
        MId id = new PlainMId( idCount++ );
        MObject old = objects.put( id, obj );
        if (old != null) {
            throw new IllegalStateException( "Newly created MId ist key already!" );
        }
        obj.attach( this, id );
        
        // name
        if (name != null) {
            old = (MObject)namedObjects.put( name, obj );
            if (old != null) {
                throw new IllegalStateException( "Name of object already exists: " + name );
            }
        }

        fireChangeEvent( obj, null, null, null );
        
        return obj;

    }


    public void removeObject( MObject obj ) {
        checkObj( obj );
        // remove all relations
        MId objId = obj.getId();
        for (Iterator<Relation> it=relations.iterator(); it.hasNext(); ) {
            Relation rel = it.next();
            if (rel.fromId.equals( objId ) ||
                    rel.toId.equals( objId )) {
                it.remove();
            }
        }
        // remove object
        objects.remove( obj.getId() );
        obj.detach();
        
        // fire events
        fireChangeEvent( obj, null, null, null );
    }

    
//    protected void registerObject( MObject obj )
//            throws ModelRuntimeException {
//
//    }


    public Object getFeatureValue( MObject obj, String featureName )
            throws ModelRuntimeException {
        checkObj( obj );
        Features objFeatures = features.get( obj.getId() );
        if (objFeatures == null) {
            return null;
        }
        else {
            return objFeatures.featureValues.get( featureName );
        }
    }


    public void setFeatureValue( MObject obj, String featureName, Object value )
            throws ModelRuntimeException {
        checkObj( obj );
        Features objFeatures = features.get( obj.getId() );
        if (objFeatures == null) {
            objFeatures = new Features();
            features.put( obj.getId(), objFeatures );
        }
        Object oldValue = objFeatures.featureValues.put( featureName, value );
        fireChangeEvent( obj, featureName, oldValue, value );
    }


    public MList<MObject> getRelatedObjects( MObject obj, String featureName, boolean isLeft )
            throws ModelRuntimeException {
        checkObj( obj );
        MId objId = obj.getId();
        ArrayList<MObject> result = new ArrayList();
        for (Relation rel : relations) {
            MId refId = rel.getId( objId, featureName, isLeft );
            if (refId != null) {
                result.add( objects.get( refId ) );
            }
        }
        return new PlainMList( result );
    }


    public MObject getRelatedObject( MObject obj, String featureName, boolean isLeft )
            throws ModelRuntimeException {
        checkObj( obj );
        MList<MObject> result = getRelatedObjects( obj, featureName, isLeft );
        if (result.isEmpty()) {
            return null;
        }
        else if (result.size() == 1) {
            return result.iterator().next();
        }
        else {
            throw new ModelRuntimeException( "There are more than one related objects for: " + obj );
        }
    }


    public void createRelation( MObject obj1, MObject obj2, String featureName, boolean isLeft )
            throws ModelRuntimeException {
        checkObj( obj1 );
        checkObj( obj2 );
        relations.add( new Relation( obj1.getId(), obj2.getId(), featureName, isLeft ) );
    }


    public void removeRelation( MObject obj1, MObject obj2, String featureName, boolean isLeft )
            throws ModelRuntimeException {
        checkObj( obj1 );
        checkObj( obj2 );
        relations.remove( new Relation( obj1.getId(), obj2.getId(), featureName, isLeft ) );
    }


    private void checkObj( MObject obj ) {
        if (obj == null) {
            throw new ModelRuntimeException( "obj == null" );
        }
        if (!objects.containsKey( obj.getId() )) {
            throw new ModelRuntimeException( "The model does not contain this object: " + obj.getId() );
        }
    }


    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     */
    class Features {
    
        Map<String,Object>  featureValues = new HashMap();

        Features() {
            super();
        }
        
    }


    /**
     * Relation between the given two ids.
     * 
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     */
    class Relation {

        MId         fromId, toId;
        
        String      featureName;
        
        int         hashCode;

        Relation( MId fromId, MId toId, String featureName, boolean isLeft ) {
            super();
            if (fromId == null || toId == null || featureName == null) {
                throw new IllegalArgumentException( "Ids must not be null." );
            }
            if (fromId.equals( toId )) {
                throw new IllegalArgumentException( "Ids must not be equal." );
            }
            this.fromId = isLeft ? fromId : toId;
            this.toId = isLeft ? toId : fromId;
            this.featureName = featureName;
        }
        
        
        MId getId( MId _id, String _featureName, boolean isLeft ) {
            if (featureName.equals( _featureName )) {
                if (isLeft && fromId.equals( _id )) {
                    return toId;
                }
                if (!isLeft && toId.equals( _id )) {
                    return fromId;
                }
            }
            return null;
        }

        
        public int hashCode() {
            if (hashCode == -1) {
                hashCode = fromId.hashCode() + toId.hashCode();
//                final int prime = 31;
//                int result = 1;
//                result = prime * result + fromId.hashCode();
//                result = prime * result + toId.hashCode();
//                result = prime * result + featureName.hashCode();
//                hashCode = result;
            }
            return hashCode;
        }

        
        public boolean equals( Object obj ) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Relation)) {
                return false;
            }
            Relation rhs = (Relation)obj;
            return toId.equals( rhs.toId ) && 
                    fromId.equals( rhs.fromId ) && 
                    featureName.equals( rhs.featureName );
        }

    }

    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     */
    class RelationKey {
        
        private Class       class1, class2;

        public RelationKey( Class class1, Class class2 ) {
            super();
            this.class1 = class1;
            this.class2 = class2;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            //result = prime * result + getOuterType().hashCode();
            result = prime * result + ((class1 == null) ? 0 : class1.hashCode());
            result = prime * result + ((class2 == null) ? 0 : class2.hashCode());
            return result;
        }

        @Override
        public boolean equals( Object obj ) {
            if (this == obj) {
                return true;
            }
            else if (obj == null) {
                return false;
            }
            else if (!(obj instanceof RelationKey)) {
                return false;
            }
            RelationKey rhs = (RelationKey)obj;
            
            throw new RuntimeException( "..." );
        }
    }

    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    class PlainSerializerContext
            implements MSerializerContext {

        public MDomain getDomain() {
            return PlainMDomain.this;
        }

        public MObject getObject( String serialized_id ) {
            return objects.get( new PlainMId( serialized_id ) );
        }

        public MObject createObject( String classname, String serialized_id )
                throws ModelRuntimeException {
            MObject obj = (MObject)factory.newObject( classname );
            MId id = new PlainMId( serialized_id );
            MObject old = objects.put( id, obj );
            if (old != null) {
                throw new IllegalStateException( "Newly created MId ist key already!" );
            }
            obj.attach( PlainMDomain.this, id );

            fireChangeEvent( obj, null, null, null );

            return obj;
        }

        public String getObjectName( MObject obj) {
            return (String)namedObjects.getKey( obj );
        }

        public void setObjectName( MObject obj, String objName ) {
            Object old = namedObjects.put( objName, obj );
            if (old != null) {
                throw new IllegalStateException( "Object name already exists: " + objName );
            }
        }
        
    }
    
}
