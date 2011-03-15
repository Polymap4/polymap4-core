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

package org.polymap.core.model.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.model.MDomainChangeEvent;
import org.polymap.core.model.MDomainChangeListener;
import org.polymap.core.model.MFeature;
import org.polymap.core.model.MList;
import org.polymap.core.model.MObject;
import org.polymap.core.model.MObjectClass;
import org.polymap.core.model.MReferences;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class MAbstractContentProvider
        implements ITreeContentProvider, MDomainChangeListener {

    private static Log log = LogFactory.getLog( MAbstractContentProvider.class );

    private MObject                     input;
    
    private Viewer                      viewer;

    /** The cache for the structure: maps parent to children. */
    private Map<MObject,List>           childrenMap = new HashMap();

    
    // SPI ************************************************

    /**
     * Example:
     * <pre>
     * if (feature.equals( XXXImpl.feature )) {
     *     return true;
     * }
     * return false;
     * </pre>
     */
    protected abstract boolean isChildFeature( MObject obj, MFeature feature );
    
    
//    /**
//     * Example:
//     * <pre>
//     * if (feature.equals( XXXImpl.feature )) {
//     *     return true;
//     * }
//     * return false;
//     * </pre>
//     */
//    protected abstract boolean isParentFeature( MObject obj, MFeature feature );
    
    
    // impl ***********************************************

    public void dispose() {
        if (input != null) {
            input.getDomain().removeDomainChangeListener( this );
        }
        input = null;
        viewer = null;
        childrenMap.clear();
    }

    
    public void inputChanged( Viewer _viewer, Object oldInput, Object newInput ) {
        log.debug( "newInput= " + newInput );
        if (newInput == null) {
            return;
        }
        dispose();
        this.input = (MObject)newInput;
        if (input != null) {
            input.getDomain().addDomainChangeListener( this );
        }
        this.viewer = _viewer;
    }


    public void domainChanged( MDomainChangeEvent ev ) {
        log.debug( "..." );
        childrenMap.clear();
        
        viewer.getControl().getDisplay().asyncExec( new Runnable() {            
            public void run() {
                viewer.refresh();
            }
        });
    }
    
    
    // ITreeContentProvider *******************************
    
    public Object[] getElements( Object _input ) {
        log.debug( "_input: " + _input );
        return getChildren( _input );
    }

    
    public Object[] getChildren( Object parent ) {
        log.debug( "parent= " + parent + " ******************" );
        List result = childrenMap.get( parent );
        if (result == null) {
            MObject obj = (MObject)parent;
            MObjectClass mcl = obj.getObjectClass();

            result = new ArrayList();
            for (MFeature feature : mcl.getFeatures()) {

                if (isChildFeature( obj, feature )) {
                    MList refs = ((MReferences)feature).get( obj );
                    result.addAll( refs );
                    log.debug( "        obj= " + obj + ", feature= " + feature.getName() + ", refs= " + refs );
                }
            }
            childrenMap.put( obj, result );
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
