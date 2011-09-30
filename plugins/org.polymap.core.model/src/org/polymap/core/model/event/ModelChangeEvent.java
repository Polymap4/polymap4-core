/*
 * polymap.org 
 * Copyright 2011, Falko Bräutigam, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.core.model.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collection of {@link PropertyChangeEvent}s collected while execution of an
 * operation.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class ModelChangeEvent
        extends EventObject
        implements PropertyChangeListener {

    private static Log log = LogFactory.getLog( ModelChangeEvent.class );

    private List<PropertyChangeEvent>       events = new LinkedList();
    
//    private List<String>                    created = new LinkedList();
//    
//    private List<String>                    removed = new LinkedList();
    
    
    public ModelChangeEvent( Object source ) {
        super( source );
    }

    public ModelChangeEvent( Object source, List<? extends PropertyChangeEvent> events ) {
        super( source );
        this.events = new ArrayList( events );
    }

    public void propertyChange( PropertyChangeEvent ev ) {
        events.add( ev );
    }
    
    public Iterable<PropertyChangeEvent> events() {
        return events;
    }

    public Iterable<PropertyChangeEvent> events( final IEventFilter f ) {
        return new Iterable<PropertyChangeEvent>() {
            
            public Iterator<PropertyChangeEvent> iterator() {
                
                // Iterator
                return new Iterator<PropertyChangeEvent>() {
                    
                    Iterator<PropertyChangeEvent>   delegate = events.iterator();
                    PropertyChangeEvent             next;

                    public boolean hasNext() {
                        if (next != null) {
                            return true;
                        }
                        while (next == null && delegate.hasNext()) {
                            PropertyChangeEvent candidate = delegate.next();
                            if (f.accept( candidate )) {
                                next = candidate;
                            }
                        }
                        return next != null;
                    }

                    public PropertyChangeEvent next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        try {
                            return next;
                        } finally {
                            next = null;
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public void addRemoved( String id ) {
        throw new RuntimeException( "not yet implemented." );
    }
    
    public Iterable<String> removed() {
        throw new RuntimeException( "not yet implemented." );
    }
    
}
