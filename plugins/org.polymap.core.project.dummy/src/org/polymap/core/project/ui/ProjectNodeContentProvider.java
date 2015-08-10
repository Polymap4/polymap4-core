/* 
 * polymap.org
 * Copyright (C) 2010-2015 Falko Br�utigam. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectNode;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class ProjectNodeContentProvider
        implements ITreeContentProvider {

    private static final Log log = LogFactory.getLog( ProjectNodeContentProvider.class );
    

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        log.debug( "new: " + newInput );
    }


    public void dispose() {
    }


    public Object[] getChildren( Object elm ) {
        if (elm instanceof IMap) {
            ((IMap)elm).layers.toArray();
        }
        return null;
    }


    public Object getParent( Object elm ) {
        if (elm instanceof ProjectNode) {
            return ((ProjectNode)elm).parentMap.get();
        }
        return null;
    }


    public boolean hasChildren( Object element ) {
        return getChildren( element ) != null;
    }


    public Object[] getElements( Object inputElement ) {
        return getChildren( inputElement );
    }
    
}
