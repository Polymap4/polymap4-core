/* 
 * polymap.org
 * Copyright (C) 2010-2016 Falko Bräutigam. All rights reserved.
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

import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectNode;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ProjectNodeContentProvider
        implements ITreeContentProvider {

    private static final Log log = LogFactory.getLog( ProjectNodeContentProvider.class );
    
    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        log.debug( "new: " + newInput );
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getChildren( Object elm ) {
        if (elm instanceof IMap) {
            return ((IMap)elm).layers.stream()
                    .sorted( ILayer.ORDER_KEY_ORDERING.reversed() )
                    .collect( Collectors.toList() ).toArray();
        }
        return null;
    }

    @Override
    public Object getParent( Object elm ) {
        if (elm instanceof ProjectNode) {
            return ((ProjectNode)elm).parentMap.get();
        }
        return null;
    }

    @Override
    public boolean hasChildren( Object elm ) {
        return getChildren( elm ) != null;
    }

    @Override
    public Object[] getElements( Object input ) {
        return getChildren( input );
    }
    
}
