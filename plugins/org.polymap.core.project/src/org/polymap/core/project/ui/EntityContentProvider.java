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
 */
package org.polymap.core.project.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.event.IEventFilter;
import org.polymap.core.project.ProjectRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public abstract class EntityContentProvider
        implements ITreeContentProvider, IModelChangeListener {

    private static Log log = LogFactory.getLog( EntityContentProvider.class );

    private Object                      input;
    
    private Viewer                      viewer;

    /** The cache for the structure: maps parent to children. */
    private Map<Object,List>            childrenMap = new HashMap();

    
    public void dispose() {
        if (input != null) {
            ProjectRepository.instance().removeModelChangeListener( this );
        }
        input = null;
        viewer = null;
        childrenMap.clear();
    }

    
    public void inputChanged( Viewer _viewer, Object oldInput, Object newInput ) {
        //log.debug( "newInput= " + newInput );
        if (newInput == null) {
            return;
        }
        dispose();
        this.input = newInput;
        if (input != null) {
            ProjectRepository.instance().addModelChangeListener( this, IEventFilter.ALL );
        }
        this.viewer = _viewer;
    }


    public void modelChanged( ModelChangeEvent ev ) {
        // XXX check if and which of our entities are affected
        childrenMap.clear();
        
        viewer.getControl().getDisplay().asyncExec( new Runnable() {            
            public void run() {
                try {
                    viewer.refresh();
                }
                // qi4j seems to not have QiEntity.equals() implemented correctly;
                catch (Exception e) {
                    log.warn( "unhandled:" + e, e );
                }
            }
        });
    }
    
    
    // ITreeContentProvider *******************************
    
    public Object[] getElements( Object _input ) {
        log.debug( "_input: " + _input );
        return getChildren( _input );
    }

    
    protected abstract Collection _getChildren( Object parent );
            
    
    public final Object[] getChildren( Object parent ) {
        log.debug( "parent= " + parent + " ******************" );
        List result = childrenMap.get( parent );
        if (result == null) {
            result = new ArrayList();
            for (Object child : _getChildren( parent )) {
                result.add( child );
            }
            childrenMap.put( parent, result );
        }
        return result.toArray();
    }

    
    public boolean hasChildren( Object elm ) {
        // result is cached by getChildren()        
        return getChildren( elm ).length > 0;
    }


    public Object getParent( Object elm ) {
        for (Map.Entry entry : childrenMap.entrySet() ) {
            List children = (List)entry.getValue();
            if (children.contains( elm )) {
                return entry.getKey();
            }
        }
        return null;
    }

}
