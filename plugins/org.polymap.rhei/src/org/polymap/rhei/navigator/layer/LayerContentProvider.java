/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
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
 * $Id: $
 */
package org.polymap.rhei.navigator.layer;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.model.AssocCollection;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public class LayerContentProvider
        implements ITreeContentProvider {

    
    public Object[] getChildren( Object elm ) {
        if (elm instanceof IMap) {
            AssocCollection<ILayer> layers = ((IMap)elm).getLayers();
            return layers.toArray(new ILayer[layers.size()]);
        }
        return null;
    }


    public Object getParent( Object elm ) {
        if (elm instanceof ILayer) {
            try {
                return ((ILayer)elm).getMap();
            }
            catch (NoSuchEntityException e) {
            }
        }
        return null;
    }


    public boolean hasChildren(Object element) {
        return getChildren(element) != null;
    }

    
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    
    public void dispose() {
        // TODO Auto-generated method stub

    }

    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub
    }
    
}
