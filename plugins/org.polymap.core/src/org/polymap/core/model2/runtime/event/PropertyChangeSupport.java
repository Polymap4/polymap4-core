/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.runtime.event;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Composite;
import org.polymap.core.runtime.ListenerList;

/**
 * Provides a mixin that adds support for {@link PropertyChangeEvent} to entities.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyChangeSupport
        extends Composite {

    private static final Log log = LogFactory.getLog( PropertyChangeSupport.class );

//    @This
    private PropertyChangeSupport                   composite;

    //        @State
    //        private EntityStateHolder                       entityState;

    private ListenerList<PropertyChangeListener>    listeners;
    
    
//    public void create()
//    throws LifecycleException {
//        log.debug( "Entity created: " + composite.toString() );
//        if (Polymap.getSessionDisplay() != null) {
//            QiModule repo = Qi4jPlugin.Session.instance().resolveModule( composite );
//            QualifiedName qname = QualifiedName.fromClass( composite.getCompositeType(), PROP_ENTITY_CREATED );
//            fireEvent( qname, composite, repo, composite );
//        }
//    }
//
//
//    public void remove()
//    throws LifecycleException {
//        log.debug( "Entity removed: " + composite.toString() );
//        // FIXME save entity state
//        QiModule repo = Qi4jPlugin.Session.instance().resolveModule( composite );
//        QualifiedName qname = QualifiedName.fromClass( composite.getCompositeType(), PROP_ENTITY_REMOVED );
//        fireEvent( qname, composite, repo, composite );
//    }
//
//
//    public void addPropertyChangeListener( PropertyChangeListener l ) {
//        if (listeners == null) {
//            synchronized (this) {
//                if (listeners == null) {
//                    listeners = new ListenerList();
//                }
//            }
//        }
//        listeners.add( l );    
//    }
//
//    
//    public void removePropertyChangeListener( PropertyChangeListener l ) {
//        if (listeners != null) {
//            listeners.remove( l );
//        }
//    }
//
//
//    public void fireEvent( QualifiedName name, Object newValue, Object oldValue, Object propOrAssoc ) {
//        PropertyChangeEvent ev = propOrAssoc != null
//        ? new StoredPropertyChangeEvent( composite, name.name(), oldValue, newValue, propOrAssoc )
//        : new PropertyChangeEvent( composite, name.name(), oldValue, newValue );
//
//        ModelEventManager instance = ModelEventManager.instance();
//        if (instance != null) {
//            instance.firePropertyChangeEvent( ev );
//        }
//
//        if (listeners != null) {
//            for (PropertyChangeListener listener : listeners) {
//                try {
//                    listener.propertyChange( ev );
//                }
//                catch (Throwable e) {
//                    PolymapWorkbench.handleError( Qi4jPlugin.PLUGIN_ID, listener, "Error while changing object: " + composite, e );
//                }
//            }
//        }
//    }

}
